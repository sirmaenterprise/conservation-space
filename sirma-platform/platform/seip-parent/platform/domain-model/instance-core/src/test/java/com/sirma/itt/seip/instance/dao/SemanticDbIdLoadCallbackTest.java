package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link SemanticDbIdLoadCallback}.
 *
 * @author BBonev
 */
public class SemanticDbIdLoadCallbackTest {

	private SemanticDbIdLoadCallback callback;

	@Mock
	private DbDao dbDao;

	@Mock
	private EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache;

	@Mock
	private EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> temporaryCache;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		callback = new SemanticDbIdLoadCallback(EmfInstance.class, dbDao, () -> temporaryCache);
	}

	@Test
	public void getId() {
		assertEquals("id", callback.getId(new EmfInstance("id")));
	}

	@Test
	public void loadingFromCache() throws Exception {
		when(cache.getByKey("emf:instance")).thenReturn(new Pair<>("emf:instance", new EmfInstance()));
		Entity<? extends Serializable> entity = callback.lookupById("emf:instance", cache);
		assertNotNull(entity);
	}

	@Test
	public void loadDeletedInstance() throws Exception {
		when(cache.getByKey("emf:instance")).thenReturn(null);
		when(dbDao.find(EmfInstance.class, "emf:instance")).thenReturn(new EmfInstance());

		Options.ALLOW_LOADING_OF_DELETED_INSTANCES.enable();
		try {
			Entity<? extends Serializable> entity = callback.lookupById("emf:instance", cache);
			assertNotNull(entity);
		} finally {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.disable();
		}
	}

	@Test
	public void loadDeletedInstance_notAllowed() throws Exception {
		when(cache.getByKey("emf:instance")).thenReturn(null);

		Entity<? extends Serializable> entity = callback.lookupById("emf:instance", cache);
		assertNull(entity);
		verify(dbDao, never()).find(any(), any());
	}

	@Test
	public void loadSavedInstance_justCreatedNoPreviousDataRetrieved_hasTempCacheValue() {
		final String INSTANCE_ID = "instance-id";
		Instance cached = new EmfInstance(INSTANCE_ID);
		when(temporaryCache.getValue(INSTANCE_ID)).thenReturn(Entity.class.cast(cached));
		when(dbDao.find(any(), eq(INSTANCE_ID))).thenReturn(null);

		assertNotNull(callback.fetchByKey(INSTANCE_ID));
		verify(temporaryCache).getValue(INSTANCE_ID);
	}

	@Test
	public void loadSavedInstance_justCreatedNoPreviousDataRetrieved_noTempCacheValue() {
		final String INSTANCE_ID = "instance-id";
		when(dbDao.find(any(), eq(INSTANCE_ID))).thenReturn(null);

		assertNull(callback.fetchByKey(INSTANCE_ID));
		verify(temporaryCache).getValue(INSTANCE_ID);
	}

	@Test
	public void loadSavedInstance_staleDBData_instanceRetrivedFromTempCache() {
		final String INSTANCE_ID = "instance-id";
		Instance cached = new EmfInstance(INSTANCE_ID);
		Calendar calendar = Calendar.getInstance();
		Date modifiedOn = calendar.getTime();
		cached.add(MODIFIED_ON, modifiedOn);
		when(temporaryCache.getValue(INSTANCE_ID)).thenReturn(Entity.class.cast(cached));

		calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) - 1);
		Instance loaded = new EmfInstance(INSTANCE_ID);
		loaded.add(MODIFIED_ON, calendar.getTime());

		when(dbDao.find(any(), eq(INSTANCE_ID))).thenReturn(loaded);
		Instance result = (Instance) callback.fetchByKey(INSTANCE_ID);
		assertNotNull(result);
		assertEquals(modifiedOn, result.get(MODIFIED_ON, Date.class));
	}

	@Test
	public void loadSavedInstance_correctDBData() {
		final String INSTANCE_ID = "instance-id";
		Instance cached = new EmfInstance(INSTANCE_ID);
		Date modifiedOn = Calendar.getInstance().getTime();
		cached.add(MODIFIED_ON, modifiedOn);
		when(temporaryCache.containsKey(INSTANCE_ID)).thenReturn(Boolean.TRUE);
		when(temporaryCache.getValue(INSTANCE_ID)).thenReturn(Entity.class.cast(cached));

		Instance loaded = new EmfInstance(INSTANCE_ID);
		loaded.add(MODIFIED_ON, modifiedOn);

		when(dbDao.find(any(), eq(INSTANCE_ID))).thenReturn(loaded);
		Instance result = (Instance) callback.fetchByKey(INSTANCE_ID);
		assertNotNull(result);
		assertEquals(modifiedOn, result.get(MODIFIED_ON, Date.class));
	}
}
