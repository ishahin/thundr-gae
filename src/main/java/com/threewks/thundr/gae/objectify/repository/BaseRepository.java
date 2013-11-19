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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.search.google.IndexOperation;
import com.threewks.thundr.search.google.SearchResult;
import com.threewks.thundr.search.google.SearchService;

public class BaseRepository<E extends RepositoryEntity> implements Repository<E> {
	protected CollectionTransformer<E, Long> toIds;
	protected ETransformer<E, Long> toId;
	protected ETransformer<Collection<E>, Map<String, E>> stringIdLookup;
	protected ETransformer<Collection<E>, Map<Long, E>> idLookup;
	protected SearchService searchService;
	protected Class<E> entityType;
	protected List<String> fieldsToIndex;

	public BaseRepository(Class<E> entityType, List<String> searchableFields, SearchService searchService) {
		this.searchService = searchService;
		this.entityType = entityType;
		this.fieldsToIndex = searchableFields;
		this.toId = Expressive.Transformers.toProperty("id", entityType);
		this.toIds = Expressive.Transformers.transformAllUsing(toId);
		this.idLookup = Expressive.Transformers.toKeyBeanLookup("id", entityType);
		this.stringIdLookup = new ETransformer<Collection<E>, Map<String, E>>() {
			@Override
			public Map<String, E> from(Collection<E> from) {
				Map<Long, E> lookup = idLookup.from(from);
				Map<String, E> results = new LinkedHashMap<>(lookup.size());
				for (Map.Entry<Long, E> entry : lookup.entrySet()) {
					String id = Transformers.IdToString.from(entry.getKey());
					results.put(id, entry.getValue());
				}
				return results;
			}
		};
	}

	public List<String> getFieldsToIndex() {
		return fieldsToIndex;
	}

	@Override
	public AsyncResult<E> save(final E entity) {
		Long initialId = entity.getId();
		final Result<Key<E>> ofyFuture = ofy().save().entity(entity);
		if (initialId == null) {
			// if no id exists - we need objectify to complete so that the id can be used in indexing the record.
			ofyFuture.now();
		}
		final IndexOperation searchFuture = searchService.index(entity, String.valueOf(entity.getId()), getFieldsToIndex());
		return new AsyncResult<E>() {
			@Override
			public E complete() {
				ofyFuture.now();
				searchFuture.complete();
				return entity;
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public AsyncResult<List<E>> save(E... entities) {
		return save(Arrays.asList(entities));
	}

	@Override
	public AsyncResult<List<E>> save(final List<E> entities) {
		List<Long> ids = toIds.from(entities);
		final Result<Map<Key<E>, E>> ofyFuture = ofy().save().entities(entities);
		if (ids.contains(null)) {
			ofyFuture.now(); // force sync save
		}
		Map<String, E> entityLookup = stringIdLookup.from(entities);
		final IndexOperation searchFuture = searchService.index(entityLookup, getFieldsToIndex());
		return new AsyncResult<List<E>>() {
			@Override
			public List<E> complete() {
				ofyFuture.now();
				searchFuture.complete();
				return entities;
			}
		};
	}

	@Override
	public E load(Long id) {
		return ofy().load().type(entityType).id(id).now();
	}

	@Override
	public List<E> load(List<Long> ids) {
		Map<Long, E> results = ofy().load().type(entityType).ids(ids);
		return Expressive.Transformers.transformAllUsing(Expressive.Transformers.usingLookup(results)).from(ids);
	}

	@Override
	public List<E> load(Long... ids) {
		Map<Long, E> results = ofy().load().type(entityType).ids(ids);
		return Expressive.Transformers.transformAllUsing(Expressive.Transformers.usingLookup(results)).from(ids);
	}

	@Override
	public List<E> list(int count) {
		return ofy().load().type(entityType).limit(count).list();
	}

	@Override
	public List<E> loadByField(String field, String value) {
		return ofy().load().type(entityType).filter(field, value).list();
	}

	@Override
	public List<E> loadByField(String field, List<String> values) {
		return ofy().load().type(entityType).filter(field + " in", values).list();
	}

	@Override
	public Search<E> search() {
		return new Search<>(this, searchService.search(entityType));
	}

	@Override
	public List<E> completeSearch(Search<E> search) {
		List<Long> articleIds = completeIdSearch(search);
		return load(articleIds);
	}

	@Override
	public List<Long> completeIdSearch(Search<E> search) {
		SearchResult<E> results = search.searchRequest.search();
		EList<Long> articleIds = Transformers.IdsFromStrings.from(results.getSearchResultIds());
		articleIds = articleIds.removeItems((Long) null);
		return articleIds;
	}

	@Override
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

	@Override
	public AsyncResult<Void> delete(E e) {
		return delete(e.getId());
	}

	@Override
	public AsyncResult<Void> delete(Long... ids) {
		return delete(Arrays.asList(ids));
	}

	@Override
	public AsyncResult<Void> delete(List<Long> ids) {
		List<String> stringIds = Transformers.IdsToStrings.from(ids);
		final Result<Void> ofyDelete = ofy().delete().type(entityType).ids(ids);
		final IndexOperation searchDelete = searchService.remove(entityType, stringIds);
		return new AsyncResult<Void>() {
			@Override
			public Void complete() {
				ofyDelete.now();
				searchDelete.complete();
				return null;
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public AsyncResult<Void> delete(E... entities) {
		List<Long> ids = toIds.from(entities);
		return delete(ids);
	}

	/**
	 * Reindexes all the entities matching the given search operation. The given {@link ReindexOperation}, if present will be applied to each batch of entities.
	 * 
	 * @param search
	 * @param batchSize
	 * @param reindexOperation
	 * @return the overall count of re-indexed entities.
	 */
	@Override
	public int reindex(Search<E> search, int batchSize, ReindexOperation<E> reindexOperation) {
		List<String> fieldsToIndex = getFieldsToIndex();
		int count = 0;
		List<E> results = completeSearch(search);
		List<List<E>> batches = Lists.partition(results, batchSize);
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
}
