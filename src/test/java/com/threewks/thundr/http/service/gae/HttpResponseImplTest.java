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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.atomicleopard.expressive.Expressive;

public class HttpResponseImplTest {

	@Test
	public void shouldGetCookieHeaders() {
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		headers.put("Set-Cookie", Expressive.list("cookie1", "cookie2"));
		headers.put("Set-Cookie2", Expressive.list("cookie3", "cookie4"));
		headers.put("Not a cookie", Expressive.list("abc", "def"));

		List<String> cookieHeaders = HttpResponseImpl.getCookieHeaders(headers);
		assertThat(cookieHeaders.size(), is(4));
		assertThat(cookieHeaders, hasItem("cookie1"));
		assertThat(cookieHeaders, hasItem("cookie2"));
		assertThat(cookieHeaders, hasItem("cookie3"));
		assertThat(cookieHeaders, hasItem("cookie4"));
	}

	@Test
	public void shouldParseValidCookie() {
		String setCookieHeader = "c9MWDuvPtT9GIMyPc3jwol1VSlO=KTxwHZpN2bq06GfbIx5XbS5FHhkcUswO2k_yMcChp75xSq4I_PM4QzXRq1x_byngAdLhKv2CkKjtIKlHQiz62v-4qEgWbZ4SYW8Z1yla36T1WQ4xAMpU3i6qk6ewlCGPnx6c3m%7cKoQcs7tw1-C635JDzbf-H64FgnYcXfBcFkJTzXQXRBcspb73TXDVTX1akuNL8rc5P3Nlq0%7cW6Lkbp7lOtYH8fLM1lPPAOwzX5E4napanxA7HOi0RPPaGTabWX97ZHwtXUh-z4Kj4iuhz0%7c1345612223; domain=.paypal.com; path=/; Secure; HttpOnly, cookie_check=yes; expires=Sat, 20-Aug-2022 05:10:23 GMT; domain=.paypal.com; path=/; Secure; HttpOnly, navcmd=_notify-validate; domain=.paypal.com; path=/; Secure; HttpOnly, navlns=0.0; expires=Tue, 17-Aug-2032 05:10:23 GMT; domain=.paypal.com; path=/; Secure; HttpOnly, Apache=10.72.109.11.1345612223427385; path=/; expires=Fri, 15-Aug-42 05:10:23 GMT";
		List<HttpCookie> cookies = HttpCookie.parse(setCookieHeader);
		assertThat(cookies.size(), is(1));
	}

	@Test
	public void shouldHandleInvalidCookie() {
		String setCookieHeader = "this is not a valid cookie";
		List<HttpCookie> cookies = HttpResponseImpl.parseCookies(setCookieHeader);
		assertThat(cookies.size(), is(0));

	}

}
