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
package com.threewks.thundr.gae.objectify;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;

public class ObjectifyModule extends BaseModule {

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		ObjectifyFactory objectifyFactory = ObjectifyService.factory();
		runBasicConfiguration(objectifyFactory);
		injectionContext.inject(objectifyFactory).as(ObjectifyFactory.class);
	}

	public static void runBasicConfiguration(ObjectifyFactory objectifyFactory) {
		// register Objectify converter to convert between java.uil.Date and org.joda.time.DateTime
		JodaTimeTranslators.add(objectifyFactory);
	}
}
