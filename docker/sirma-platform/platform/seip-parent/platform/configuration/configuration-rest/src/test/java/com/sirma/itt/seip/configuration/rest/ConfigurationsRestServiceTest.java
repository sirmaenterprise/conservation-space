package com.sirma.itt.seip.configuration.rest;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.configuration.event.ConfigurationReloadRequest;
import com.sirma.itt.seip.event.EventService;

/**
 * Tests for ConfigurationsRestService.
 *
 * @author A. Kunchev
 */
public class ConfigurationsRestServiceTest {

	@Mock
	private EventService eventService;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private ConfigurationInstance configurationInstance;

	@InjectMocks
	private ConfigurationsRestService service;

	@Before
	public void setup() {
		service = new ConfigurationsRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void reloadConfigurations() {
		Response reloadResponse = service.reloadConfigurations();
		Assert.assertEquals(Response.Status.OK.getStatusCode(), reloadResponse.getStatus());
		Mockito.verify(eventService, Mockito.times(1)).fire(Matchers.any(ConfigurationReloadRequest.class));
	}

	// ----------------------- getAllConfigurations --------------------------------

	/**
	 * Empty filter.
	 */
	@Test
	public void getAllConfigurations_emptyFilter_oneConfigurationInResponse() {
		getAllConfigurationsInternal("");
	}

	/**
	 * Null filter.
	 */
	@Test
	public void getAllConfigurations_nullFilter_oneConfigurationInResponse() {
		getAllConfigurationsInternal(null);
	}

	private void getAllConfigurationsInternal(String filter) {
		Collection<Configuration> configurations = new ArrayList<>();
		configurations.add(prepareConfiguration(false));
		when(configurationManagement.getAllConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredConfigurations = service.getAllConfigurations(filter);
		Assert.assertNotNull(filteredConfigurations);
		Assert.assertTrue(filteredConfigurations.size() == 1);
	}

	/**
	 * Some filter value.
	 */
	@Test
	public void getAllConfigurations_filteredConfigurations_filteredResult() {
		Collection<Configuration> configurations = prepareConfigurationsCollection(false);
		when(configurationManagement.getAllConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredConfigurations = service.getAllConfigurations("configKey");
		Assert.assertNotNull(filteredConfigurations);
		Assert.assertTrue(filteredConfigurations.size() == 1);
	}

	// ---------------------- getConfigurations - tenant ----------------------------

	/**
	 * Empty filter.
	 */
	@Test
	public void getConfigurations_emptyFilter_oneConfigurationInResponse() {
		getConfigurationsInternal("");
	}

	/**
	 * Null filter.
	 */
	@Test
	public void getConfigurations_nullFilter_oneConfigurationInResponse() {
		getConfigurationsInternal(null);
	}

	private void getConfigurationsInternal(String filter) {
		Collection<Configuration> configurations = new ArrayList<>();
		configurations.add(prepareConfiguration(false));
		when(configurationManagement.getCurrentTenantConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredConfigurations = service.getConfigurations(filter);
		Assert.assertNotNull(filteredConfigurations);
		Assert.assertTrue(filteredConfigurations.size() == 1);
	}

	/**
	 * Some filter value.
	 */
	@Test
	public void getConfigurations_filteredConfigurations_filteredResult() {
		Collection<Configuration> configurations = prepareConfigurationsCollection(false);
		when(configurationManagement.getCurrentTenantConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredConfigurations = service.getConfigurations("configKey");
		Assert.assertNotNull(filteredConfigurations);
		Assert.assertTrue(filteredConfigurations.size() == 1);
	}

	// ---------------------- getSystemConfigurations ------------------------------

	/**
	 * Empty filter.
	 */
	@Test
	public void getSystemConfigurations_emptyFilter_oneConfigurationInResponse() {
		getSystemConfigurationsInternal("");
	}

	/**
	 * Null filter.
	 */
	@Test
	public void getSystemConfigurations_nullFilter_oneConfigurationInResponse() {
		getSystemConfigurationsInternal(null);
	}

	private void getSystemConfigurationsInternal(String filter) {
		Collection<Configuration> configurations = new ArrayList<>();
		configurations.add(prepareConfiguration(true));
		when(configurationManagement.getSystemConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredSystemConfigurations = service.getSystemConfigurations(filter);
		Assert.assertNotNull(filteredSystemConfigurations);
		Assert.assertTrue(filteredSystemConfigurations.size() == 1);
	}

	/**
	 * Some filter value.
	 */
	@Test
	public void getSystemConfigurations_filteredConfigurations_filteredResult() {
		Collection<Configuration> configurations = prepareConfigurationsCollection(true);
		when(configurationManagement.getSystemConfigurations()).thenReturn(configurations);

		Collection<Configuration> filteredSystemConfigurations = service.getSystemConfigurations("configKey");
		Assert.assertNotNull(filteredSystemConfigurations);
		Assert.assertTrue(filteredSystemConfigurations.size() == 1);
	}

	// ---------------------- updateTenantConfig ------------------------------------

	/**
	 * Update tenant configurations successfully.
	 */
	@Test
	public void updateTenantConfig_emptyRequestData_okResponse() {
		Configuration configuration = new Configuration();
		configuration.setConfigurationKey("key");
		configuration.setValue("value");
		Collection<Configuration> configurations = Arrays.asList(configuration);

		when(configurationManagement.getCurrentTenantConfigurations()).thenReturn(configurations);

		Collection<Configuration> updatedConfigurations = service.updateTenantConfig(configurations);
		Mockito.verify(configurationManagement, Mockito.times(1)).updateConfigurations(Matchers.eq(configurations));
		Assert.assertNotNull(updatedConfigurations);
	}

	// ---------------------- updateSystemConfig ------------------------------------

	/**
	 * Update system configurations successfully.
	 */
	@Test
	public void updateSystemConfig_emptyRequestData_okResponse() {
		Configuration configuration = prepareConfiguration(true);
		Collection<Configuration> configurations = Arrays.asList(configuration);

		when(configurationManagement.getSystemConfigurations()).thenReturn(configurations);

		Collection<Configuration> updatedConfigurations = service.updateSystemConfig(configurations);
		Mockito.verify(configurationManagement, Mockito.times(1))
				.updateSystemConfigurations(Matchers.eq(configurations));
		Assert.assertNotNull(updatedConfigurations);
	}

	// ---------------------- util method --------------------------------------------

	private Configuration prepareConfiguration(boolean system) {
		Configuration configuration = new Configuration();
		configuration.setConfigurationKey("configKey");
		configuration.setValue("configValue");
		configuration.setDefinition(configurationInstance);
		when(configurationInstance.getType()).thenReturn(Object.class);
		when(configurationInstance.getAlias()).thenReturn("someKey");
		when(configurationInstance.getSubSystem()).thenReturn("someSubSystem");
		when(configurationInstance.isSystemConfiguration()).thenReturn(system);
		configuration.setTenantId("unit.test");
		return configuration;
	}

	private Collection<Configuration> prepareConfigurationsCollection(boolean system) {
		Collection<Configuration> configurations = new ArrayList<>();
		Configuration config1 = prepareConfiguration(system);
		config1.setConfigurationKey("somethingElse");
		Configuration config2 = prepareConfiguration(system);
		configurations.add(config1);
		configurations.add(config2);
		return configurations;
	}

}