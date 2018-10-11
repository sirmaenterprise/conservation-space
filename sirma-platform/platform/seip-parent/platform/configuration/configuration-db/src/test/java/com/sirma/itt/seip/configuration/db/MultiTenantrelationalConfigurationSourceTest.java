package com.sirma.itt.seip.configuration.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.db.DbDao;

/**
 * Test the multi tenant relational configuration retrieval.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiTenantrelationalConfigurationSourceTest {

	@Mock
	private DbDao dbDao;

	@InjectMocks
	private MultiTenantRelationalConfigurationSource configurationsSource;

	@Test
	@SuppressWarnings("unchecked")
	public void should_getConfigurationValues_fromDb() {
		List<String> values = new ArrayList<>();
		values.add(" value");

		Mockito.when(
				dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList(), Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(values);
		Assert.assertEquals("value", configurationsSource.getConfigurationValue("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getNullConfigurationValues_when_empty_fromDb() {
		Mockito.when(
				dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList(), Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(new ArrayList<>());
		Assert.assertEquals(null, configurationsSource.getConfigurationValue("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getEmptyConfigurationValues_when_illegalStateExceptionOccurs_fromDb() {
		Mockito.when(
				dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList(), Matchers.anyInt(), Matchers.anyInt()))
				.thenThrow(IllegalStateException.class);
		Assert.assertEquals(null, configurationsSource.getConfigurationValue("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getEmptyConfigurationValues_when_persistenceExceptionOccurs_fromDb() {
		Mockito.when(
				dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList(), Matchers.anyInt(), Matchers.anyInt()))
				.thenThrow(PersistenceException.class);
		Assert.assertEquals(null, configurationsSource.getConfigurationValue("key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getTenantConfigurationValues_fromDb() {
		List<String> values = new ArrayList<>();
		values.add(" value");

		Mockito.when(
				dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList(), Matchers.anyInt(), Matchers.anyInt()))
				.thenReturn(values);
		Assert.assertEquals("value", configurationsSource.getConfigurationValue("key", "tenant"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getConfigurations_fromDb() {
		List<ConfigurationEntity> entities = mockConfigurationEntities();
		Mockito.when(dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList())).thenReturn(entities);

		Assert.assertEquals("value", configurationsSource.getConfigurations().get("tenant.(tenant)key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getTenantConfigurations_fromDb() {
		List<ConfigurationEntity> entities = mockConfigurationEntities();
		Mockito.when(dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList())).thenReturn(entities);

		Assert.assertEquals("value", configurationsSource.getConfigurations("tenant").get("tenant.(tenant)key"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_getNullTenantConfigurations_when_tenantIsNotSpecified() {
		List<ConfigurationEntity> entities = mockConfigurationEntities();
		Mockito.when(dbDao.fetchWithNamed(Matchers.anyString(), Matchers.anyList())).thenReturn(entities);

		Assert.assertEquals(null, configurationsSource.getConfigurations(null).get("tenant.(tenant)key"));
	}

	private static List<ConfigurationEntity> mockConfigurationEntities() {
		List<ConfigurationEntity> entities = new ArrayList<>();
		ConfigurationEntity entity = new ConfigurationEntity();
		ConfigurationId id = new ConfigurationId();
		id.setTenantId("tenant");
		id.setKey("key");
		entity.setId(id);
		entity.setValue("value");

		entities.add(entity);
		return entities;
	}

}
