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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.ETransformer;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.service.HttpRequest;
import com.threewks.thundr.http.service.HttpService;
import com.threewks.thundr.http.service.typeTransformer.IncomingByteArrayTypeTransformer;
import com.threewks.thundr.http.service.typeTransformer.IncomingInputStreamTypeTransformer;
import com.threewks.thundr.http.service.typeTransformer.IncomingStringTypeTransformer;
import com.threewks.thundr.http.service.typeTransformer.OutgoingDefaultTypeTransformer;
import com.threewks.thundr.http.service.typeTransformer.OutgoingStringTypeTransformer;

public class HttpServiceImpl implements HttpService {
	private Map<Class<?>, ETransformer<?, InputStream>> outgoingTypeConvertors = new LinkedHashMap<Class<?>, ETransformer<?, InputStream>>();
	private List<Class<?>> outgoingTypeConvertorOrder = new ArrayList<Class<?>>();
	private Map<Class<?>, ETransformer<InputStream, ?>> incomingTypeConvertors = new HashMap<Class<?>, ETransformer<InputStream, ?>>();

	private URLFetchService fetchService;

	public HttpServiceImpl(URLFetchService fetchService) {
		this.fetchService = fetchService;
		// default outgoing transformers
		addOutgoingTypeConvertor(Object.class, new OutgoingDefaultTypeTransformer());
		addOutgoingTypeConvertor(String.class, new OutgoingStringTypeTransformer());

		// default incoming transformers
		addIncomingTypeConvertor(String.class, new IncomingStringTypeTransformer());
		addIncomingTypeConvertor(InputStream.class, new IncomingInputStreamTypeTransformer());
		addIncomingTypeConvertor(byte[].class, new IncomingByteArrayTypeTransformer());
	}

	public <T> void addIncomingTypeConvertor(Class<T> type, ETransformer<InputStream, T> convertor) {
		incomingTypeConvertors.put(type, convertor);
	}

	public <T> void addOutgoingTypeConvertor(Class<T> type, ETransformer<T, InputStream> convertor) {
		outgoingTypeConvertors.put(type, convertor);
		outgoingTypeConvertorOrder.add(type);
	}

	public HttpRequest request(String url) {
		return new HttpRequestImpl(this, url);
	}

	@SuppressWarnings("unchecked")
	public <T> InputStream convertOutgoing(T t) {
		for (int i = outgoingTypeConvertorOrder.size() - 1; i >= 0; i--) {
			Class<?> type = outgoingTypeConvertorOrder.get(i);
			if (Cast.is(t, type)) {
				ETransformer<T, InputStream> transformer = (ETransformer<T, InputStream>) outgoingTypeConvertors.get(type);
				return transformer.from(t);
			}
		}
		throw new BaseException("Unable to convert the given object to an input stream, no convertor found. Object: %s", t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertIncoming(InputStream is, Class<T> type) {
		ETransformer<InputStream, ?> convertor = incomingTypeConvertors.get(type);
		if (convertor == null) {
			throw new BaseException("Unable to convert the response to the type %s, please make sure a convertor is registered", type.getName());
		}
		return (T) convertor.from(is);
	}

	public HttpResponseImpl fetch(HTTPRequest request) {
		Future<HTTPResponse> fetchAsync = fetchService.fetchAsync(request);
		return new HttpResponseImpl(fetchAsync, this);
	}
}
