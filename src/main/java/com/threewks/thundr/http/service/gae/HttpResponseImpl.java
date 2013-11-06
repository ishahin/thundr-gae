/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.http.service.gae;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.Expressive;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.threewks.thundr.http.HttpSupport.Header;
import com.threewks.thundr.http.service.HttpRequestException;
import com.threewks.thundr.http.service.HttpResponse;
import com.threewks.thundr.http.service.HttpResponseException;
import com.threewks.thundr.http.service.HttpService;
import com.threewks.thundr.logger.Logger;

public class HttpResponseImpl implements HttpResponse {
	private Future<HTTPResponse> future;
	private HTTPResponse response;
	private Map<String, List<String>> headers;
	private HttpService service;
	private Map<String, List<HttpCookie>> cookies;

	public HttpResponseImpl(Future<HTTPResponse> future, HttpService service) {
		this.future = future;
		this.service = service;
	}

	@Override
	public int getStatus() {
		return response().getResponseCode();
	}

	@Override
	public String getContentType() {
		return getHeader(Header.ContentType);
	}

	@Override
	public String getHeader(String name) {
		List<String> headers = getHeaders().get(name);
		return headers == null ? null : headers.get(0);
	}

	@Override
	public List<String> getHeaders(String name) {
		return getHeaders().get(name);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		response();
		return Collections.unmodifiableMap(headers);
	}

	@Override
	public HttpCookie getCookie(String cookieName) {
		List<HttpCookie> cookies = getCookies(cookieName);
		return cookies == null ? null : cookies.get(0);
	}

	@Override
	public List<HttpCookie> getCookies(String name) {
		response();
		return cookies.get(name);
	}

	@Override
	public List<HttpCookie> getCookies() {
		response();
		return Expressive.flatten(cookies.values());
	}

	@Override
	public String getBody() {
		return getBody(String.class);
	}

	@Override
	public <T> T getBody(Class<T> as) {
		return service.convertIncoming(getBodyAsStream(), as);
	}

	@Override
	public byte[] getBodyAsBytes() {
		return response().getContent();
	}

	@Override
	public InputStream getBodyAsStream() {
		byte[] content = response().getContent();
		content = content == null ? new byte[0] : content;
		return new ByteArrayInputStream(content);
	}

	@Override
	public URI getUri() {
		try {
			return response().getFinalUrl().toURI();
		} catch (URISyntaxException e) {
			throw new HttpResponseException(e, "Uri cannot be parsed: %s", e.getMessage());
		}
	}

	private HTTPResponse response() {
		if (response == null) {
			try {
				response = future.get();
				headers = buildHeaderMap();
				cookies = buildCookieMap();
				return response;
			} catch (InterruptedException e) {
				throw new HttpRequestException("Failed to wait for completion of asynchronous request: %s", e.getMessage());
			} catch (ExecutionException e) {
				throw new HttpRequestException(e, "Failed to get result for asynchronous request: %s", e.getMessage());
			}
		}
		return response;
	}

	private Map<String, List<String>> buildHeaderMap() {
		Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
		for (HTTPHeader header : response.getHeadersUncombined()) {
			String key = header.getName();
			String value = header.getValue();
			List<String> values = headers.get(key);
			if (values == null) {
				values = new ArrayList<String>();
				headers.put(key, values);
			}
			values.add(value);
		}
		return headers;
	}

	private Map<String, List<HttpCookie>> buildCookieMap() {
		Map<String, List<HttpCookie>> cookies = new LinkedHashMap<String, List<HttpCookie>>();
		for (String setCookieHeader : getCookieHeaders(headers)) {
			List<HttpCookie> cookieSet = parseCookies(setCookieHeader);
			for (HttpCookie httpCookie : cookieSet) {
				String name = httpCookie.getName();
				List<HttpCookie> existingCookies = cookies.get(name);
				if (existingCookies == null) {
					existingCookies = new ArrayList<HttpCookie>();
					cookies.put(name, existingCookies);
				}
				existingCookies.add(httpCookie);
			}
		}
		return cookies;
	}

	/**
	 * Get all cookie headers from the given header map.
	 * 
	 * Note: this will get all "Set-Cookie" and "Set-Cookie2" headers.
	 * 
	 * @param headers the map of headers to get the set cookie headers from.
	 * @return a list of headers.
	 */
	static List<String> getCookieHeaders(Map<String, List<String>> headers) {
		EList<String> cookieHeaders = Expressive.list();
		cookieHeaders.addItems(headers.get(Header.SetCookie));
		cookieHeaders.addItems(headers.get(Header.SetCookie2));
		return cookieHeaders;
	}

	/**
	 * Safely parse a cookie header. If any exceptions are encountered then the exception is caught
	 * and an empty list is returned.
	 * 
	 * @param setCookieHeader the header to parse.
	 * @return a list of headers, or an empty list if anything goes wrong parsing the header.
	 */
	static List<HttpCookie> parseCookies(String setCookieHeader) {
		List<HttpCookie> cookies = new ArrayList<HttpCookie>();
		try {
			cookies = HttpCookie.parse(setCookieHeader);
		} catch (Exception e) {
			try {
				// old version of java (<7) fail for cookies with HttpOnly present, we'll strip that out and try again
				cookies = HttpCookie.parse(setCookieHeader.replaceAll("(?i);\\s*HttpOnly", ""));
			} catch (Exception e2) {
				Logger.warn("Unable to parse cookie from header '%s': %s", setCookieHeader, e2.getMessage());
			}
		}
		return cookies;
	}
}
