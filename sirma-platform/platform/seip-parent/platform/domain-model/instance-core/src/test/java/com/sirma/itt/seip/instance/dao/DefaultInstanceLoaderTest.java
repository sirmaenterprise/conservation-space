package com.sirma.itt.seip.instance.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The Class DefaultInstanceLoaderTest.
 *
 * @author BBonev
 */
@Test
public class DefaultInstanceLoaderTest {

	@Mock
	InstancePersistCallback persistCallback;
	@Mock
	InstanceConverter instanceConverter;
	@Mock
	EntityConverter entityConverter;
	@Mock
	InstanceLoadCallback primaryIdLoadCallback;
	@Mock
	InstanceLoadCallback secondaryIdLoadCallback;
	@Mock
	DbDao dbDao;
	DefaultInstanceLoader loader;
	@SuppressWarnings("rawtypes")
	@Mock
	private EntityLookupCache cache;

	/**
	 * Initialize.
	 */
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void initialize() {
		MockitoAnnotations.initMocks(this);

		when(persistCallback.getDataSource()).thenReturn(dbDao);
		when(persistCallback.getEntityConverter()).thenReturn(entityConverter);
		when(persistCallback.getInstanceConverter()).thenReturn(instanceConverter);
		when(persistCallback.getPrimaryIdLoadHandler()).thenReturn(primaryIdLoadCallback);
		when(persistCallback.getSecondaryIdLoadHandler()).thenReturn(secondaryIdLoadCallback);
		when(persistCallback.getCache()).thenReturn(cache);

		when(instanceConverter.convertToInstance(any(Entity.class))).then(invocation -> (Instance) invocation.getArguments()[0]);

		when(primaryIdLoadCallback.getId(any(Entity.class))).then(invocation -> ((Entity<Serializable>) invocation.getArguments()[0]).getId());

		when(secondaryIdLoadCallback.getId(any(Entity.class))).then(invocation -> ((DmsAware) invocation.getArguments()[0]).getDmsId());
		loader = new DefaultInstanceLoader(persistCallback);
	}

	/**
	 * Test primary id loading_null.
	 */
	@SuppressWarnings("unchecked")
	public void testPrimaryIdLoading_null() {
		assertNull(loader.find(null));
		verify(instanceConverter, never()).convertToInstance(any(Entity.class));
		verify(persistCallback, never()).onInstanceConverted(any(Instance.class), any(Instance.class));
	}

	/**
	 * Test primary id loading_nothing found.
	 */
	@SuppressWarnings("unchecked")
	public void testPrimaryIdLoading_nothingFound() {
		assertNull(loader.find("notFoundId"));
		verify(instanceConverter, never()).convertToInstance(any(Entity.class));
		verify(persistCallback, never()).onInstanceConverted(any(Instance.class), any(Instance.class));
	}

	/**
	 * Test secondary id loading_null.
	 */
	@SuppressWarnings("unchecked")
	public void testSecondaryIdLoading_null() {
		assertNull(loader.findBySecondaryId(null));
		verify(instanceConverter, never()).convertToInstance(any(Entity.class));
		verify(persistCallback, never()).onInstanceConverted(any(Instance.class), any(Instance.class));
	}

	/**
	 * Test secondary id loading_nothing found.
	 */
	@SuppressWarnings("unchecked")
	public void testSecondaryIdLoading_nothingFound() {
		assertNull(loader.findBySecondaryId("notFoundId"));
		verify(instanceConverter, never()).convertToInstance(any(Entity.class));
		verify(persistCallback, never()).onInstanceConverted(any(Instance.class), any(Instance.class));
	}

	/**
	 * Test batch primary_null.
	 */
	public void testBatchPrimary_null() {
		Collection<Instance> collection = loader.load(null);
		assertNotNull(collection);
		assertTrue(collection.isEmpty());
		verify(persistCallback, never()).onBatchConvertedInstances(anyListOf(Instance.class));
	}

	/**
	 * Test batch primary not found.
	 */
	public void testBatchPrimaryNotFound() {
		Collection<Instance> collection = loader.load(Arrays.asList("notFoundId"));
		assertNotNull(collection);
		assertTrue(collection.isEmpty());
		verify(persistCallback, never()).onBatchConvertedInstances(anyListOf(Instance.class));
	}

