package com.sirma.itt.seip.instance.archive.properties;

import static com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity.QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity;

/**
 * Unit test for {@link ArchivedPropertiesDao}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class ArchivedPropertiesDaoTest {

	@InjectMocks
	private ArchivedPropertiesDao propertiesDao;

	@Mock
	private DbDao dbDao;

	@Mock
	private ArchivedPropertiesConverter archivedPropertiesConverter;

	@Test
	public void save() {
		propertiesDao.persist(new EmfInstance("emf:version-v1.0"));
		verify(archivedPropertiesConverter).toPersistent(any());
		verify(dbDao).saveOrUpdate(any(ArchivedJsonPropertiesEntity.class));
	}

	@Test
	public void loadSingle_noPropertiesFound() {
		EmfInstance version = new EmfInstance("emf:version-v1.3");
		propertiesDao.load(version);
		assertTrue(version.getOrCreateProperties().isEmpty());
		verify(dbDao).find(eq(ArchivedJsonPropertiesEntity.class), any());
		verifyZeroInteractions(archivedPropertiesConverter);
	}

	@Test
	public void loadSingle_propertiesFound() {
		EmfInstance version = new EmfInstance("emf:version-v1.4");
		Map<String, Serializable> properties = Collections.singletonMap("title", "Darkness");
		when(dbDao.find(eq(ArchivedJsonPropertiesEntity.class), any()))
				.thenReturn(new ArchivedJsonPropertiesEntity(version.getId().toString(), properties));
		when(archivedPropertiesConverter.toInstanceProperties(anyMap())).thenReturn(properties);

		propertiesDao.load(version);

		assertFalse(version.getProperties().isEmpty());
		verify(dbDao).find(eq(ArchivedJsonPropertiesEntity.class), any());
	}

	@Test
	public void batchLoad_noPropertiesFound() {
		EmfInstance version1 = new EmfInstance("emf:version-v1.2");
		EmfInstance version2 = new EmfInstance("emf:version-v1.5");

		propertiesDao.load(Arrays.asList(version1, version2));

		assertTrue(version1.getOrCreateProperties().isEmpty());
		assertTrue(version2.getOrCreateProperties().isEmpty());
		verify(dbDao).fetchWithNamed(eq(QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY), anyList());
		verifyZeroInteractions(archivedPropertiesConverter);
	}

	@Test
	public void batchLoad_propertiesFound() {
		EmfInstance version1 = new EmfInstance("emf:version-v1.2");
		EmfInstance version2 = new EmfInstance("emf:version-v1.5");
		Map<String, Serializable> properties = Collections.singletonMap("title", "Darkness");
		when(dbDao.fetchWithNamed(eq(QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY), anyList()))
				.thenReturn(Arrays.asList(new ArchivedJsonPropertiesEntity(version1.getId().toString(), properties),
						new ArchivedJsonPropertiesEntity(version2.getId().toString(), properties)));
		when(archivedPropertiesConverter.toInstanceProperties(anyMap())).thenReturn(properties);

		propertiesDao.load(Arrays.asList(version1, version2));

		assertFalse(version1.getProperties().isEmpty());
		assertFalse(version2.getProperties().isEmpty());
		verify(dbDao).fetchWithNamed(eq(QUERY_BATCH_ARCHIVED_PROPERTY_VALUE_BY_VERSION_IDS_KEY), anyList());
	}

	@Test
	public void delete() {
		propertiesDao.delete("emf:version-v1.6");
		verify(dbDao).delete(eq(ArchivedJsonPropertiesEntity.class), eq("emf:version-v1.6"));
	}
}