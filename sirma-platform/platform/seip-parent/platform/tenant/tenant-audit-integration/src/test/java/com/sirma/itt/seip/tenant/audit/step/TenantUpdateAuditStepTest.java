package com.sirma.itt.seip.tenant.audit.step;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Test the tenant update audit step.
 *
 * @author nvelkov
 */
public class TenantUpdateAuditStepTest {

	@Mock
	private AuditSolrProvisioning auditSolrProvisioning;

	@Mock
	private AuditConfiguration auditConfiguration;

	@Mock
	protected SecurityContextManager securityContextManager;

	@InjectMocks
	private TenantUpdateAuditStep step = new TenantUpdateAuditStep();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("boxing")
	public void testExecute() throws RollbackedException, JSONException, IOException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{id:'reuseDatabase', value:true}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		// Meaning the core doesn't exist
		Mockito.when(auditSolrProvisioning.coreExists(Matchers.anyString(), Matchers.any(TenantInfo.class))).thenReturn(
				false);
		step.execute(data, context);

		// Verify that the audit solr provisioning is called.
		Mockito.verify(auditSolrProvisioning).provisionRecentActivitiesModel(Matchers.any(TenantInfo.class)
		);
	}

	@Test
	@SuppressWarnings("boxing")
	public void testExecuteCoreAlreadyExists()
			throws RollbackedException, JSONException, IOException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[{id:'reuseDatabase', value:true}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		// Meaning the core already exists
		Mockito.when(auditSolrProvisioning.coreExists(Matchers.anyString(), Matchers.any(TenantInfo.class))).thenReturn(
				true);
		step.execute(data, context);

		// Verify that the audit solr provisioning is called.
		Mockito.verify(auditSolrProvisioning, VerificationModeFactory.times(0)).provisionRecentActivitiesModel(
				Matchers.any(TenantInfo.class));
	}
}