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

/**
 * The {@link SearchService} provides a java class based abstraction over a document search service.
 * 
 * @see GoogleSearchService
 */
public interface SearchService {

	/**
	 * Index an object.
	 * 
	 * @param object the object to index.
	 * @param id the id of the object.
	 * @param fields the fields of the object to index.
	 * @return 
	 */
	public <T> IndexOperation index(T object, String id, Iterable<String> fields);

	/**
	 * Remove objects from the index.
	 * 
	 * @param as the type of the object.
	 * @param ids the ids of the objects to remove.
	 */
	public <T> void remove(Class<T> as, Iterable<String> ids);

	/**
	 * Remove all objects of a given type from the index.
	 * 
	 * @param as the type of object to remove.
	 * @return the number of objects removed from the index.
	 */
	public <T> int removeAll(Class<T> as);

	/**
	 * Create a {@link SearchRequest} for the given type.
	 * 
	 * @param type the type of object to search for.
	 * @return a {@link SearchRequest} that can be used to search.
	 */
	public <T> SearchRequest<T> search(Class<T> type);

}
