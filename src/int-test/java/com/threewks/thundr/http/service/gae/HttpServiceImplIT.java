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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.threewks.thundr.http.service.HttpResponse;

public class HttpServiceImplIT {
	private static TestWebServer webServer;
	private LocalServiceTestHelper helper;
	private URLFetchService urlFetchService;

	@BeforeClass
	public static void beforeClass() throws IOException {
		webServer = new BasicTestWebServer();
		webServer.start();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		webServer.stop();
	}

	@Before
	public void before() {
		webServer.fatal.set(false);

		LocalURLFetchServiceTestConfig urlFetchConfig = new LocalURLFetchServiceTestConfig();
		helper = new LocalServiceTestHelper(urlFetchConfig);
		helper.setTimeZone(TimeZone.getDefault());
		helper.setUp();
		
		urlFetchService = URLFetchServiceFactory.getURLFetchService();
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void shouldPerformBasicHead() {
		HttpResponse response = new HttpServiceImpl(urlFetchService).request(webServer.getUri()).header("Content-Type", "text/plain").parameter("q", "shoes").head();
		assertThat(response.getBody(), is(""));
		assertThat(response.getHeader("headHeader"), is("expected header"));
		assertThat(response.getHeader("q"), is("shoes"));
		assertThat(response.getCookie("headCookie").getValue(), is("expected cookie"));
		failIfWebserverErrors();
	}

	@Test
	public void shouldPerformBasicGet() {
		HttpResponse response = new HttpServiceImpl(urlFetchService).request(webServer.getUri()).parameter("q", "shoes").get();
		assertThat(response.getBody(), is("Expected GET Result"));
		assertThat(response.getHeader("getHeader"), is("expected header"));
		assertThat(response.getHeader("q"), is("shoes"));
		assertThat(response.getCookie("getCookie").getValue(), is("expected cookie"));
		failIfWebserverErrors();
	}

	@Test
	public void shouldPerformBasicPost() {
		HttpResponse response = new HttpServiceImpl(urlFetchService).request(webServer.getUri()).parameter("query", "queryParam").post();
		assertThat(response.getBody(), is("Expected POST Result"));
		assertThat(response.getHeader("postHeader"), is("expected header"));
		assertThat(response.getHeader("query"), is("queryParam"));
		assertThat(response.getCookie("postCookie").getValue(), is("expected cookie"));
		failIfWebserverErrors();
	}

	@Test
	public void shouldPerformBasicPut() {
		HttpResponse response = new HttpServiceImpl(urlFetchService).request(webServer.getUri()).parameter("query", "queryParam").put();
		assertThat(response.getBody(), is("Expected PUT Result"));
		assertThat(response.getHeader("putHeader"), is("expected header"));
		assertThat(response.getHeader("query"), is("queryParam"));
		assertThat(response.getCookie("putCookie").getValue(), is("expected cookie"));
		failIfWebserverErrors();
	}

	@Test
	public void shouldPerformBasicDelete() {
		HttpResponse response = new HttpServiceImpl(urlFetchService).request(webServer.getUri()).parameter("query", "queryParam").delete();
		assertThat(response.getBody(), is("Expected DELETE Result"));
		assertThat(response.getHeader("deleteHeader"), is("expected header"));
		assertThat(response.getHeader("query"), is("queryParam"));
		assertThat(response.getCookie("deleteCookie").getValue(), is("expected cookie"));
		failIfWebserverErrors();
	}

	private void failIfWebserverErrors() {
		if (webServer.fatal.get()) {
			fail("Web server recorded a failure!");
		}
	}

	private static class BasicTestWebServer extends TestWebServer {
		@Override
		public void head(Request req, Response resp) throws Exception {
			resp.add("headHeader", "expected header");
			for (Map.Entry<String, String> queryParam : req.getQuery().entrySet()) {
				resp.add(queryParam.getKey(), queryParam.getValue());
			}
			resp.setCookie(new Cookie("headCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected HEAD Result");
		}

		@Override
		public void get(Request req, Response resp) throws Exception {
			resp.add("getHeader", "expected header");
			for (Map.Entry<String, String> queryParam : req.getQuery().entrySet()) {
				resp.add(queryParam.getKey(), queryParam.getValue());
			}
			resp.setCookie(new Cookie("getCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected GET Result");
		}

		@Override
		public void post(Request req, Response resp) throws Exception {
			resp.add("postHeader", "expected header");
			for (Map.Entry<String, String> param : req.getForm().entrySet()) {
				resp.add(param.getKey(), param.getValue());
			}
			resp.setCookie(new Cookie("postCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected POST Result");
		}

		@Override
		public void put(Request req, Response resp) throws Exception {
			resp.add("putHeader", "expected header");
			for (Map.Entry<String, String> param : req.getForm().entrySet()) {
				resp.add(param.getKey(), param.getValue());
			}
			resp.setCookie(new Cookie("putCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected PUT Result");
		}

		@Override
		public void delete(Request req, Response resp) throws Exception {
			resp.add("deleteHeader", "expected header");
			for (Map.Entry<String, String> queryParam : req.getQuery().entrySet()) {
				resp.add(queryParam.getKey(), queryParam.getValue());
			}
			resp.setCookie(new Cookie("deleteCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected DELETE Result");
		}

		@Override
		public void options(Request req, Response resp) throws Exception {
			resp.add("optionsHeader", "expected header");
			for (Map.Entry<String, String> queryParam : req.getQuery().entrySet()) {
				resp.add(queryParam.getKey(), queryParam.getValue());
			}
			resp.setCookie(new Cookie("optionsCookie", "expected cookie"));
			resp.setCode(200);
			resp.getPrintStream().print("Expected OPTIONS Result");
		}
	}
}
