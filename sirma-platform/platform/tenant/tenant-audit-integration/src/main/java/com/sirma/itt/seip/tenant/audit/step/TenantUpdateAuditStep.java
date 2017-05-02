package com.sirma.itt.seip.tenant.audit.step;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantUpdateAuditStep handles the update of the audit log functionalities when a tenant is being updated. This
 * step will check if the recentActivities core exists and if it doesn't it will create it in the tenant's context.
 *
 * @author nvelkov
 */

@ApplicationScoped
@Extension(target = TenantStep.UPDATE_STEP_NAME, order = 6.6)
public class TenantUpdateAuditStep extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
				auditSolrProvisioning.provisionRecentActivitiesModel(context.getTenantInfo(), tenantName);
			}
		} catch (Exception e) {
			throw new TenantCreationException("Error during Recent activities init step!", e);
		}
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		TenantRelationalContext relationalContext = context.getProvisionContext(getIdentifier());
		try {
			auditSolrProvisioning.rollbackRecentActivitiesCoreCreation(relationalContext, context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during recent activities Solr rollback!", e);
		}
		return false;
	}

}