	/**
	 * Test batch secondary_null.
	 */
	public void testBatchSecondary_null() {
		Collection<Instance> collection = loader.loadBySecondaryId(null);
		assertNotNull(collection);
		assertTrue(collection.isEmpty());
		verify(persistCallback, never()).onBatchConvertedInstances(anyListOf(Instance.class));
	}

	/**
	 * Test batch secondary not found.
	 */
	public void testBatchSecondaryNotFound() {
		Collection<Instance> collection = loader.loadBySecondaryId(Arrays.asList("notFoundId"));
		assertNotNull(collection);
		assertTrue(collection.isEmpty());
		verify(persistCallback, never()).onBatchConvertedInstances(anyListOf(Instance.class));
	}

	/**
	 * Test find by primary id_found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFindByPrimaryId_found() {
		EmfInstance instance = new EmfInstance();
		instance.setId("foundId");
		when(primaryIdLoadCallback.lookupById("foundId", cache)).thenReturn((Entity) instance);

		assertNotNull(loader.find("foundId"));
		verify(instanceConverter).convertToInstance(instance);
		verify(persistCallback).onInstanceConverted(instance, instance);
	}

	/**
	 * Test find by secondary id_found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFindBySecondaryId_found() {
		EmfInstance instance = new EmfInstance();
		instance.setId("foundId");
		instance.setDmsId("dmsId");
		when(secondaryIdLoadCallback.lookupById("dmsId", cache)).thenReturn((Entity) instance);

		assertNotNull(loader.findBySecondaryId("dmsId"));
		verify(instanceConverter).convertToInstance(instance);
		verify(persistCallback).onInstanceConverted(instance, instance);
	}

	/**
	 * Test batch load by primary id_found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBatchLoadByPrimaryId_found() {
		EmfInstance instance = new EmfInstance();
		instance.setId("foundId");
		when(primaryIdLoadCallback.getFromCacheById("foundId", cache)).thenReturn((Entity) instance);

		Collection<Instance> collection = loader.load(Collections.singletonList("foundId"));
		assertNotNull(collection);
		assertFalse(collection.isEmpty());
		verify(instanceConverter).convertToInstance(instance);
		verify(persistCallback).onBatchConvertedInstances(Collections.singletonList(instance));
	}

	/**
	 * Test batch load by secondary id_found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBatchLoadBySecondaryId_found() {
		EmfInstance instance = new EmfInstance();
		instance.setId("foundId");
		instance.setDmsId("dmsId");
		when(secondaryIdLoadCallback.getFromCacheById("dmsId", cache)).thenReturn((Entity) instance);

		Collection<Instance> collection = loader.loadBySecondaryId(Collections.singletonList("dmsId"));
		assertNotNull(collection);
		assertFalse(collection.isEmpty());
		verify(instanceConverter).convertToInstance(instance);
		verify(persistCallback).onBatchConvertedInstances(Collections.singletonList(instance));
	}

	/**
	 * Test batch load by primary id_found_with second pass.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testBatchLoadByPrimaryId_found_withSecondPass() {
		EmfInstance instance = new EmfInstance();
		instance.setId("foundId");
		when(primaryIdLoadCallback.getFromCacheById("foundId", cache)).thenReturn((Entity) instance);
		EmfInstance notFound = new EmfInstance();
		notFound.setId("notFoundId");
		when(primaryIdLoadCallback.loadPersistedEntities(Collections.singleton("notFoundId")))
				.thenReturn(new ArrayList<Entity<? extends Serializable>>(Collections.singletonList(notFound)));

		Collection<Instance> collection = loader.load(Arrays.asList("foundId", "notFoundId"));
		assertNotNull(collection);
		assertEquals(collection.size(), 2);
		verify(instanceConverter).convertToInstance(instance);
		verify(persistCallback).onBatchConvertedInstances(Arrays.asList(instance, notFound));
		verify(primaryIdLoadCallback).loadPersistedEntities(Collections.singleton("notFoundId"));
	}

}
