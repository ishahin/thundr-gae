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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import jodd.bean.BeanUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.collection.Pair;
import com.atomicleopard.expressive.collection.Triplets;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.profiler.ProfilableFuture;
import com.threewks.thundr.profiler.Profiler;

public class GoogleSearchService implements SearchService {
	private com.google.appengine.api.search.SearchService searchService = SearchServiceFactory.getSearchService();
	private Profiler profiler;

	public GoogleSearchService() {
	}

	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	@Override
	public <T> IndexOperation index(T object, String id, Iterable<String> fields) {
		Class<T> as = getType(object);
		Index index = getIndex(as);
		Map<String, Object> map = extractSearchableFields(object, fields);
		Document document = buildDocument(as, id, map);
		Future<PutResponse> putAsync = index.putAsync(document);
		return new IndexOperation(putAsync);
	}

	@Override
	public <T> IndexOperation index(Map<String, T> objects, Iterable<String> fields) {
		Future<PutResponse> future = null;
		if (!objects.isEmpty()) {
			String first = objects.keySet().iterator().next();
			T t = objects.get(first);
			Class<T> as = getType(t);
			Index index = getIndex(as);

			List<Document> documents = new ArrayList<Document>(objects.size());
			for (Map.Entry<String, T> entry : objects.entrySet()) {
				String id = entry.getKey();
				T object = entry.getValue();
				Map<String, Object> fieldValues = extractSearchableFields(object, fields);
				Document document = buildDocument(as, id, fieldValues);
				documents.add(document);
			}
			future = index.putAsync(documents);
		}

		return new IndexOperation(future);
	}

	@Override
	public <T> IndexOperation remove(Class<T> as, Iterable<String> ids) {
		Index index = getIndex(as);
		Future<Void> deleteAsync = index.deleteAsync(ids);
		return new IndexOperation(deleteAsync);
	}

	@Override
	public <T> int removeAll(Class<T> as) {
		int count = 0;
		Index index = getIndex(as);
		GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).setLimit(200).build();
		GetResponse<Document> response = index.getRange(request);

