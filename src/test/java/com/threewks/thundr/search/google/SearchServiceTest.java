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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.atomicleopard.expressive.Expressive;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void shouldIndexObjects() {
		Index index = mock(Index.class);
		Future<PutResponse> future = mock(Future.class);
		when(index.putAsync(Mockito.<Document> anyVararg())).thenReturn(future);
		SearchService localSearchService = spySearchService();
		doReturn(index).when(localSearchService).getIndex(Mockito.any(IndexSpec.class));

		Map<String, Object> objects = new LinkedHashMap<String, Object>();
		objects.put("1", testType);
		objects.put("2", new TestType(100, 2000L, new BigDecimal("100.2300"), "String String", new Date(1000), false));

		searchService.index(objects, list("intType", "longType", "bigDecType", "stringType", "dateType", "boolType"));

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(index).putAsync(captor.capture());

		List<Document> document = captor.getValue();
		assertThat(document.get(0).getId(), is("1"));
		assertThat(document.get(0).getOnlyField("intType").getNumber(), is(1d));
		assertThat(document.get(0).getOnlyField("longType").getNumber(), is(2d));
		assertThat(document.get(0).getOnlyField("bigDecType").getNumber(), is(1.23d));
		assertThat(document.get(0).getOnlyField("stringType").getText(), is("String"));
		assertThat(document.get(0).getOnlyField("dateType").getDate(), is(new Date(1)));
		assertThat(document.get(0).getOnlyField("boolType").getText(), is("true"));

		assertThat(document.get(1).getId(), is("2"));
		assertThat(document.get(1).getOnlyField("intType").getNumber(), is(100d));
		assertThat(document.get(1).getOnlyField("longType").getNumber(), is(2000d));
		assertThat(document.get(1).getOnlyField("bigDecType").getNumber(), is(100.2300d));
		assertThat(document.get(1).getOnlyField("stringType").getText(), is("String String"));
		assertThat(document.get(1).getOnlyField("dateType").getDate(), is(new Date(1000)));
		assertThat(document.get(1).getOnlyField("boolType").getText(), is("false"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRemoveFromIndex() throws InterruptedException, ExecutionException {
		Index index = mock(Index.class);
		Future<Void> future = mock(Future.class);
		when(index.deleteAsync(anyListOf(String.class))).thenReturn(future);
		SearchService localSearchService = spySearchService();
		doReturn(index).when(localSearchService).getIndex(Mockito.any(IndexSpec.class));

		IndexOperation indexOperation = searchService.remove(TestType.class, list("1", "2"));

		verify(index).deleteAsync(list("1", "2"));

		indexOperation.complete();

		verify(future).get();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRemoveAllFromIndex() throws InterruptedException, ExecutionException {
		Index index = mock(Index.class);

		GetResponse<Document> response = mock(GetResponse.class);
		when(index.getRange(Mockito.any(GetRequest.class))).thenReturn(response);
		SearchService localSearchService = spySearchService();
		doReturn(index).when(localSearchService).getIndex(Mockito.any(IndexSpec.class));

		Document doc1 = mockDocument("1");
		Document doc2 = mockDocument("2");
		when(response.getResults()).thenReturn(list(doc1, doc2), Expressive.<Document> list());
		when(response.iterator()).thenReturn(list(doc1, doc2).iterator(), Expressive.<Document> list().iterator());

		int count = searchService.removeAll(TestType.class);

		verify(index).delete(list("1", "2"));
		assertThat(count, is(2));

	}

	private Document mockDocument(String idg) {
		Document doc = mock(Document.class);
		when(doc.getId()).thenReturn(idg);
		return doc;
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
