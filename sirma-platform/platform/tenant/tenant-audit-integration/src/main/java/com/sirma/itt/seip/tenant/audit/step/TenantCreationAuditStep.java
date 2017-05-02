package com.sirma.itt.seip.tenant.audit.step;

import static com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning.isDatabaseReused;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.audit.AuditDbProvisioning;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.step.TenantCreationDbStep;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantCreationAuditStep handles the creation of one database (for the audit log) and two solr cores - one for the
 * audit log and the second one for the recent activities log. The database's name is in the format tenantname_audit and
 * the two solr cores are named tenantname_audit and tenantname_recentActivities.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 6.5)
public class TenantCreationAuditStep extends AbstractTenantCreationStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuditDbProvisioning auditDbProvisioning;

	@Inject
	private AuditSolrProvisioning solrAuditProvisioning;

	@Override
	public String getIdentifier() {
		return "AuditInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			TenantRelationalContext relationalContext = new TenantRelationalContext();
			context.addProvisionContext(getIdentifier(), relationalContext);
			TenantRelationalContext contextToUse = null;
			if (isDatabaseReused(data)) {
				contextToUse = context.getProvisionContext(TenantCreationDbStep.STEP_NAME);
			}
			auditDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), contextToUse,
					relationalContext);
			solrAuditProvisioning.provisionAuditModel(data.getProperties(), context.getTenantInfo(), relationalContext);

			String tenantName = DbProvisioning.createUserNameFromTenantId(context.getTenantInfo());
			solrAuditProvisioning.provisionRecentActivitiesModel(context.getTenantInfo(), tenantName);

		} catch (Exception e) {
			throw new TenantCreationException("Error during Audit init step!", e);
		}
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		TenantRelationalContext relationalContext = context.getProvisionContext(getIdentifier());
		try {
			// if database was not reused then remove it otherwise do not
			auditDbProvisioning.rollback(relationalContext, context.getTenantInfo(), !isDatabaseReused(data));
		} catch (Exception e) {
			LOGGER.error("Error during audit DB rollback!", e);
		}
		try {
			solrAuditProvisioning.rollbackAuditCoreCreation(relationalContext, context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during audit Solr rollback!", e);
		}
		try {
			solrAuditProvisioning.rollbackRecentActivitiesCoreCreation(relationalContext, context.getTenantInfo());
		} catch (Exception e) {
			LOGGER.error("Error during recent activities Solr rollback!", e);
		}
		return false;
	}

}
