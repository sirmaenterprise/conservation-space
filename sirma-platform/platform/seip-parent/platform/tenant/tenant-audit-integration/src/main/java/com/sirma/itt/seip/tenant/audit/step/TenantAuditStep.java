package com.sirma.itt.seip.tenant.audit.step;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tenant.audit.AuditDbProvisioning;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.RelationDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.step.AbstractTenantRelationalStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * The TenantAuditStep handles the creation/deletion of one database (for the audit log) and two
 * solr cores - one for the audit log and the second one for the recent activities log. The
 * database's name is in the format tenantname_audit and the two solr cores are named
 * tenantname_audit and tenantname_recentActivities.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 6.5)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 14)
public class TenantAuditStep extends AbstractTenantRelationalStep {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuditDbProvisioning auditDbProvisioning;

	@Inject
	private AuditSolrProvisioning solrAuditProvisioning;

	@Inject
	private AuditConfiguration auditDatabaseConfigurations;
	
	@Inject
	private RelationDbProvisioning relationDbProvisioning;
	
	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Override
	public String getIdentifier() {
		return "AuditInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		try {
			TenantRelationalContext relationalContext = new TenantRelationalContext();
			context.addProvisionContext(getIdentifier(), relationalContext);
			TenantRelationalContext contextToUse = getRelationalContextIfReused(data, databaseConfiguration,
					relationDbProvisioning.getDatasourceName(context.getTenantInfo()), context.getTenantInfo());
			auditDbProvisioning.provision(data.getProperties(), context.getTenantInfo(), contextToUse,
					relationalContext);
			solrAuditProvisioning.provisionAuditModel(data.getProperties(), context.getTenantInfo(), relationalContext);

			solrAuditProvisioning.provisionRecentActivitiesModel(context.getTenantInfo());

		} catch (Exception e) {
			throw new TenantCreationException("Error during Audit init step!", e);
		}
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		TenantRelationalContext relationalContext = getRelationalContext(auditDatabaseConfigurations,
				auditDbProvisioning.getDatasourceName(tenantInfo), tenantInfo);
		try {
			TenantRelationalContext contextToUse = getRelationalContextIfReused(data, databaseConfiguration,
					relationDbProvisioning.getDatasourceName(tenantInfo), tenantInfo);
			// if database was not reused then remove it otherwise do not
			auditDbProvisioning.rollback(relationalContext, contextToUse, tenantInfo, !isDatabaseReused(data));
		} catch (Exception e) {
			LOGGER.warn("Error during audit DB rollback {}!", e.getMessage());
			LOGGER.trace("Error during audit DB rollback!", e);
		}
		try {
			solrAuditProvisioning.rollbackAuditCoreCreation(relationalContext, tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Error during audit Solr rollback {}!", e.getMessage());
			LOGGER.trace("Error during audit Solr rollback!", e);
		}
		try {
			solrAuditProvisioning.rollbackRecentActivitiesCoreCreation(tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Error during recent activities Solr rollback {}!", e.getMessage());
			LOGGER.trace("Error during recent activities Solr rollback!", e);
		}
		return false;
	}

}