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

import java.util.Date;

public class SearchOperation<E extends RepositoryEntity> {
	private com.threewks.thundr.search.google.SearchOperation<E> delegate;
	private Search<E> search;

	public SearchOperation(Search<E> search, com.threewks.thundr.search.google.SearchOperation<E> delegate) {
		super();
		this.search = search;
		this.delegate = delegate;
	}

	public Search<E> is(String value) {
		delegate.is(value);
		return search;
	}

	public Search<E> is(Number value) {
		delegate.is(value);
		return search;
	}

	public Search<E> is(Date value) {
		delegate.is(value);
		return search;
	}

	public Search<E> in(Date... values) {
		delegate.in(values);
		return search;
	}

	public Search<E> inDates(Iterable<Date> values) {
		delegate.inDates(values);
		return search;
	}

	public Search<E> in(Iterable<String> values) {
		delegate.in(values);
		return search;
	}

	public Search<E> in(String... values) {
		delegate.in(values);
		return search;
	}

	public Search<E> inNumbers(Iterable<Number> values) {
		delegate.inNumbers(values);
		return search;
	}

	public Search<E> in(Number... values) {
		delegate.in(values);
		return search;
	}

	public Search<E> eq(String value) {
		delegate.eq(value);
		return search;
	}

	public Search<E> eq(Number value) {
		delegate.eq(value);
		return search;
	}

	public Search<E> eq(Date value) {
		delegate.eq(value);
		return search;
	}

	public Search<E> lessThan(String value) {
		delegate.lessThan(value);
		return search;
	}

	public Search<E> lessThan(Number value) {
		delegate.lessThan(value);
		return search;
	}

	public Search<E> lessThan(Date value) {
		delegate.lessThan(value);
		return search;
	}

	public Search<E> lessThanEquals(String value) {
		delegate.lessThanEquals(value);
		return search;
	}

	public Search<E> lessThanEquals(Number value) {
		delegate.lessThanEquals(value);
		return search;
	}

	public Search<E> lessThanEquals(Date value) {
		delegate.lessThanEquals(value);
		return search;
	}

	public Search<E> greaterThan(String value) {
		delegate.greaterThan(value);
		return search;
	}

	public Search<E> greaterThan(Number value) {
		delegate.greaterThan(value);
		return search;
	}

	public Search<E> greaterThan(Date value) {
		delegate.greaterThan(value);
		return search;
	}

	public Search<E> greaterThanEquals(String value) {
		delegate.greaterThanEquals(value);
		return search;
	}

	public Search<E> greaterThanEquals(Number value) {
		delegate.greaterThanEquals(value);
		return search;
	}

	public Search<E> greaterThanEquals(Date value) {
		delegate.greaterThanEquals(value);
		return search;
	}

	public String toString() {
		return delegate.toString();
	}

}
