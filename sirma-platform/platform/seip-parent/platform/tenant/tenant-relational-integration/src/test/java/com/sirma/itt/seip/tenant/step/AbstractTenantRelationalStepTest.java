package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Test the abstract tenant relational step.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractTenantRelationalStepTest {

	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private TenantDbStep step;

	@Test
	public void should_getRelationalContext_ifReused() throws JSONException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{'id':'reuseDatabase', 'value':'true'}]}"));
		TenantRelationalContext context = step.getRelationalContextIfReused(data, mockDatabaseSettings(), "dsName",
				new TenantInfo("tenantId"));
		verify(securityContextManager).initializeTenantContext("tenantId");
		verify(securityContextManager).endContextExecution();
		assertEquals("dbName", context.getDatabaseName());
		assertEquals("dsName", context.getDatasourceName());
	}

	@Test
	public void should_returnNull_when_getRelationalContext_ifNotReused() throws JSONException {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantRelationalContext context = step.getRelationalContextIfReused(data, mockDatabaseSettings(), "dsName",
				new TenantInfo("tenantId"));
		assertNull(context);
	}

	private static DatabaseSettings mockDatabaseSettings() {
		DatabaseSettings settings = Mockito.mock(DatabaseSettings.class);
		ConfigurationProperty<String> dbNameConfig = mockConfiguration("dbName");
		when(settings.getDatabaseNameConfiguration()).thenReturn(dbNameConfig);
		ConfigurationProperty<String> adminUsernameConfig = mockConfiguration("admin");
		when(settings.getAdminUsernameConfiguration()).thenReturn(adminUsernameConfig);
		ConfigurationProperty<String> adminPasswordConfig = mockConfiguration("pass");
		when(settings.getAdminPasswordConfiguration()).thenReturn(adminPasswordConfig);
		ConfigurationProperty<String> dbDialectConfig = mockConfiguration("dialect");
		when(settings.getDatabaseDialectConfiguration()).thenReturn(dbDialectConfig);
		ConfigurationProperty<String> dbHostConfig = mockConfiguration("host");
		when(settings.getDatabaseHostConfiguration()).thenReturn(dbHostConfig);
		ConfigurationProperty<Integer> dbPortConfig = mockConfiguration(25);
		when(settings.getDatabasePortConfiguration()).thenReturn(dbPortConfig);
		return settings;
	}

	private static <T> ConfigurationProperty<T> mockConfiguration(T value) {
		ConfigurationProperty<T> property = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(property.get()).thenReturn(value);
		return property;
	}
}
