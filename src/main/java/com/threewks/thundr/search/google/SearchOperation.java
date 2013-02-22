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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;

public class SearchOperation<T> {
	private static final String GreaterThan = ">";
	private static final String GreatThanOrEqualTo = ">=";
	private static final String LessThanOrEqualTo = "<=";
	private static final String LessThan = "<";
	private static final String Equals = "=";
	private static final String Is = ":";
	//private static final String IsLike = ":~";

	private static final DecimalFormat decimalFormat = new DecimalFormat("###0.##########");
	private static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
	private SearchRequest<T> searchRequest;
	private String field;

	public SearchOperation(SearchRequest<T> searchRequest, String field) {
		this.searchRequest = searchRequest;
		this.field = field;
	}

	public SearchRequest<T> is(String value) {
		return appendQuery(group(value), Is);
	}

	private String group(String value) {
		return String.format("(%s)", value);
	}

	public SearchRequest<T> is(Number value) {
		return appendQuery(value, Is);
	}

	public SearchRequest<T> is(Date value) {
		return appendQuery(value, Is);
	}

	public SearchRequest<T> in(Date... values) {
		return inDates(Arrays.asList(values));
	}

	public SearchRequest<T> inDates(Iterable<Date> values) {
		String value = orValues(values, formatDates, false);
		return appendQuery(value, Is);
	}

	public SearchRequest<T> in(Iterable<String> values) {
		String value = orValues(values, noneTransformer, true);
		return appendQuery(value, Is);
	}

	public SearchRequest<T> in(String... values) {
		return in(Arrays.asList(values));
	}

	public SearchRequest<T> inNumbers(Iterable<Number> values) {
		String value = orValues(values, formatNumbers, false);
		return appendQuery(value, Is);
	}

	public SearchRequest<T> in(Number... values) {
		return inNumbers(Arrays.asList(values));
	}

	// TODO - Search Service abstraction - equals is a terrible name for this, maybe eq or similar to avoid confusion with Object.equals
	public SearchRequest<T> equals(String value) {
		return appendQuery(quote.from(value), Equals);
	}

	// TODO - Search Service abstraction - equals is a terrible name for this, maybe eq or similar to avoid confusion with Object.equals
	public SearchRequest<T> equals(Number value) {
		return appendQuery(value, Equals);
	}

	// TODO - Search Service abstraction - equals is a terrible name for this, maybe eq or similar to avoid confusion with Object.equals
	public SearchRequest<T> equals(Date value) {
		return appendQuery(value, Equals);
	}

	public SearchRequest<T> lessThan(String value) {
		return appendQuery(quote.from(value), LessThan);
	}

	public SearchRequest<T> lessThan(Number value) {
		return appendQuery(value, LessThan);
	}

	public SearchRequest<T> lessThan(Date value) {
		return appendQuery(value, LessThan);
	}

	public SearchRequest<T> lessThanEquals(String value) {
		return appendQuery(quote.from(value), LessThanOrEqualTo);
	}

	public SearchRequest<T> lessThanEquals(Number value) {
		return appendQuery(value, LessThanOrEqualTo);
	}

	public SearchRequest<T> lessThanEquals(Date value) {
		return appendQuery(value, LessThanOrEqualTo);
	}

	public SearchRequest<T> greaterThan(String value) {
		return appendQuery(quote.from(value), GreaterThan);
	}

	public SearchRequest<T> greaterThan(Number value) {
		return appendQuery(value, GreaterThan);
	}

	public SearchRequest<T> greaterThan(Date value) {
		return appendQuery(value, GreaterThan);
	}

	public SearchRequest<T> greaterThanEquals(String value) {
		return appendQuery(quote.from(value), GreatThanOrEqualTo);
	}

	public SearchRequest<T> greaterThanEquals(Number value) {
		return appendQuery(value, GreatThanOrEqualTo);
	}

	public SearchRequest<T> greaterThanEquals(Date value) {
		return appendQuery(value, GreatThanOrEqualTo);
	}

	private SearchRequest<T> appendQuery(String value, String operation) {
		return value == null ? searchRequest : searchRequest.query(String.format("%s%s%s", field, operation, value));
	}

	private SearchRequest<T> appendQuery(Number value, String operation) {
		return appendQuery(formatNumber.from(value), operation);
	}

	private SearchRequest<T> appendQuery(Date value, String operation) {
		return appendQuery(formatDate.from(value), operation);
	}

	private <V> String orValues(Iterable<V> values, CollectionTransformer<V, String> transformer, boolean isString) {
		List<V> noNullValues = Expressive.list(values).removeItems(Expressive.Predicate.<V> isNull());
		List<String> strings = transformer.from(noNullValues);
		List<String> quoted = isString ? quoteAll.from(strings) : strings;
		return quoted.isEmpty() ? null : String.format("(%s)", StringUtils.join(quoted, " OR "));
	}

	private static ETransformer<Number, String> formatNumber = new ETransformer<Number, String>() {
		@Override
		public String from(Number from) {
			return decimalFormat.format(from);
		}
	};
	private static ETransformer<Date, String> formatDate = new ETransformer<Date, String>() {
		@Override
		public String from(Date from) {
			return dateFormat.format(from);
		}
	};
	private static ETransformer<String, String> quote = new ETransformer<String, String>() {
		@Override
		public String from(String from) {
			return String.format("\"%s\"", from);
		}
	};

	private static CollectionTransformer<Date, String> formatDates = Expressive.Transformers.transformAllUsing(formatDate);
	private static CollectionTransformer<Number, String> formatNumbers = Expressive.Transformers.transformAllUsing(formatNumber);
	private static CollectionTransformer<String, String> quoteAll = Expressive.Transformers.transformAllUsing(quote);
	private static final CollectionTransformer<String, String> noneTransformer = Expressive.Transformers.transformAllUsing(new ETransformer<String, String>() {
		@Override
		public String from(String from) {
			return from;
		}
	});

}
