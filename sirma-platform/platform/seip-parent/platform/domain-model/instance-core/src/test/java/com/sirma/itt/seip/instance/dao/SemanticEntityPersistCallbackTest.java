package com.sirma.itt.seip.instance.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypes;

/**
 * Test for {@link SemanticEntityPersistCallback}.
 *
 * @author A. Kunchev
 */
public class SemanticEntityPersistCallbackTest {

	private SemanticEntityPersistCallback callback;

	@Mock
	private DbDao dbDao;

	@Mock
	private EntityLookupCacheContext cacheContext;

	@Mock
	private CopyInstanceConverter instanceConverter;

	@Mock
	private InstanceTypes instanceTypes;

	@Mock
	private EntityLookupCache<Serializable, Object, Serializable> cache;

	@Mock
	private EntityLookupCache<Serializable, Object, Serializable> temporaryCache;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(cacheContext.getCache("SEMANTIC_INSTANCE_ENTITY_CACHE")).thenReturn(cache);
		when(cacheContext.getCache("INSTANCE_TEMPORARY_CACHE")).thenReturn(temporaryCache);

		callback = new SemanticEntityPersistCallback(dbDao, cacheContext, instanceConverter, instanceTypes);
	}

	@Test
	public void persistAndUpdateCache_updateBothCaches_returnsOldReference() {
		final String OLD_REFERENCE_INSTANCE_ID = "old-instance-id";
		final String NEW_REFERENCE_INSTANCE_ID = "new-instance-id";

		Instance oldReference = new EmfInstance(OLD_REFERENCE_INSTANCE_ID);
		when(instanceConverter.convertToInstance(any())).thenReturn(oldReference);
		when(cache.getByKey(anyString()))
				.thenReturn(new Pair<>(OLD_REFERENCE_INSTANCE_ID, oldReference));

		Instance instance = new EmfInstance(NEW_REFERENCE_INSTANCE_ID);
		when(dbDao.saveOrUpdate(instance, oldReference)).thenReturn(instance);

		Instance result = callback.persistAndUpdateCache(instance);

		verify(dbDao).saveOrUpdate(instance, oldReference);
		verify(cache).setValue(NEW_REFERENCE_INSTANCE_ID, instance);
		verify(temporaryCache).setValue(NEW_REFERENCE_INSTANCE_ID, instance);
		assertEquals(OLD_REFERENCE_INSTANCE_ID, result.getId());
	}
}
