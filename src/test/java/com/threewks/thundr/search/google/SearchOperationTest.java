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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class SearchOperationTest {

	private SearchRequest<BigDecimal> searchRequest;
	private SearchOperation<BigDecimal> searchOperation;
	@SuppressWarnings("deprecation")
	private Date dateValue = new Date(2013 - 1900, 0, 1, 0, 0, 0);
	private String stringPhraseValue = "String value";
	private String stringValue = "string_value";
	private long longValue = 123L;
	private AtomicInteger atomicIntValue = new AtomicInteger(123);

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		searchRequest = mock(SearchRequest.class);
		when(searchRequest.query(anyString())).thenReturn(searchRequest);
		searchOperation = new SearchOperation<BigDecimal>(searchRequest, "fieldName");
	}

	@Test
	public void shouldApplyQueryForDateEquality() {
		SearchRequest<BigDecimal> chained = searchOperation.eq(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateLessThan() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThan(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateLessThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThanEquals(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<=2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateGreaterThan() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThan(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateGreaterThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThanEquals(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>=2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateIs() {
		SearchRequest<BigDecimal> chained = searchOperation.is(dateValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:2013-01-01");
	}

	@Test
	public void shouldApplyQueryForDateIn() {
		@SuppressWarnings("deprecation")
		Date dateValue2 = new Date(2013 - 1900, 1, 12, 0, 0, 0);
		SearchRequest<BigDecimal> chained = searchOperation.in(dateValue, dateValue2, null);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(2013-01-01 OR 2013-02-12)");
	}

	@Test
	public void shouldApplyQueryForStringEquality() {
		SearchRequest<BigDecimal> chained = searchOperation.eq(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=\"String value\"");
	}

	@Test
	public void shouldApplyQueryForStringLessThan() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThan(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<\"String value\"");
	}

	@Test
	public void shouldApplyQueryForStringLessThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThanEquals(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<=\"String value\"");
	}

	@Test
	public void shouldApplyQueryForStringGreaterThan() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThan(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>\"String value\"");
	}

	@Test
	public void shouldApplyQueryForStringGreaterThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThanEquals(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>=\"String value\"");
	}

	@Test
	public void shouldApplyQueryForStringIs() {
		SearchRequest<BigDecimal> chained = searchOperation.is(stringPhraseValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(String value)");
	}

	@Test
	public void shouldApplyQueryForStringIn() {
		SearchRequest<BigDecimal> chained = searchOperation.in(stringPhraseValue, "other value", null);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(\"String value\" OR \"other value\")");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringEquality() {
		SearchRequest<BigDecimal> chained = searchOperation.eq(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=\"string_value\"");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringLessThan() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThan(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<\"string_value\"");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringLessThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThanEquals(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<=\"string_value\"");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringGreaterThan() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThan(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>\"string_value\"");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringGreaterThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThanEquals(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>=\"string_value\"");
	}
	
	@Test
	public void shouldApplyQueryForEscapedQuotedSingleWordString() {
		String quotedStringValue = "\"Oh no\" says O'Neil";
		SearchRequest<BigDecimal> chained = searchOperation.eq(quotedStringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=\"\\\"Oh no\\\" says O'Neil\"");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringIs() {
		SearchRequest<BigDecimal> chained = searchOperation.is(stringValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(string_value)");
	}

	@Test
	public void shouldApplyQueryForSingleWordStringIn() {
		SearchRequest<BigDecimal> chained = searchOperation.in(stringValue, "other value", null);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(\"string_value\" OR \"other value\")");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongEquality() {
		SearchRequest<BigDecimal> chained = searchOperation.eq(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongLessThan() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThan(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongLessThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThanEquals(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongGreaterThan() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThan(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongGreaterThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThanEquals(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongIs() {
		SearchRequest<BigDecimal> chained = searchOperation.is(longValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:123");
	}

	@Test
	public void shouldApplyQueryForNumberAsLongIn() {
		long longValue2 = 123456789L;
		SearchRequest<BigDecimal> chained = searchOperation.in(longValue, longValue2, null);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(123 OR 123456789)");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerEquality() {
		SearchRequest<BigDecimal> chained = searchOperation.eq(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerLessThan() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThan(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerLessThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.lessThanEquals(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName<=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerGreaterThan() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThan(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerGreaterThanEquals() {
		SearchRequest<BigDecimal> chained = searchOperation.greaterThanEquals(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName>=123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerIs() {
		SearchRequest<BigDecimal> chained = searchOperation.is(atomicIntValue);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:123");
	}

	@Test
	public void shouldApplyQueryForNumberAsAtomicIntergerIn() {
		long atomicIntValue2 = 123456789L;
		SearchRequest<BigDecimal> chained = searchOperation.in(atomicIntValue, atomicIntValue2, null);

		assertThat(chained, is(sameInstance(searchRequest)));
		verify(searchRequest).query("fieldName:(123 OR 123456789)");
	}

	@Test
	public void testCorrectNumericComparisions() {
		GoogleSearchService searchService = mock(GoogleSearchService.class);
		SearchRequest<TestPojo> request = new SearchRequest<TestPojo>(searchService, TestPojo.class);
		SearchOperation<TestPojo> op = new SearchOperation<TestPojo>(request, "price");
		op.greaterThanEquals(new BigDecimal(15.50));
		assertThat(request.query().get(0), is("price>=15.5"));

		request = new SearchRequest<TestPojo>(searchService, TestPojo.class);
		op = new SearchOperation<TestPojo>(request, "price");
		op.lessThanEquals(new BigDecimal(11.0));
		assertThat(request.query().get(0), is("price<=11"));

		request = new SearchRequest<TestPojo>(searchService, TestPojo.class);
		op = new SearchOperation<TestPojo>(request, "price");
		op.eq(new BigDecimal(11.0));
		assertThat(request.query().get(0), is("price=11"));

		request = new SearchRequest<TestPojo>(searchService, TestPojo.class);
		op = new SearchOperation<TestPojo>(request, "price");
		op.greaterThan(new BigDecimal(11.0));
		assertThat(request.query().get(0), is("price>11"));

		request = new SearchRequest<TestPojo>(searchService, TestPojo.class);
		op = new SearchOperation<TestPojo>(request, "price");
		op.lessThan(new BigDecimal(11.0));
		assertThat(request.query().get(0), is("price<11"));
	}

	static class TestPojo {
		private BigDecimal price;

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}
	}
}
