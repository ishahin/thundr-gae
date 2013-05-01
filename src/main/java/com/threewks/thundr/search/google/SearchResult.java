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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jodd.bean.BeanUtil;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Field.FieldType;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;

public class SearchResult<T> {
	private Future<Results<ScoredDocument>> searchAsync;
	private Results<ScoredDocument> results;
	private Class<T> type;
	private Integer offset;
	private List<ScoredDocument> resultsList;

	public SearchResult(Class<T> type, Future<Results<ScoredDocument>> searchAsync, Integer offset) {
		this.searchAsync = searchAsync;
		this.type = type;
		this.offset = offset;
	}

	public EList<T> getSearchResults() throws SearchException {
		List<ScoredDocument> results = resultsList();
		EList<Map<String, Object>> data = toMap.from(results);
		EList<T> objects = toBean.from(data);
		return objects;
	}

	public EList<String> getSearchResultIds() throws SearchException {
		return toIds.from(resultsList());
	}

	public long getMatchingRecordCount() {
		Results<ScoredDocument> results = results();
		return results.getNumberFound();
	}

	public long getReturnedRecordCount() {
		Results<ScoredDocument> results = results();
		return Math.max(0, results.getNumberFound() - offset());
	}

	public String cursor() {
		return results.getCursor().toWebSafeString();
	}

	/**
	 * When applying an offset to a query in the Google Search service, you can no longer order by fields.
	 * To get around this, we apply the offset manually to the searched result set.
	 * 
	 * @return
	 */
	private List<ScoredDocument> resultsList() {
		if (this.resultsList == null) {
			Results<ScoredDocument> results = results();
			List<ScoredDocument> resultsList = new ArrayList<ScoredDocument>(results.getResults());
			int end = resultsList.size();
			int start = Math.min(offset(), end);
			this.resultsList = resultsList.subList(start, end);
		}
		return resultsList;
	}

	private Results<ScoredDocument> results() {
		if (results == null) {
			try {
				results = searchAsync.get();
			} catch (InterruptedException e) {
				throw new SearchException(e, "Failed to retrieve search results: %s", e.getMessage());
			} catch (ExecutionException e) {
				throw new SearchException(e, "Failed to retrieve search results: %s", e.getMessage());
			}
		}
		return results;
	}

	private int offset() {
		return offset == null ? 0 : offset;
	}

	private CollectionTransformer<Map<String, Object>, T> toBean = Expressive.Transformers.transformAllUsing(new ETransformer<Map<String, Object>, T>() {
		@Override
		public T from(Map<String, Object> from) {
			try {
				T instance = type.newInstance();
				for (Map.Entry<String, Object> entry : from.entrySet()) {
					BeanUtil.setPropertyForced(instance, entry.getKey(), entry.getValue());
				}
				return instance;
			} catch (Exception e) {
				throw new SearchException(e, "Failed to create a new instance of %s for search results: %s", type.getName(), e.getMessage());
			}
		}
	});
	private CollectionTransformer<ScoredDocument, Map<String, Object>> toMap = Expressive.Transformers.transformAllUsing(new ETransformer<ScoredDocument, Map<String, Object>>() {
		@Override
		public Map<String, Object> from(ScoredDocument from) {
			Map<String, Object> results = new HashMap<String, Object>();
			for (Field field : from.getFields()) {
				FieldType fieldType = field.getType();
				Object value = null;
				if (FieldType.TEXT.equals(fieldType)) {
					value = field.getText();
				} else if (FieldType.NUMBER.equals(fieldType)) {
					value = field.getNumber();
				} else if (FieldType.DATE.equals(fieldType)) {
					value = field.getDate();
				} else if (FieldType.ATOM.equals(fieldType)) {
					value = field.getAtom();
				} else if (FieldType.HTML.equals(fieldType)) {
					value = field.getHTML();
				}
				results.put(field.getName(), value);
			}
			return results;
		}
	});

	private CollectionTransformer<ScoredDocument, String> toIds = Expressive.Transformers.transformAllUsing(new ETransformer<ScoredDocument, String>() {
		@Override
		public String from(ScoredDocument from) {
			return from.getId();
		}
	});

}
