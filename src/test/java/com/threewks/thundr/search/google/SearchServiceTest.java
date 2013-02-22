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
package com.threewks.thundr.search.google;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.threewks.thundr.search.google.GoogleSearchService;

public class SearchServiceTest {

	private LocalServiceTestHelper helper;
	private GoogleSearchService searchService;

	@Before
	public void before() {
		LocalSearchServiceTestConfig searchConfig = new LocalSearchServiceTestConfig();
		helper = new LocalServiceTestHelper(searchConfig);
		helper.setTimeZone(TimeZone.getDefault());
		helper.setUp();
		searchService = new GoogleSearchService();
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void shouldDetermineIfFieldIsNumeric() {
		// verify type detection using fields directly
		assertThat(searchService.isNumericField(TestType.class, "intType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "longType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "bigDecType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "stringType"), is(false));
		assertThat(searchService.isNumericField(TestType.class, "dateType"), is(false));
		assertThat(searchService.isNumericField(TestType.class, "boolType"), is(false));

		// verify type detection using getters
		assertThat(searchService.isNumericField(TestType.class, "getIntType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "getLongType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "getBigDecType"), is(true));
		assertThat(searchService.isNumericField(TestType.class, "getStringType"), is(false));
		assertThat(searchService.isNumericField(TestType.class, "getDateType"), is(false));
		assertThat(searchService.isNumericField(TestType.class, "isBoolType"), is(false));
	}

	@SuppressWarnings("unused")
	private class TestType {
		private int intType;
		private long longType;
		private BigDecimal bigDecType;
		private String stringType;
		private Date dateType;
		private boolean boolType;

		public int getGetIntType() {
			return intType;
		}

		public BigDecimal getGetBigDecType() {
			return bigDecType;
		}

		public long getGetLongType() {
			return longType;
		}

		public String getGetStringType() {
			return stringType;
		}

		public Date getGetDateType() {
			return dateType;
		}

		public boolean isIsBoolType() {
			return boolType;
		}
	}

}
