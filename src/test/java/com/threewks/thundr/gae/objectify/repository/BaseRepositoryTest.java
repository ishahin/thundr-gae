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

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.gae.SetupAppengine;
import com.threewks.thundr.gae.objectify.SetupObjectify;
import com.threewks.thundr.search.google.GoogleSearchService;
import com.threewks.thundr.search.google.SearchService;

public class BaseRepositoryTest {
	@Rule public ExpectedException thrown = ExpectedException.none();
	@Rule public SetupAppengine setupAppengine = new SetupAppengine();
	@Rule public SetupObjectify setupObjectify = new SetupObjectify(TestEntity.class);

	private SearchService searchService;
	private BaseRepository<TestEntity> repository;

	@Before
	public void before() {
		searchService = new GoogleSearchService();
		repository = new BaseRepository<>(TestEntity.class, list("id", "name"), searchService);
	}

	@Test
	public void shouldAllowSaveAndLoadOfEntity() {
		TestEntity testEntity = new TestEntity("name");
		AsyncResult<TestEntity> result = repository.save(testEntity);
		assertThat(result, is(notNullValue()));
		TestEntity complete = result.complete();
		assertThat(complete, is(sameInstance(testEntity)));

		TestEntity load = repository.load(testEntity.getId());
		assertThat(load.equals(testEntity), is(true));
	}

	@Test
	public void shouldAllowSaveAndSearchOfEntity() {
		TestEntity testEntity = new TestEntity("name");
		AsyncResult<TestEntity> result = repository.save(testEntity);
		assertThat(result, is(notNullValue()));
		TestEntity complete = result.complete();
		assertThat(complete, is(sameInstance(testEntity)));

		List<TestEntity> results = repository.search().field("name").eq("name").search();
		assertThat(results, hasItem(testEntity));
	}

	@Test
	public void shouldAllowSaveAndLoadOfMultipleEntities() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		AsyncResult<List<TestEntity>> result = repository.save(testEntity, testEntity2);
		assertThat(result, is(notNullValue()));
		List<TestEntity> complete = result.complete();
		assertThat(complete.contains(testEntity), is(true));
		assertThat(complete.contains(testEntity2), is(true));

