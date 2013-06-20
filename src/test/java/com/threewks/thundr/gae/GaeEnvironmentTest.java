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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

public class GaeEnvironmentTest {
	@After
	public void after() {
		SystemProperty.Environment.environment.set(Environment.Value.Development);
	}

	@Test
	public void shouldReturnEmptyStringForApplicationMajorVersionInDevMode() {
		assertThat(GaeEnvironment.applicationVersion(), is(""));
	}

	@Test
	public void shouldReturnApplicationMajorVersion() {
		SystemProperty.Environment.environment.set(Environment.Value.Production);
		SystemProperty.applicationVersion.set("");

		assertThat(GaeEnvironment.applicationVersion(), is(""));
		SystemProperty.applicationVersion.set("1");

		assertThat(GaeEnvironment.applicationVersion(), is("1"));
		SystemProperty.applicationVersion.set("1.123456");
		assertThat(GaeEnvironment.applicationVersion(), is("1"));

		SystemProperty.applicationVersion.set("2.123456");
		assertThat(GaeEnvironment.applicationVersion(), is("2"));

		SystemProperty.applicationVersion.set("textversion");
		assertThat(GaeEnvironment.applicationVersion(), is("textversion"));

		SystemProperty.applicationVersion.set("textversion.123456");
		assertThat(GaeEnvironment.applicationVersion(), is("textversion"));

		SystemProperty.applicationVersion.set("textversion.");
		assertThat(GaeEnvironment.applicationVersion(), is("textversion"));

		SystemProperty.applicationVersion.set(".12345");
		assertThat(GaeEnvironment.applicationVersion(), is(""));
	}

	@Test
	public void shouldReturnFalseForIsProductionWhenInDev() {
		assertThat(GaeEnvironment.isProduction(), is(false));
	}

	@Test
	public void shouldReturnTrueForIsProductionSystemPropertyEnvironmentIsSetToProduction() {
		SystemProperty.Environment.environment.set(Environment.Value.Production);
		assertThat(GaeEnvironment.isProduction(), is(true));
	}

	@Test
	public void shouldReturnApplicationIdOfDevInDevmode() {
		assertThat(GaeEnvironment.applicationId(), is("dev"));
	}

	@Test
	public void shouldReturnApplicationIdWhenInProductionMode() {
		SystemProperty.Environment.environment.set(Environment.Value.Production);
		SystemProperty.Environment.applicationId.set("appid");
		assertThat(GaeEnvironment.applicationId(), is("appid"));
	}
}
