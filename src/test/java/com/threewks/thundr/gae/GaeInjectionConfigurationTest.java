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
package com.threewks.thundr.gae;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.threewks.thundr.configuration.Environment;
import com.threewks.thundr.http.service.HttpService;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;

public class GaeInjectionConfigurationTest {
	private LocalServiceTestHelper helper;
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Before
	public void before() {
		LocalDatastoreServiceTestConfig hrdDatastore = new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0.01f);
		LocalMemcacheServiceTestConfig memcacheConfig = new LocalMemcacheServiceTestConfig();
		LocalSearchServiceTestConfig searchConfig = new LocalSearchServiceTestConfig();
		helper = new LocalServiceTestHelper(hrdDatastore, memcacheConfig, searchConfig);
		helper.setTimeZone(TimeZone.getDefault());
		helper.setUp();
	}

	@After
	public void after() {
		helper.tearDown();
		Environment.set(null);
	}

	@Test
	public void shouldSetThundrEnvironmentFromAppengineEnvironment() {
		Environment.set(null);
		new GaeInjectionConfiguration().initialise(injectionContext);
		assertThat(Environment.get(), is("dev"));
	}

	@Test
	public void shouldInjectEnvironmentStringIntoInjectionContextWhenNoEnvironmentDefined() {
		new GaeInjectionConfiguration().initialise(injectionContext);
		assertThat(injectionContext.get(String.class, "environment"), is("dev"));
	}

	@Test
	public void shouldInjectHttpService() {
		new GaeInjectionConfiguration().configure(injectionContext);
		assertThat(injectionContext.get(HttpService.class), is(notNullValue()));
		assertThat(injectionContext.get(URLFetchService.class), is(notNullValue()));
	}
}
