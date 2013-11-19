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

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.threewks.thundr.search.google.SearchRequest;
import com.threewks.thundr.search.google.Sort;

public class Search<E extends RepositoryEntity> {
	protected SearchRequest<E> searchRequest;
	protected BaseRepository<E> repository;

	protected Search(BaseRepository<E> repository, SearchRequest<E> searchRequest) {
		super();
		this.searchRequest = searchRequest;
		this.repository = repository;
	}

	public List<E> search() {
		return repository.completeSearch(this);
	}

	public List<Long> searchIds() {
		return repository.completeIdSearch(this);
	}

	public int reindex(int batchSize, ReindexOperation<E> reindexOp) {
		return repository.reindex(this, batchSize, reindexOp);
	}

	public Search<E> query(CharSequence query) {
		searchRequest.query(query);
		return this;
	}

	public Search<E> limit(Integer limit) {
		searchRequest.limit(limit);
		return this;
	}

	public Search<E> offset(Integer offset) {
		searchRequest.offset(offset);
		return this;
	}

	public SearchOperation<E> field(String field) {
		return new SearchOperation<E>(this, searchRequest.field(field));
	}

	public SortOperation<E> order(String field) {
		return new SortOperation<E>(this, searchRequest.order(field));
	}

	public List<String> query() {
		return searchRequest.query();
	}

	public List<Sort> sort() {
		return searchRequest.sort();
	}

	public Integer limit() {
		return searchRequest.limit();
	}

	public Integer offset() {
		return searchRequest.offset();
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
