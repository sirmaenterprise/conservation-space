package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.db.DatasourceProvisioner;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;

/**
 * Test the camunda db provisioning.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class CamundaDbProvisioningTest {

	@Mock
	private ConfigurationProperty<String> dbDialect;

	@Mock
	private ConfigurationProperty<String> dbUser;

	@Mock
	private ConfigurationProperty<String> dbPass;

	@Mock
	private DbProvisioning dbProvisioning;

	@Mock
	private DatasourceProvisioner datasourceProvisioner;

	@Mock
	private SubsystemTenantAddressProvider addressProvider;

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private ConfigurationManagement configurationManagement;

	CamundaConfiguration camundaConfiguration;

	@InjectMocks
	private CamundaDbProvisioning provisioning;

	@Before
	public void beforeMethod() {
		camundaConfiguration = Mockito.mock(CamundaConfiguration.class, (Answer<Object>) invocation -> {
			if (String.class.equals(invocation.getMethod().getReturnType())) {
				return "value";
			}
			ConfigurationProperty<String> configuration = Mockito.mock(ConfigurationProperty.class);
			Mockito.when(configuration.getName()).thenReturn("name");
			Mockito.when(configuration.get()).thenReturn("value");
			return configuration;
		});
		ReflectionUtils.setFieldValue(provisioning, "camundaConfiguration", camundaConfiguration);
	}

	@Test
	public void should_provisionCamundaDb_withContext() throws URISyntaxException, RollbackedException {

		TenantRelationalContext contextToUse = new TenantRelationalContext();
		contextToUse.setServerAddress(new URI("http://www.test.com"));
		Mockito.when(addressProvider.provideAddressForNewTenant(Matchers.any()))
		.thenReturn(new URI("http://www.test.com"));

		provisioning.provision(new HashMap<>(), new TenantInfo("tenant"), contextToUse, new TenantRelationalContext());

		Mockito.verify(datasourceProvisioner, Mockito.times(1)).createXaDatasource(Matchers.any(DatasourceModel.class));
		Mockito.verify(configurationManagement, Mockito.times(1)).addConfigurations(Matchers.anyCollection());
	}

	@Test
	public void should_provisionCamundaDb_withoutContext() throws URISyntaxException, RollbackedException {

		Mockito.when(addressProvider.provideAddressForNewTenant(Matchers.any()))
				.thenReturn(new URI("http://www.test.com"));

		provisioning.provision(new HashMap<>(), new TenantInfo("tenant"), null, new TenantRelationalContext());

		Mockito.verify(datasourceProvisioner, Mockito.times(1)).createXaDatasource(Matchers.any(DatasourceModel.class));
		Mockito.verify(configurationManagement, Mockito.times(1)).addConfigurations(Matchers.anyCollection());
	}

	@Test
	public void should_provisionCamundaDb_forDefaultTenant() throws URISyntaxException, RollbackedException {

		mockDatasourceProvisioner();
		Mockito.when(addressProvider.provideAddressForNewTenant(Matchers.any()))
				.thenReturn(new URI("http://www.test.com"));

		provisioning.provision(new HashMap<>(), new TenantInfo("default.tenant"), null, new TenantRelationalContext());

		Mockito.verifyZeroInteractions(configurationManagement);
	}

	@Test
	public void should_rollback_when_provisioningHasFailed_with_reusableDb() throws RollbackedException, SQLException {

		mockDatasourceProvisioner();

		verifyCorrectRollbackExecution();
	}

	/**
	 * If while rolling back the provisioning an exception occurs while removing the datasource, the implementation must
	 * try to remove the database and configurations.
	 *
	 * @throws RollbackedException
	 *             if a rollback exception occurs
	 * @throws SQLException
	 *             if an sql exception occurs
	 */
	@Test
	public void should_rollback_when_datasourceRemovalHasFailed() throws RollbackedException, SQLException {

		verifyCorrectRollbackExecution();
	}

	/**
	 * If while rolling back the provisioning an exception occurs while removing the database, the implementation must
	 * try to remove the datasource and configurations.
	 *
	 * @throws RollbackedException
	 *             if a rollback exception occurs
	 * @throws SQLException
	 *             if an sql exception occurs
	 */
	@Test
	public void should_rollback_when_databaseRemovalHasFailed() throws RollbackedException, SQLException {

		mockDatasourceProvisioner();
		Mockito.doThrow(Exception.class).when(dbProvisioning).dropDatabase("dbName", null, null, null, null);

		verifyCorrectRollbackExecution();
	}

	private void verifyCorrectRollbackExecution() throws SQLException, RollbackedException {
		TenantRelationalContext relationalContext = new TenantRelationalContext();
		relationalContext.setDatasourceName("dsName");
		relationalContext.setDatabaseName("dbName");
		URI uri = null;
		try {
			uri = new URI("http://www.test.com");
			relationalContext.setServerAddress(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		provisioning.rollback(relationalContext, null, new TenantInfo("tenant"), true);

		Mockito.verify(configurationManagement, Mockito.times(6)).removeConfiguration(Matchers.any());
		Mockito.verify(dbProvisioning, Mockito.times(1)).dropDatabase("value", null, uri, null, null);
		Mockito.verify(datasourceProvisioner, Mockito.times(1)).removeDatasource(Matchers.anyString());
	}

	private void mockDatasourceProvisioner() {
		Mockito.when(datasourceProvisioner.getXaDataSourceServerName(Matchers.anyString())).thenReturn("name");
		Mockito.when(datasourceProvisioner.getXaDataSourcePort(Matchers.anyString())).thenReturn("port");
	}
}