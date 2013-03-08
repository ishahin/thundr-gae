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

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.SearchService;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.threewks.thundr.test.TestSupport;

public class SearchServiceTest {

	private LocalServiceTestHelper helper;
	private GoogleSearchService searchService;
	private TestType testType = new TestType(1, 2L, new BigDecimal("1.23"), "String", new Date(1), true);

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

	@SuppressWarnings("unchecked")
	@Test
	public void shouldIndexObjectReturningIndexOperationWrappingFuture() throws InterruptedException, ExecutionException {
		Index index = mock(Index.class);
		Future<PutResponse> future = mock(Future.class);
		when(index.putAsync(Mockito.<Document> anyVararg())).thenReturn(future);
		SearchService localSearchService = spySearchService();
		doReturn(index).when(localSearchService).getIndex(Mockito.any(IndexSpec.class));

		IndexOperation indexOp = searchService.index(testType, "1", list("intType"));

		verify(future, times(0)).get();

		indexOp.complete();

		verify(future).get();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBuildDocumentGivenObjectAndNamedFields() throws InterruptedException, ExecutionException {
		Index index = mock(Index.class);
		Future<PutResponse> future = mock(Future.class);
		when(index.putAsync(Mockito.<Document> anyVararg())).thenReturn(future);
		SearchService localSearchService = spySearchService();
		doReturn(index).when(localSearchService).getIndex(Mockito.any(IndexSpec.class));

		searchService.index(testType, "1", list("intType", "longType", "bigDecType", "stringType", "dateType", "boolType"));

		ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
		verify(index).putAsync(captor.capture());

		Document document = captor.getValue();
		assertThat(document.getId(), is("1"));
		assertThat(document.getOnlyField("intType").getNumber(), is(1d));
		assertThat(document.getOnlyField("longType").getNumber(), is(2d));
		assertThat(document.getOnlyField("bigDecType").getNumber(), is(1.23d));
		assertThat(document.getOnlyField("stringType").getText(), is("String"));
		assertThat(document.getOnlyField("dateType").getDate(), is(new Date(1)));
		assertThat(document.getOnlyField("boolType").getText(), is("true"));
	}

	private SearchService spySearchService() {
		SearchService localSearchService = TestSupport.getField(searchService, "searchService");
		localSearchService = spy(localSearchService);
		TestSupport.setField(searchService, "searchService", localSearchService);
		return localSearchService;
	}

	@SuppressWarnings("unused")
	private class TestType {
		private int intType;
		private long longType;
		private BigDecimal bigDecType;
		private String stringType;
		private Date dateType;
		private boolean boolType;

		public TestType(int intType, long longType, BigDecimal bigDecType, String stringType, Date dateType, boolean boolType) {
			super();
			this.intType = intType;
			this.longType = longType;
			this.bigDecType = bigDecType;
			this.stringType = stringType;
			this.dateType = dateType;
			this.boolType = boolType;
		}

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
