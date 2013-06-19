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
