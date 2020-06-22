package com.sirma.itt.seip.tenant.audit.step;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantUpdateAuditStep handles the update of the audit log functionalities when a tenant is
 * being updated. This step will check if the recentActivities core exists and if it doesn't it will
 * create it in the tenant's context.
 *
 * @author nvelkov
 */

@ApplicationScoped
@Extension(target = TenantStep.UPDATE_STEP_NAME, order = 6.6)
public class TenantUpdateAuditStep extends AbstractTenantStep {

	@Inject
	private AuditSolrProvisioning auditSolrProvisioning;

	@Override
	public String getIdentifier() {
		return "RecentActivitiesInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			String tenantName = DbProvisioning.createUserNameFromTenantId(context.getTenantInfo());
			if (!auditSolrProvisioning.coreExists(tenantName + AuditSolrProvisioning.RECENT_ACTIVITIES_SUFFIX,
					context.getTenantInfo())) {
				auditSolrProvisioning.provisionRecentActivitiesModel(context.getTenantInfo());
			}
		} catch (Exception e) {
			throw new TenantCreationException("Error during Recent activities init step!", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		return true;
	}
}
