package com.sirma.itt.seip.db;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test the configuration datasource provisioner.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationDatasourceProvisionerTest {

	@Mock
	private DatasourceProvisioner provisioner;

	@InjectMocks
	private ConfigurationDatasourceProvisioner configProvisioner = new ConfigurationDatasourceProvisioner();

	/**
	 * Test for the datasource creation
	 *
	 * @throws RollbackedException
	 *             if an exception occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void should_createDataSource_when_missing() throws RollbackedException {
		DefaultDatabaseConfigurations configurations = new DefaultDatabaseConfigurations();
		ReflectionUtils.setFieldValue(configurations, "databaseDialect", Mockito.mock(ConfigurationProperty.class));
		ReflectionUtils.setFieldValue(configurations, "databaseName", Mockito.mock(ConfigurationProperty.class));
		ConfigurationProperty<Integer> portConfig = Mockito.mock(ConfigurationProperty.class);
		when(portConfig.get()).thenReturn(1337);
		ReflectionUtils.setFieldValue(configurations, "databasePort", portConfig);
		ConfigurationProperty<String> hostConfig = Mockito.mock(ConfigurationProperty.class);
		when(hostConfig.get()).thenReturn("host");
		ReflectionUtils.setFieldValue(configurations, "databaseHost", hostConfig);
		ReflectionUtils.setFieldValue(configurations, "adminPassword", Mockito.mock(ConfigurationProperty.class));
		ReflectionUtils.setFieldValue(configurations, "adminUsername", Mockito.mock(ConfigurationProperty.class));
		ConfigurationProperty property = Mockito.mock(ConfigurationProperty.class);
		when(property.computeIfNotSet(any())).then(a -> a.getArgumentAt(0, Supplier.class).get());
		ReflectionUtils.setFieldValue(configurations, "connectionTimeoutSettings", property);
		ReflectionUtils.setFieldValue(configurations, "connectionValidationSettings", property);
		ReflectionUtils.setFieldValue(configurations, "connectionPoolSettings", property);

		ArgumentCaptor<DatasourceModel> modelCaptor = ArgumentCaptor.forClass(DatasourceModel.class);

		Mockito.doThrow(new RollbackedException()).when(provisioner)
				.createXaDatasource(any(DatasourceModel.class));
		configProvisioner.lookupDataSource("jndiName", configurations);

		Mockito.verify(provisioner, Mockito.atLeastOnce()).createXaDatasource(modelCaptor.capture());
		Assert.assertEquals(1337, modelCaptor.getValue().getDatabasePort());
		Assert.assertEquals("host", modelCaptor.getValue().getDatabaseHost());

	}
}
