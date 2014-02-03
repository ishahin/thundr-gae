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
package com.threewks.thundr.gae.objectify.repository;

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.search.google.GoogleSearchService;
import com.threewks.thundr.search.google.SearchRequest;
import com.threewks.thundr.search.google.Sort;

public class SearchTest {

	private BaseRepository<TestEntity> repository;
	private Search<TestEntity> search;
	private SearchRequest<TestEntity> searchRequest;
	private GoogleSearchService searchService;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		repository = mock(BaseRepository.class);
		searchRequest = new SearchRequest<>(searchService, TestEntity.class);
		search = new Search<>(repository, searchRequest);
	}

	@Test
	public void shouldApplyAndRetainLimit() {
		assertThat(search.limit(87), is(search));
		assertThat(search.limit(), is(87));
		assertThat(searchRequest.limit(), is(87));
	}

	@Test
	public void shouldApplyAndRetainOffset() {
		assertThat(search.offset(87), is(search));
		assertThat(search.offset(), is(87));
		assertThat(searchRequest.offset(), is(87));
	}

	@Test
	public void shouldApplyAndRetainQuery() {
		assertThat(search.query("Text"), is(search));
		assertThat(search.query(), hasItems("Text"));
		assertThat(searchRequest.query(), hasItems("Text"));
	}

	@Test
	public void shouldApplyFieldOperation() {
		assertThat(search.field("name").eq("value"), is(search));
		assertThat(search.query(), hasItems("name=\"value\""));
		assertThat(searchRequest.query(), hasItems("name=\"value\""));
	}

	@Test
	public void shouldApplyOrderOperation() {
		assertThat(search.order("name").descending(), is(search));
		assertThat(search.sort(), hasItems(new Sort("name", true)));
		assertThat(searchRequest.sort(), hasItems(new Sort("name", true)));
	}

	@Test
	public void shouldSearchByDelegatingToRepository() {
		List<TestEntity> results = list(new TestEntity("name"));
		when(repository.completeSearch(search)).thenReturn(results);
		assertThat(search.search(), is(results));

		verify(repository).completeSearch(search);
	}

	@Test
	public void shouldSearchByIdByDelegatingToRepository() {
		List<Long> results = list(1L, 2L);
		when(repository.completeIdSearch(search)).thenReturn(results);
		assertThat(search.searchIds(), is(results));

		verify(repository).completeIdSearch(search);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRedindexByDelegatingToRepository() {
		ReindexOperation<TestEntity> reindexOp = mock(ReindexOperation.class);
		when(repository.reindex(search, 11, reindexOp)).thenReturn(13);
		assertThat(search.reindex(11, reindexOp), is(13));

		verify(repository).reindex(search, 11, reindexOp);
	}
}
