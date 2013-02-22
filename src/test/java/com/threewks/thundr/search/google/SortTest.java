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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SortTest {

	@Test
	public void shouldCreateAndRetainFieldAndOrder() {
		assertThat(new Sort("fieldName", true).getField(), is("fieldName"));
		assertThat(new Sort("fieldName", true).isDescending(), is(true));
		assertThat(new Sort("differentFieldName", false).getField(), is("differentFieldName"));
		assertThat(new Sort("differentFieldName", false).isDescending(), is(false));
	}

	@Test
	public void shouldToStringToHumanReadableString() {
		assertThat(new Sort("fieldName", true).toString(), is("fieldName Desc"));
		assertThat(new Sort("otherField", false).toString(), is("otherField Asc"));
	}

	@Test
	public void shouldProvideEqualityAndHashcodeOnFieldNameAndDirection() {
		Sort fieldDesc = new Sort("Field", true);
		Sort fieldDesc2 = new Sort("Field", true);
		Sort fieldAsc = new Sort("Field", false);
		Sort field2Desc = new Sort("Field2", true);
		Sort field2Asc = new Sort("Field2", false);
		Sort fieldNullDesc = new Sort(null, true);
		Sort fieldNullAsc = new Sort(null, false);

		assertThat(fieldDesc.equals(fieldDesc), is(true));
		assertThat(fieldDesc.equals(fieldDesc2), is(true));
		assertThat(fieldDesc2.equals(fieldDesc), is(true));
		
		assertThat(fieldDesc.equals(fieldAsc), is(false));
		assertThat(fieldDesc.equals(field2Desc), is(false));
		assertThat(fieldDesc.equals(field2Asc), is(false));
		assertThat(fieldDesc.equals(fieldNullAsc), is(false));
		
		assertThat(fieldNullAsc.equals(fieldDesc), is(false));
		assertThat(fieldDesc.equals(fieldNullDesc), is(false));
		
		assertThat(fieldDesc.equals(null), is(false));
		assertThat(fieldDesc.equals("A String"), is(false));

	}
}
