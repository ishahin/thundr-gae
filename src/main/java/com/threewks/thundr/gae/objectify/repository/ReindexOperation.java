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

/**
 * Allows the re-index operation to modify the entities provided before they are re-indexed.
 * 
 * Note that all entities that are returned from apply will be saved and re-indexed.
 * This means that the re-index operation can also be use to filter out articles not matching
 * an adhoc criteria.
 */
public interface ReindexOperation<E extends RepositoryEntity> {
	/**
	 * @param batch the batch of articles to be updated.
	 * @return a list of the articles which are to be updated - re-indexing will not occur on articles not in the result list
	 */
	List<E> apply(List<E> batch);
}
