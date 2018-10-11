package com.sirma.itt.seip.tenant.audit.step;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.audit.AuditDbProvisioning;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Test the tenant creation audit step.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantAuditStepTest {

	@Mock
	private AuditDbProvisioning auditDbProvisioning;

	@Mock
	private AuditSolrProvisioning solrAuditProvisioning;

	@Mock
	private AuditConfiguration auditDatabaseConfigurations;

	@Mock
	private RelationDbProvisioning relationDbProvisioning;

	@Mock
	private DatabaseConfiguration databaseConfiguration;

	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private TenantAuditStep step = new TenantAuditStep();

	@Before
	public void init() {
		mockDatabaseSettings(auditDatabaseConfigurations);
		mockDatabaseSettings(databaseConfiguration);
	}

	@Test
	public void testExecute() throws RollbackedException, JSONException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{id:'reuseDatabase', value:true}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);

		step.execute(data, context);
		// Verify that the db provisioning is called.
		Mockito.verify(auditDbProvisioning).provision(Matchers.anyMapOf(String.class, Serializable.class),
				Matchers.any(TenantInfo.class), Matchers.any(TenantRelationalContext.class),
				Matchers.any(TenantRelationalContext.class));

		// Verify that the audit solr provisioning is called.
		Mockito.verify(solrAuditProvisioning).provisionAuditModel(Matchers.anyMapOf(String.class, Serializable.class),
				Matchers.any(TenantInfo.class), Matchers.any(TenantRelationalContext.class));

		// Verify that the audit solr provisioning is called.
		Mockito.verify(solrAuditProvisioning).provisionRecentActivitiesModel(Matchers.any(TenantInfo.class));
	}

	@Test
	@SuppressWarnings("boxing")
	public void testRollback() throws RollbackedException, JSONException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{id:'reuseDatabase', value:true}]}"));
		TenantInfo info = new TenantInfo("test.id");

		step.delete(data, info, true);
		// Verify that the db rollback is called.
		Mockito.verify(auditDbProvisioning).rollback(Matchers.any(TenantRelationalContext.class),
				Matchers.any(TenantRelationalContext.class), Matchers.any(TenantInfo.class),
				Matchers.any(Boolean.class));

		// Verify that the audit solr rollback is called.
		Mockito.verify(solrAuditProvisioning).rollbackAuditCoreCreation(Matchers.any(TenantRelationalContext.class),
				Matchers.any(TenantInfo.class));

		// Verify that the audit solr rollback is called.
		Mockito.verify(solrAuditProvisioning).rollbackRecentActivitiesCoreCreation(Matchers.any(TenantInfo.class));
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