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

import jodd.util.StringPool;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

public class GaeEnvironment {
	private static final String DEV_APPLICATION_ID = "dev";

	/**
	 * @return The application version deployed to. In dev server, returns an empty string
	 */
	public static String applicationVersion() {
		// The version identifier for the current application version.
		// Result is of the form <major>.<minor> where <major> is the version name supplied at deploy time
		// and <minor> is a timestamp value maintained by App Engine
		String fullApplicationVersion = SystemProperty.applicationVersion.get();
		return isProduction() ? StringUtils.substringBefore(fullApplicationVersion, ".") : StringPool.EMPTY;
	}

	public static boolean isProduction() {
		return Environment.Value.Production.name() == SystemProperty.Environment.environment.get();
	}

	/**
	 * Get the application id of the running application. When running in Development mode
	 * (ie: in a local SDK environment) this simply returns the value "dev". When running
	 * in Production mode, this returns the value of the application id.
	 * 
	 * @return the application id.
	 */
	public static String applicationId() {
		return isProduction() ? SystemProperty.applicationId.get() : DEV_APPLICATION_ID;
	}
}
