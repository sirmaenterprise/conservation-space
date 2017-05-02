package com.sirma.itt.seip.tenant.audit.step;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.audit.AuditDbProvisioning;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Test the tenant creation audit step.
 *
 * @author nvelkov
 */
public class TenantCreationAuditStepTest {

	@Mock
	private AuditDbProvisioning auditDbProvisioning;

	@Mock
	private AuditSolrProvisioning solrAuditProvisioning;

	@InjectMocks
	private TenantCreationAuditStep step = new TenantCreationAuditStep();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
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
		Mockito.verify(solrAuditProvisioning).provisionRecentActivitiesModel(Matchers.any(TenantInfo.class),
				Matchers.anyString());
	}

	@Test
	public void testRollback() throws RollbackedException, JSONException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{id:'reuseDatabase', value:true}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);

		step.rollback(data, context);
		// Verify that the db rollback is called.
		Mockito.verify(auditDbProvisioning).rollback(Matchers.any(TenantRelationalContext.class),
				Matchers.any(TenantInfo.class), Matchers.any(Boolean.class));

		// Verify that the audit solr rollback is called.
		Mockito.verify(solrAuditProvisioning).rollbackAuditCoreCreation(Matchers.any(TenantRelationalContext.class),
				Matchers.any(TenantInfo.class));

		// Verify that the audit solr rollback is called.
		Mockito.verify(solrAuditProvisioning).rollbackRecentActivitiesCoreCreation(
				Matchers.any(TenantRelationalContext.class), Matchers.any(TenantInfo.class));
	}

}