		// can only delete documents in blocks of 200 so we need to iterate until they're all gone
		while (!response.getResults().isEmpty()) {
			List<String> ids = new ArrayList<String>();
			for (Document document : response) {
				ids.add(document.getId());
			}
			index.delete(ids);
			count += ids.size();
			response = index.getRange(request);
		}
		return count;
	}

	@Override
	public <T> SearchRequest<T> search(Class<T> type) {
		return new SearchRequest<T>(this, type);
	}

	protected <T> SearchResult<T> createSearchResult(SearchRequest<T> searchRequest, Class<T> type) {
		String queryString = StringUtils.join(searchRequest.query(), " ");

		Index index = getIndex(type);
		SortOptions.Builder sortOptions = SortOptions.newBuilder();
		for (Sort sort : searchRequest.sort()) {
			SortExpression.Builder expression = SortExpression.newBuilder().setExpression(sort.getField());
			expression = expression.setDirection(sort.isDescending() ? SortExpression.SortDirection.DESCENDING : SortExpression.SortDirection.ASCENDING);
			if (isNumericField(type, sort.getField())) {
				expression = expression.setDefaultValueNumeric(0);
			} else {
				expression = expression.setDefaultValue("");
			}
			sortOptions = sortOptions.addSortExpression(expression);
		}

		QueryOptions.Builder queryOptions = QueryOptions.newBuilder();
		queryOptions.setSortOptions(sortOptions);
		Integer limit = searchRequest.limit();
		int offset = 0;
		if (limit != null) {
			offset = searchRequest.offset() == null ? 0 : searchRequest.offset();
			int effectiveLimit = limit + offset;
			if (effectiveLimit > 1000) {
				Logger.warn("Currently the Google Search API does not support queries with a limit over 1000. With an offset of %d and a limit of %d, you have an effective limit of %d", offset,
						limit, effectiveLimit);
			}
			limit = effectiveLimit;
			/* Note, this can't be more than 1000 (Crashes) */
			queryOptions = queryOptions.setLimit(limit); 
		}
		Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);
		Future<Results<ScoredDocument>> searchAsync = index.searchAsync(query);
		if (profiler != null) {
			searchAsync = new ProfilableFuture<Results<ScoredDocument>>(Profiler.CategorySearch, queryString, profiler, searchAsync);
		}
		Logger.debug("Text search on %s: %s", index.getName(), queryString);
		return new SearchResult<T>(type, searchAsync, searchRequest.offset());
	}

	private Index getIndex(Class<?> type) {
		String indexName = type.getName().replaceAll("\\.", "-");
		return searchService.getIndex(IndexSpec.newBuilder().setName(indexName));
	}

	private BigDecimal getAsNumber(Object value) {
		if (value instanceof Number) {
			return new BigDecimal(((Number) value).doubleValue());
		}
		return null;
	}

	private String getAsCollection(Object value) {
		Collection<?> collection = Cast.as(value, Collection.class);
		if (collection != null) {
			return StringUtils.join(collection.toArray(), " ");
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private Class getPropertyType(Class type, String field) {
		String getterName = "get" + StringUtils.capitalize(field);
		Class<?> fieldType = null;
		try {
			@SuppressWarnings("unchecked")
			Method method = type.getMethod(getterName);
			fieldType = method.getReturnType();
		} catch (Exception e) {
			// swallow, try a field
		}
		if (fieldType == null) {
			try {
				java.lang.reflect.Field javaField = type.getDeclaredField(field);
				fieldType = javaField.getType();
			} catch (Exception e) {
				// swallow
			}
		}
		return fieldType;
	}

	@SuppressWarnings("rawtypes")
	private Class getNestedPropertyType(Class clazz, String property) {
		if (property.contains("_")) {
			String[] fields = StringUtils.split(property, '_');
			for (String field : fields) {
				clazz = getPropertyType(clazz, field);
			}
		} else {
			clazz = getPropertyType(clazz, property);
		}
		return clazz;
	}

	private Triplets<Class<?>, String, Boolean> isFieldNumericLookup = new Triplets<Class<?>, String, Boolean>(new ConcurrentHashMap<Pair<Class<?>, String>, Boolean>());

	/**
	 * We need to know when doing a search if a field has a numeric representation or not - this method will tell us based on
	 * the current bean definition (that is, this is probably weak based on type migration.
	 * Results are cached to reduce reflection overheads
	 */
	boolean isNumericField(Class<?> type, String field) {
		Boolean cached = isFieldNumericLookup.get(type, field);
		if (cached != null) {
			return cached;
		}
		@SuppressWarnings("rawtypes")
		Class fieldType = getNestedPropertyType(type, field);

		boolean isNumeric = fieldType == null ? false : int.class.equals(fieldType) || long.class.equals(fieldType) || float.class.equals(fieldType) || double.class.equals(fieldType)
				|| Number.class.isAssignableFrom(fieldType);
		isFieldNumericLookup.put(type, field, isNumeric);
		return isNumeric;
	}

	// TODO - Search Service abstraction - type mapping and conversion between input data and what is supported in
	// the search API is a wider problem. A full type conversion strategy would be useful here.
	private Date getAsDate(Object value) {
		if (value instanceof Date) {
			return (Date) value;
		}
		if (value instanceof DateTime) {
			return ((DateTime) value).toDate();
		}
		return null;
	}

	private <T> Map<String, Object> extractSearchableFields(T object, Iterable<String> fields) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (String field : fields) {
			Object value = BeanUtil.getDeclaredPropertySilently(object, field);
			map.put(field, value);
		}
		return map;
	}

	private <T> Document buildDocument(Class<T> as, String id, Map<String, Object> fields) {
		Builder documentBuilder = Document.newBuilder();
		documentBuilder.setId(id);
		for (Map.Entry<String, Object> fieldData : fields.entrySet()) {
			Object value = fieldData.getValue();
			String fieldName = fieldData.getKey();
			if (value != null) {
				try {
					Field field = buildField(fieldName, value);
					documentBuilder.addField(field);
				} catch (Exception e) {
					throw new SearchException(e, "Failed to index type %s with id %s for field %s with a value of %s: %s", as.getSimpleName(), id, fieldName, value.toString(), e.getMessage());
				}
			}
		}

		return documentBuilder.build();
	}

	private Field buildField(String field, Object value) {
		field = SearchRequest.encodeFieldName(field);
		com.google.appengine.api.search.Field.Builder fieldBuilder = Field.newBuilder().setName(field);

		String stringVal = Cast.as(value, String.class);
		GeoPoint geoPointVal = stringVal != null ? null : Cast.as(value, GeoPoint.class);
		Date dateVal = geoPointVal != null ? null : getAsDate(value);
		BigDecimal numberVal = dateVal != null ? null : getAsNumber(value);
		String collectionVal = numberVal != null ? null : getAsCollection(value);

		if (dateVal != null) {
			fieldBuilder.setDate(dateVal);
		} else if (numberVal != null) {
			fieldBuilder.setNumber(numberVal.doubleValue());
		} else if (collectionVal != null) {
			fieldBuilder.setText(collectionVal);
		} else if (geoPointVal != null) {
			fieldBuilder.setGeoPoint(geoPointVal);
		} else {
			fieldBuilder.setText(value.toString());
		}
		return fieldBuilder.build();
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getType(T t) {
		return (Class<T>) t.getClass();
	}
}
