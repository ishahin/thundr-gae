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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SearchRequest<T> {
	private Class<T> type;
	private GoogleSearchService searchService;
	private List<String> queryFragments = new ArrayList<String>();
	private List<Sort> sortOrder = new ArrayList<Sort>();
	private Integer limit;
	private Integer offset;

	public SearchRequest(GoogleSearchService searchService, Class<T> type) {
		this.searchService = searchService;
		this.type = type;
	}

	public SearchRequest<T> query(CharSequence query) {
		this.queryFragments.add(query.toString());
		return this;
	}

	public SearchRequest<T> limit(Integer limit) {
		this.limit = limit;
		return this;
	}

	public SearchRequest<T> offset(Integer offset) {
		this.offset = offset;
		return this;
	}

	public SearchOperation<T> field(String field) {
		field = encodeFieldName(field);
		return new SearchOperation<T>(this, field);
	}

	public SortOperation<T> order(String field) {
		field = encodeFieldName(field);
		return new SortOperation<T>(this, field);
	}

	public SearchResult<T> search() {
		return searchService.createSearchResult(this, type);
	}

	public List<String> query() {
		return queryFragments;
	}

	public List<Sort> sort() {
		return sortOrder;
	}

	public Integer limit() {
		return limit;
	}

	public Integer offset() {
		return offset;
	}

	// package protected for testing
	void sort(String field, boolean descending) {
		field = encodeFieldName(field);
		this.sortOrder.add(new Sort(field, descending));
	}

	@Override
	public String toString() {
		return type.getSimpleName() + " where " + (queryFragments.isEmpty() ? "*" : StringUtils.join(queryFragments, " ")) + (sortOrder.isEmpty() ? "" : " " + sortOrder);
	}

	public static String encodeFieldName(String fieldName) {
		// ensure the field name matches the requirements of the search interface
		return fieldName.replaceAll("\\.", "_");
	}
}
