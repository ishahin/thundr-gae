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

/**
 * Represents a search on the {@link SearchService}. Searches are performed by building the request up by invoking
 * fluent methods.
 * 
 * 
 * 
 * @param <T>
 */
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

	/**
	 * includes a string query which applies across all fields in the index.
	 * 
	 * @param query
	 * @return
	 */
	public SearchRequest<T> query(CharSequence query) {
		this.queryFragments.add(query.toString());
		return this;
	}

	/**
	 * Limits the number of results in the final {@link SearchResult}
	 * 
	 * @param limit
	 * @return
	 */
	public SearchRequest<T> limit(Integer limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * Adjusts the results in the final {@link SearchResult} such that the given number of results are skipped over
	 * and not included in the results.
	 * 
	 * @param offset
	 * @return
	 */
	public SearchRequest<T> offset(Integer offset) {
		this.offset = offset;
		return this;
	}

	/**
	 * Defines a search operation on the given field to apply to the current search. The operation is specified on the returned {@link SearchOperation} instance.
	 * 
	 * e.g. search.field("fieldName").lessThan(50);
	 * 
	 * @param field
	 * @return
	 */
	public SearchOperation<T> field(String field) {
		field = encodeFieldName(field);
		return new SearchOperation<T>(this, field);
	}

	/**
	 * Defines a sort order on the given field to apply to the current search. The operation is specified on the returned {@link SortOperation} instance.
	 * 
	 * e.g. search.order("fieldName").ascending();
	 * 
	 * @param field
	 * @return
	 */
	public SortOperation<T> order(String field) {
		field = encodeFieldName(field);
		return new SortOperation<T>(this, field);
	}

	/**
	 * Performs the search operation by combining all the previously specified search operations, sort orders, limits and offset.
	 * 
	 * @return
	 */
	public SearchResult<T> search() {
		return searchService.createSearchResult(this, type);
	}

	/**
	 * @return the ordered series of query fragments that were specified on this search request
	 */
	public List<String> query() {
		return queryFragments;
	}

	/**
	 * @return the ordered series of sort operations that were specified on this search request
	 */
	public List<Sort> sort() {
		return sortOrder;
	}

	/**
	 * @return the limit applied to this search request
	 */
	public Integer limit() {
		return limit;
	}

	/**
	 * @return the offset applied to this search request
	 */
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
