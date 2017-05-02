package com.sirma.itt.seip.instance.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * Test for {@link SemanticDbIdLoadCallback}.
 *
 * @author BBonev
 */
public class SemanticDbIdLoadCallbackTest {

	private SemanticDbIdLoadCallback callback;

	private DbDao dbDao;
	private EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache;

	@Before
	public void beforeMethod() {
		dbDao = mock(DbDao.class);
		cache = mock(EntityLookupCache.class);
		callback = new SemanticDbIdLoadCallback(EmfInstance.class, dbDao);
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
}
