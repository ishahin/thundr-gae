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

import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.threewks.thundr.configuration.Environment;
import com.threewks.thundr.http.service.HttpService;
import com.threewks.thundr.http.service.gae.HttpServiceImpl;
import com.threewks.thundr.injection.BaseInjectionConfiguration;
import com.threewks.thundr.injection.InjectionException;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.search.google.GoogleSearchService;
import com.threewks.thundr.search.google.SearchService;
import com.threewks.thundr.view.GlobalModel;

public class GaeInjectionConfiguration extends BaseInjectionConfiguration {
	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		Environment.set(GaeEnvironment.applicationId());
		String environment = Environment.get();
		Logger.info("Running as environment %s", environment);
		injectionContext.inject(environment).named("environment").as(String.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
		com.google.appengine.api.search.SearchService searchService = SearchServiceFactory.getSearchService();
		Cache cache = initialiseJCache();

		injectionContext.inject(cache).as(Cache.class);
		injectionContext.inject(memcacheService).as(MemcacheService.class);
		injectionContext.inject(urlFetchService).as(URLFetchService.class);
		injectionContext.inject(HttpServiceImpl.class).as(HttpService.class);
		injectionContext.inject(GoogleSearchService.class).as(SearchService.class);
		injectionContext.inject(searchService).as(com.google.appengine.api.search.SearchService.class);
		
		GlobalModel globalModel = injectionContext.get(GlobalModel.class);
		globalModel.put("applicationVersion", SystemProperty.applicationVersion.get());
		globalModel.put("environment", GaeEnvironment.applicationId());
	}

	private Cache initialiseJCache() {
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			Cache cache = cacheFactory.createCache(Collections.emptyMap());
			return cache;
		} catch (CacheException e) {
			throw new InjectionException(e, "Failed to initialise %s: %s", Cache.class.getName(), e.getMessage());
		}
	}
}