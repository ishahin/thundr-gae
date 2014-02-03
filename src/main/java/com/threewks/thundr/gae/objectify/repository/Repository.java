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

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.threewks.thundr.logger.Logger;

public interface Repository<E extends RepositoryEntity> {
	public AsyncResult<E> save(final E entity);

	/**
	 * Save the given entities.
	 * 
	 * @param entities
	 * @return an async result to complete the save operation
	 */
	@SuppressWarnings("unchecked")
	public AsyncResult<List<E>> save(E... entities);

	/**
	 * Save the given entities.
	 * 
	 * @param entities
	 * @return an async result to complete the save operation
	 */
	public AsyncResult<List<E>> save(final List<E> entities);

	/**
	 * Load the entity with the given id
	 * 
	 * @param id
	 * @return the entity, or null if no entity exists
	 */
	public E load(Long id);

	/**
	 * Load the entities with the given ids
	 * 
	 * @param ids
	 * @return a list containing an entry for each corresponding id, containing the entity or null if none exists
	 */
	public List<E> load(Long... ids);

	/**
	 * Load the entities with the given ids
	 * 
	 * @param ids
	 * @return a list containing an entry for each corresponding id, containing the entity or null if none exists
	 */
	public List<E> load(List<Long> ids);

	/**
	 * List up to count entities.
	 * This will load all entities into memory, so should only be used where the number of entities is constrained.
	 * 
	 * @param count
	 * @return
	 */
	public List<E> list(int count);

	/**
	 * Load all entities whose field has the value of the given object.
	 * Note that the given field must be indexed for anything to be returned.
	 * This will load all entities into memory, so should only be used where the number of entities is constrained.
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public List<E> loadByField(String field, Object value);

	/**
	 * Load all entities who field has the values of any of the given objects.
	 * Note that the given field must be indexed for anything to be returned.
	 * This will load all entities into memory, so should only be used where the number of entities is constrained.
	 * 
	 * @param field
	 * @param values
	 * @return
	 */
	public List<E> loadByField(String field, List<Object> values);

	/**
	 * @return a builder for a search operation
	 */
	public Search<E> search();

	/**
	 * Used by implementations to complete a search. You should use the fluent {@link #search()} call instead.
	 * 
	 * @param search
	 * @return
	 */
	public List<E> completeSearch(Search<E> search);

	/**
	 * Used by implementations to complete a search. You should use the fluent {@link #search()} call instead.
	 * 
	 * @param search
	 * @return
	 */
	public List<Long> completeIdSearch(Search<E> search);

	/**
	 * Delete the entity with the given id
	 * 
	 * @param id
	 * @return an async operation used to complete the delete operation
	 */
	public AsyncResult<Void> delete(long id);

	/**
	 * Delete the entities with the given ids
	 * 
	 * @param ids
	 * @return an async operation used to complete the delete operation
	 */
	public AsyncResult<Void> delete(Long... ids);

	/**
	 * Delete the entities with the given ids
	 * 
	 * @param ids
	 * @return an async operation used to complete the delete operation
	 */
	public AsyncResult<Void> delete(List<Long> ids);

	/**
	 * Delete the given entity
	 * 
	 * @param entity
	 * @return an async operation used to complete the delete operation
	 */
	public AsyncResult<Void> delete(E entity);

	/**
	 * Delete the given entities
	 * 
	 * @param ids
	 * @return an async operation used to complete the delete operation
	 */
	@SuppressWarnings("unchecked")
	public AsyncResult<Void> delete(E... entities);

	/**
	 * Reindexes all the entities matching the given search operation. The given {@link ReindexOperation}, if present will be applied to each batch of entities.
	 * 
	 * @param search
	 * @param batchSize
	 * @param reindexOperation
	 * @return the overall count of re-indexed entities.
	 */
	public int reindex(Search<E> search, int batchSize, ReindexOperation<E> reindexOperation);

	public static class Transformers {
		public static final ETransformer<Long, String> IdToString = new ETransformer<Long, String>() {
			@Override
			public String from(Long from) {
				return String.valueOf(from);
			}
		};
		public static final ETransformer<String, Long> IdFromString = new ETransformer<String, Long>() {
			@Override
			public Long from(String from) {
				try {
					return Long.valueOf(from);
				} catch (Exception e) {
					Logger.warn("Could not convert string id to a long: %s", from);
					return null;
				}
			}
		};
		public static final CollectionTransformer<String, Long> IdsFromStrings = Expressive.Transformers.transformAllUsing(IdFromString);
		public static final CollectionTransformer<Long, String> IdsToStrings = Expressive.Transformers.transformAllUsing(IdToString);

	}
}
