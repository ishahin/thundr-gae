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
import com.threewks.thundr.profiler.NoProfiler;
import com.threewks.thundr.profiler.Profiler;

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
	}

	@Test
	public void shouldSetThundrEnvironmentFromAppengineEnvironment() {
		Environment.set(null);
		new GaeInjectionConfiguration().configure(injectionContext);
		assertThat(Environment.get(), is("dev"));
	}

	@Test
	public void shouldInjectHttpService() {
		injectionContext.inject(NoProfiler.class).as(Profiler.class);
		new GaeInjectionConfiguration().configure(injectionContext);
		assertThat(injectionContext.get(HttpService.class), is(notNullValue()));
		assertThat(injectionContext.get(URLFetchService.class), is(notNullValue()));
	}
}
