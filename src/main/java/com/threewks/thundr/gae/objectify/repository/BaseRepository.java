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

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.search.google.IndexOperation;
import com.threewks.thundr.search.google.SearchResult;
import com.threewks.thundr.search.google.SearchService;

public class BaseRepository<E extends RepositoryEntity> {
	protected ETransformer<Collection<E>, Map<String, E>> stringIdLookup;
	protected SearchService searchService;
	protected Class<E> entityType;
	protected List<String> fieldsToIndex;

	public BaseRepository(Class<E> entityType, List<String> searchableFields, SearchService searchService) {
		this.searchService = searchService;
		this.entityType = entityType;
		this.fieldsToIndex = searchableFields;
		this.stringIdLookup = Expressive.Transformers.toKeyBeanLookup("id", entityType);
	}

	public List<String> getFieldsToIndex() {
		return fieldsToIndex;
	}

	public AsyncResult<E> save(final E entity) {
		// if no id exists - we need objectify to complete so that the id can be used in indexing the record.
		if (entity.getId() != null) {
			final Result<Key<E>> ofyFuture = ofy().save().entity(entity);
			final IndexOperation searchFuture = searchService.index(entity, String.valueOf(entity.getId()), getFieldsToIndex());
			return new AsyncResult<E>() {
				@Override
				public E complete() {
					ofyFuture.now();
					searchFuture.complete();
					return entity;
				}
			};
		} else {
			ofy().save().entity(entity).now();
			final IndexOperation searchFuture = searchService.index(entity, Transformers.IdToString.from(entity.getId()), getFieldsToIndex());
			return new AsyncResult<E>() {
				@Override
				public E complete() {
					searchFuture.complete();
					return entity;
				}
			};
		}
	}

	public E load(Long id) {
		return ofy().load().type(entityType).id(id).now();
	}

	public List<E> load(List<Long> ids) {
		return new ArrayList<E>(ofy().load().type(entityType).ids(ids).values());
	}

	public List<E> load(Long... ids) {
		return new ArrayList<E>(ofy().load().type(entityType).ids(ids).values());
	}

	public List<E> list(int count) {
		return ofy().load().type(entityType).limit(count).list();
	}

	public List<E> loadByField(String field, String value) {
		return ofy().load().type(entityType).filter(field, value).list();
	}

	public List<E> loadByField(String field, List<String> values) {
		return ofy().load().type(entityType).filter(field + " in", values).list();
	}

	public Search<E> search() {
		return new Search<>(this, searchService.search(entityType));
	}

	public List<E> completeSearch(Search<E> search) {
		SearchResult<E> results = search.searchRequest.search();
		EList<Long> articleIds = Transformers.IdsFromStrings.from(results.getSearchResultIds());
		articleIds = articleIds.removeItems((Long) null);
		return load(articleIds);
	}

	public AsyncResult<Void> delete(long id) {
		String stringId = Transformers.IdToString.from(id);
		final Result<Void> ofyDelete = ofy().delete().type(entityType).id(id);
		final IndexOperation searchDelete = searchService.remove(entityType, Collections.singleton(stringId));
		return new AsyncResult<Void>() {
			@Override
			public Void complete() {
				ofyDelete.now();
				searchDelete.complete();
				return null;
			}
		};
	}

	public AsyncResult<Void> delete(E e) {
		String stringId = Transformers.IdToString.from(e.getId());
		final Result<Void> ofyDelete = ofy().delete().entity(e);
		final IndexOperation searchDelete = searchService.remove(entityType, Collections.singleton(stringId));
		return new AsyncResult<Void>() {
			@Override
			public Void complete() {
				ofyDelete.now();
				searchDelete.complete();
				return null;
			}
		};
	}

	/**
	 * Reindexes all the entities matching the given search operation. The given {@link ReindexOperation}, if present will be applied to each batch of entities.
	 * 
	 * @param search
	 * @param batchSize
	 * @param reindexOperation
	 * @return the overall count of re-indexed entities.
	 */
	public int reindex(Search<E> search, int batchSize, ReindexOperation<E> reindexOperation) {
		List<String> fieldsToIndex = getFieldsToIndex();
		int count = 0;
		List<E> results = completeSearch(search);
		List<List<E>> batches = partition(results, batchSize);
		for (List<E> batch : batches) {
			batch = reindexOperation == null ? batch : reindexOperation.apply(batch);
			Map<String, E> keyedLookup = stringIdLookup.from(batch);
			if (reindexOperation != null) {
				// we only re-save the batch when a re-index op is supplied, otherwise the data can't have changed.
				ofy().save().entities(batch).now();
			}
			searchService.index(keyedLookup, fieldsToIndex).complete();
			count += batch.size();
			Logger.info("Reindexed %d entities of type %s, %d of %d", keyedLookup.size(), entityType.getSimpleName(), count, results.size());
		}
		return count;
	}

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
					Logger.warn("Obtained an article search result with an invalid id: %s", from);
					return null;
				}
			}
		};
		public static final CollectionTransformer<String, Long> IdsFromStrings = Expressive.Transformers.transformAllUsing(IdFromString);
		public static final CollectionTransformer<Long, String> IdsToStrings = Expressive.Transformers.transformAllUsing(IdToString);

	}

	protected static <T> List<List<T>> partition(List<T> source, int size) {
		List<List<T>> batches = new ArrayList<List<T>>();
		for (int i = 0; i < source.size(); i += size) {
			int end = Math.min(source.size() - i, i + size);
			List<T> batch = source.subList(i, end);
			batches.add(batch);
		}

		return batches;
	}
}