		List<TestEntity> load = repository.load(testEntity.getId(), testEntity2.getId());
		assertThat(load, hasItems(testEntity, testEntity2));
	}

	@Test
	public void shouldAllowSaveAndSearchOfMultipleEntities() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		AsyncResult<List<TestEntity>> result = repository.save(testEntity, testEntity2);
		assertThat(result, is(notNullValue()));
		List<TestEntity> complete = result.complete();
		assertThat(complete.contains(testEntity), is(true));
		assertThat(complete.contains(testEntity2), is(true));

		List<TestEntity> search = repository.search().field("name").in("name", "name2").search();
		assertThat(search, hasItems(testEntity, testEntity2));
	}

	@Test
	public void shouldListGivenCount() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		TestEntity testEntity3 = new TestEntity("name3");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		List<TestEntity> list = repository.list(2);
		assertThat(list, hasItems(testEntity, testEntity2));
		assertThat(list.size(), is(2));
	}

	@Test
	public void shouldLoadByField() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		TestEntity testEntity3 = new TestEntity("name3");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		List<TestEntity> list = repository.loadByField("name", "name2");
		assertThat(list.size(), is(1));
		assertThat(list, hasItem(testEntity2));
	}

	@Test
	public void shouldLoadByFieldReturningEmptyListWhenNoResults() {
		List<TestEntity> list = repository.loadByField("name", "none");
		assertThat(list.size(), is(0));
	}

	@Test
	public void shouldLoadByFieldCollection() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		TestEntity testEntity3 = new TestEntity("name3");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		List<TestEntity> list = repository.loadByField("name", Expressive.<Object> list("name2", "name3"));
		assertThat(list.size(), is(2));
		assertThat(list, hasItems(testEntity2, testEntity3));
	}

	@Test
	public void shouldLoadByFieldCollectionReturningEmptyListWhenNoResults() {
		List<TestEntity> list = repository.loadByField("name", Expressive.<Object> list("none"));
		assertThat(list.size(), is(0));
	}

	@Test
	public void shouldSearchAllowingOrderAndLimit() {
		TestEntity testEntity = new TestEntity(1, "name");
		TestEntity testEntity2 = new TestEntity(2, "name");
		TestEntity testEntity3 = new TestEntity(3, "name");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		List<TestEntity> results = repository.search().field("name").eq("name").order("id").ascending().limit(2).search();
		assertThat(results.size(), is(2));
		assertThat(results, hasItems(testEntity, testEntity2));
	}

	@Test
	public void shouldDeleteEntityById() {
		TestEntity testEntity = new TestEntity("name");
		repository.save(testEntity).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));
		assertThat(repository.search().field("id").eq(testEntity.getId()).search().isEmpty(), is(false));

		repository.delete(testEntity.getId()).complete();

		assertThat(repository.load(testEntity.getId()), is(nullValue()));
		assertThat(repository.search().field("id").eq(testEntity.getId()).search().isEmpty(), is(true));
	}

	@Test
	public void shouldDeleteEntityByEntity() {
		TestEntity testEntity = new TestEntity("name");
		repository.save(testEntity).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));
		assertThat(repository.search().field("id").eq(testEntity.getId()).search().isEmpty(), is(false));

		repository.delete(testEntity).complete();

		assertThat(repository.load(testEntity.getId()), is(nullValue()));
		assertThat(repository.search().field("id").eq(testEntity.getId()).search().isEmpty(), is(true));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDeleteEntitiesById() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		repository.save(testEntity, testEntity2).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));
		assertThat(repository.load(testEntity2.getId()), is(testEntity2));
		assertThat(repository.search().field("id").in(testEntity.getId(), testEntity2.getId()).search().size(), is(2));

		repository.delete(testEntity.getId(), testEntity2.getId()).complete();

		assertThat(repository.load(testEntity.getId(), testEntity2.getId()), Matchers.<TestEntity> hasItems(nullValue(), nullValue()));
		assertThat(repository.search().field("id").in(testEntity.getId(), testEntity2.getId()).search().isEmpty(), is(true));
	}

	@Test
	public void shouldDeleteEntitiesByEntity() {
		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		repository.save(testEntity, testEntity2).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));
		assertThat(repository.load(testEntity2.getId()), is(testEntity2));
		assertThat(repository.search().field("id").in(testEntity.getId(), testEntity2.getId()).search().size(), is(2));

		repository.delete(testEntity, testEntity2).complete();

		assertThat(repository.load(testEntity.getId(), testEntity2.getId()), Matchers.<TestEntity> hasItems(nullValue(), nullValue()));
		assertThat(repository.search().field("id").in(testEntity.getId(), testEntity2.getId()).search().isEmpty(), is(true));
	}

	@Test
	public void shouldAllowSaveAndLoadOfEntityWhenNotSearchIndexing() {
		repository = new BaseRepository<>(TestEntity.class, null, null);
		TestEntity testEntity = new TestEntity("name");
		AsyncResult<TestEntity> result = repository.save(testEntity);
		assertThat(result, is(notNullValue()));
		TestEntity complete = result.complete();
		assertThat(complete, is(sameInstance(testEntity)));

		TestEntity load = repository.load(testEntity.getId());
		assertThat(load.equals(testEntity), is(true));
	}

	@Test
	public void shouldAllowSaveAndLoadOfMultipleEntitiesWhenNotSearchIndexing() {
		repository = new BaseRepository<>(TestEntity.class, null, null);

		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		AsyncResult<List<TestEntity>> result = repository.save(testEntity, testEntity2);
		assertThat(result, is(notNullValue()));
		List<TestEntity> complete = result.complete();
		assertThat(complete.contains(testEntity), is(true));
		assertThat(complete.contains(testEntity2), is(true));

		List<TestEntity> load = repository.load(testEntity.getId(), testEntity2.getId());
		assertThat(load, hasItems(testEntity, testEntity2));
	}

	@Test
	public void shouldDeleteEntityByIdWhenNotSearchIndexing() {
		repository = new BaseRepository<>(TestEntity.class, null, null);

		TestEntity testEntity = new TestEntity("name");
		repository.save(testEntity).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));

		repository.delete(testEntity.getId()).complete();

		assertThat(repository.load(testEntity.getId()), is(nullValue()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldDeleteEntitiesByIdWhenNotSearchIndexing() {
		repository = new BaseRepository<>(TestEntity.class, null, null);

		TestEntity testEntity = new TestEntity("name");
		TestEntity testEntity2 = new TestEntity("name2");
		repository.save(testEntity, testEntity2).complete();

		assertThat(repository.load(testEntity.getId()), is(testEntity));
		assertThat(repository.load(testEntity2.getId()), is(testEntity2));

		repository.delete(testEntity.getId(), testEntity2.getId()).complete();

		assertThat(repository.load(testEntity.getId(), testEntity2.getId()), Matchers.<TestEntity> hasItems(nullValue(), nullValue()));
	}

	@Test
	public void shouldThrowExceptionWhenSearchWhenNotSearchIndexing() {
		thrown.expect(BaseException.class);
		thrown.expectMessage("Unable to search on type TestEntity - there are no searchable fields");

		repository = new BaseRepository<>(TestEntity.class, null, null);

		repository.search();
	}

	@Test
	public void shouldReindexEntitiesBasedOnSearch() {
		TestEntity testEntity = new TestEntity(1, "name");
		TestEntity testEntity2 = new TestEntity(2, "name");
		TestEntity testEntity3 = new TestEntity(3, "name");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		assertThat(repository.search().field("id").greaterThan(0).search(), hasItems(testEntity, testEntity2, testEntity3));

		repository = new BaseRepository<>(TestEntity.class, list("name"), searchService);
		repository.reindex(repository.search().field("name").is("name"), 10, null);

		assertThat(repository.search().field("id").greaterThan(0).search().isEmpty(), is(true));
	}

	@Test
	public void shouldReindexEntitiesAndWriteBackToDatastoreWhenReindexOperationProvided() {
		TestEntity testEntity = new TestEntity(1, "name");
		TestEntity testEntity2 = new TestEntity(2, "name");
		TestEntity testEntity3 = new TestEntity(3, "name");
		repository.save(testEntity, testEntity2, testEntity3).complete();

		assertThat(repository.search().field("id").greaterThan(0).search(), hasItems(testEntity, testEntity2, testEntity3));

		repository.reindex(repository.search().field("name").is("name"), 10, new ReindexOperation<TestEntity>() {
			@Override
			public List<TestEntity> apply(List<TestEntity> batch) {
				for (TestEntity entity : batch) {
					entity.setName("different");
				}
				return batch;
			}
		});

		assertThat(repository.search().field("name").is("different").search(), hasItems(testEntity, testEntity2, testEntity3));
		assertThat(repository.loadByField("name", "different"), hasItems(testEntity, testEntity2, testEntity3));
	}
}
