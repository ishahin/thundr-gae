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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.TranslatorFactory;
import com.googlecode.objectify.impl.translate.TranslatorRegistry;
import com.googlecode.objectify.impl.translate.opt.joda.DateTimeZoneTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.LocalDateTimeTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.LocalDateTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.LocalTimeTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.ReadableInstantTranslatorFactory;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.test.TestSupport;

public class ObjectifyModuleTest {

	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Test
	public void shouldRegisterTranslatorsAndInjectObjectifyFactory() {
		new ObjectifyModule().initialise(injectionContext);

		assertThat(injectionContext.get(ObjectifyFactory.class), is(notNullValue()));

		TranslatorRegistry translators = ObjectifyService.factory().getTranslators();
		List<TranslatorFactory<?>> factories = TestSupport.getField(translators, "translators");
		assertThat(factoriesContain(factories, ReadableInstantTranslatorFactory.class), is(true));
		assertThat(factoriesContain(factories, LocalDateTranslatorFactory.class), is(true));
		assertThat(factoriesContain(factories, LocalDateTimeTranslatorFactory.class), is(true));
		assertThat(factoriesContain(factories, LocalTimeTranslatorFactory.class), is(true));
		assertThat(factoriesContain(factories, DateTimeZoneTranslatorFactory.class), is(true));

	}

	private boolean factoriesContain(List<TranslatorFactory<?>> factories, Class<? extends TranslatorFactory<?>> class1) {
		for (TranslatorFactory<?> factory : factories) {
			if (factory.getClass() == class1) {
				return true;
			}
		}
		return false;
	}
}
