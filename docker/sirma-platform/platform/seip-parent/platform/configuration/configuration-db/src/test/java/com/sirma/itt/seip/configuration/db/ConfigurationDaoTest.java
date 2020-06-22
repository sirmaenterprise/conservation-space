package com.sirma.itt.seip.configuration.db;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;

/**
 * Tests for {@link ConfigurationDao}
 *
 * @author BBonev
 */
public class ConfigurationDaoTest {

	@InjectMocks
	private ConfigurationDao configurationDao;

	@Mock
	private DbDao dbDao;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#getAllEntities()}.
	 */
	@Test
	public void testGetAllEntities() {
		configurationDao.getAllEntities();
		verify(dbDao).fetchWithNamed(eq(ConfigurationEntity.QUERY_ALL_CONFIG_KEY), anyList());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#getSystemConfigurations()}.
	 */
	@Test
	public void testGetSystemConfigurations() {
		configurationDao.getSystemConfigurations();
		verify(dbDao).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_TENANT_KEY), anyList());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#getConfigurationsByTenant(java.lang.String)}.
	 */
	@Test
	public void testGetConfigurationsByTenant() {
		configurationDao.getConfigurationsByTenant("tenantId");
		verify(dbDao).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_TENANT_KEY), anyList());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#getSystemConfigurations(java.util.Collection)}.
	 */
	@Test
	public void testGetSystemConfigurationsCollectionOfString() {
		assertNotNull(configurationDao.getSystemConfigurations(Collections.emptyList()));
		verify(dbDao, never()).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY), anyList());

		configurationDao.getSystemConfigurations(Arrays.asList("test"));
		verify(dbDao).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY), anyList());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#getTenantConfigurations(java.util.Collection, java.lang.String)}.
	 */
	@Test
	public void testGetTenantConfigurations() {
		assertNotNull(configurationDao.getTenantConfigurations(Collections.emptyList(), "tenantId"));
		verify(dbDao, never()).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY), anyList());

		configurationDao.getTenantConfigurations(Arrays.asList("test"), "tenantId");
		verify(dbDao).fetchWithNamed(eq(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY), anyList());
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#persist(com.sirma.itt.seip.configuration.db.ConfigurationEntity)}.
	 */
	@Test
	public void testPersist() {
		configurationDao.persist(new ConfigurationEntity());
		verify(dbDao).saveOrUpdate(any(ConfigurationEntity.class));
	}

	/**
	 * Test method for {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#deleteConfiguration(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testDeleteConfiguration() {
		configurationDao.deleteConfiguration("configKey", "tenantId");
		verify(dbDao).delete(eq(ConfigurationEntity.class), any(ConfigurationId.class));
	}
	
	/**
	 * Test method for
	 * {@link com.sirma.itt.seip.configuration.db.ConfigurationDao#deleteTenantConfigurations(java.lang.String)}.
	 */
	@Test
	public void should_deleteAllConfigurations() {
		configurationDao.deleteTenantConfigurations("tenantId");
		verify(dbDao).executeUpdate(eq(ConfigurationEntity.DELETE_ALL_CONFIGS_FOR_TENANT_KEY), anyList());
	}

}
