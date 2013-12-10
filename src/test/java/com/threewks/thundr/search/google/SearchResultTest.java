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

import com.google.appengine.api.search.OperationResult;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.StatusCode;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import static com.google.appengine.api.search.ScoredDocument.newBuilder;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchResultTest {

	private SearchResult<String> searchResult;
	@SuppressWarnings("unchecked")
	private Future<Results<ScoredDocument>> searchAsync = mock(Future.class);

	@Before
	public void setUp() throws Exception {
		when(searchAsync.get()).thenReturn(mockResults());
	}

	private MockResults<ScoredDocument> mockResults() {
		ScoredDocument.Builder doc1Builder = newBuilder();
		doc1Builder.setId("docId1");

		ScoredDocument.Builder doc2Builder = newBuilder();
		doc2Builder.setId("docId2");

		List<ScoredDocument> docs = asList(doc1Builder.build(), doc2Builder.build());

		return new MockResults<>(docs);
	}

	@Test
	public void shouldGetTwoSearchResultIdsOffsetIsZero() {
		searchResult = new SearchResult<>(String.class, searchAsync, 0);
		List<String> expectedResults = asList("docId1", "docId2");
		assertThat(this.searchResult.getSearchResultIds(), is(expectedResults));
	}

	@Test
	public void shouldGetOneSearchResultIdsOffsetIsOne() {
		searchResult = new SearchResult<>(String.class, searchAsync, 1);
		List<String> expectedResults = asList("docId2");
		assertThat(this.searchResult.getSearchResultIds(), is(expectedResults));
	}

	@Test
	public void shouldGetTwoMatchingRecordsOffsetIsOne() {
		searchResult = new SearchResult<>(String.class, searchAsync, 1);
		assertThat(this.searchResult.getMatchingRecordCount(), is(2L));
	}

	@Test
	public void shouldGetOneReturnedRecordWhenOffsetIsOne() {
		searchResult = new SearchResult<>(String.class, searchAsync, 1);
		assertThat(this.searchResult.getReturnedRecordCount(), is(1L));
	}

	private class MockResults<T> extends Results<T> {
		private static final long serialVersionUID = 754439389254135943L;
		private List<T> results;

		public MockResults(List<T> results) {
			super(new OperationResult(StatusCode.OK, ""), results, 0, 0, null);
			this.results = results;
		}

		@Override
		public Collection<T> getResults() {
			return results;
		}

		@Override
		public long getNumberFound() {
			return results.size();
		}
	}
}
