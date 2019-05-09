package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.tenant.service.SepRuntimeCamundaContainerDelegate;

@RunWith(MockitoJUnitRunner.class)
public class TenantCamundaStepTest {

	@Mock
	private CamundaDbProvisioning camundaDbProvisioning;

	@Mock
	private DatabaseConfiguration databaseConfigurations;

	@Mock
	private RelationDbProvisioning relationDbProvisioning;

	@Mock
	private CamundaConfiguration camundaDatabaseConfigurations;

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private SepRuntimeCamundaContainerDelegate camundaRuntimeContainerDelegate;

	@InjectMocks
	private TenantCamundaStep step;

	@Before
	public void init() {
		mockDatabaseSettings(databaseConfigurations);
		mockDatabaseSettings(camundaDatabaseConfigurations);
	}

	@Test
	public void should_executeStep() throws JSONException, RollbackedException {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();

		final String TENANT_ID = "tenantId";
		context.setTenantInfo(new TenantInfo(TENANT_ID));
		step.execute(data, context);
		verify(camundaDbProvisioning).provision(anyMap(), any(TenantInfo.class), any(TenantRelationalContext.class),
				any(TenantRelationalContext.class));

		verify(camundaRuntimeContainerDelegate).deployEngine(TENANT_ID);
	}

	@Test(expected = TenantCreationException.class)
	public void should_throwException() {
		// Exception will be thrown because of the empty json object with no properties.
		step.execute(new TenantStepData("id", new JSONObject()), new TenantInitializationContext());
	}

	@Test
	public void should_performDelete() throws JSONException {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenantId"));
		boolean result = step.delete(data, new TenantDeletionContext(new TenantInfo("tenantId"), false));
		assertTrue(result);
		verify(camundaDbProvisioning)
				.rollback(any(TenantRelationalContext.class), any(TenantRelationalContext.class), any(TenantInfo.class),
						anyBoolean());
	}

	@Test
	public void should_returnFalse_onException() throws JSONException {
		boolean result = step.delete(new TenantStepData("id", new JSONObject()),
				new TenantDeletionContext(new TenantInfo("tenantId"), false));
		assertFalse(result);
	}

	private static void mockDatabaseSettings(DatabaseSettings settings) {
		// Can't stub while stubbing so we need to stub the returned properties beforehand.
		ConfigurationProperty<String> dbName = mockConfigurationProperty("dbName");
		Mockito.when(settings.getDatabaseNameConfiguration()).thenReturn(dbName);
		ConfigurationProperty<String> username = mockConfigurationProperty("admin");
		Mockito.when(settings.getAdminUsernameConfiguration()).thenReturn(username);
		ConfigurationProperty<String> pass = mockConfigurationProperty("pass");
		Mockito.when(settings.getAdminPasswordConfiguration()).thenReturn(pass);
		ConfigurationProperty<String> dialect = mockConfigurationProperty("dialect");
		Mockito.when(settings.getDatabaseDialectConfiguration()).thenReturn(dialect);
		ConfigurationProperty<String> host = mockConfigurationProperty("host");
		Mockito.when(settings.getDatabaseHostConfiguration()).thenReturn(host);
		ConfigurationProperty<Integer> port = mockConfigurationProperty(new Integer(25));
		Mockito.when(settings.getDatabasePortConfiguration()).thenReturn(port);
	}

	private static <T> ConfigurationProperty<T> mockConfigurationProperty(T value) {
		ConfigurationProperty<T> property = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(property.get()).thenReturn(value);
		return property;
	}

}
