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

public class SortOperation<E extends RepositoryEntity> {
	private Search<E> search;
	private com.threewks.thundr.search.google.SortOperation<E> sortOperation;

	public SortOperation(Search<E> search, com.threewks.thundr.search.google.SortOperation<E> sortOperation) {
		super();
		this.search = search;
		this.sortOperation = sortOperation;
	}

	public Search<E> ascending() {
		sortOperation.ascending();
		return search;
	}

	public Search<E> descending() {
		sortOperation.descending();
		return search;
	}

	@Override
	public String toString() {
		return sortOperation.toString();
	}
}
