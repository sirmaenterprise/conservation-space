package com.sirma.itt.seip.tenant.audit.patch;

import java.io.IOException;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.audit.AuditSolrProvisioning;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Re-creates the recent activities solr core.
 *
 * @author nvelkov
 */
public class RecentActivitiesSolrCoreRecreationPatch implements CustomTaskChange {

	protected AuditSolrProvisioning auditSolrProvisioning;
	private SecurityContextManager securityContextManager;

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public void setUp() throws SetupException {
		auditSolrProvisioning = CDI.instantiateBean(AuditSolrProvisioning.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContextManager = CDI.instantiateBean(SecurityContextManager.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	protected TenantInfo getTenantInfo() {
		String currentTenantId = securityContextManager.getCurrentContext().getCurrentTenantId();
		return new TenantInfo(currentTenantId);
	}

	protected String getTenantName() {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(getTenantInfo());
		return DbProvisioning.createUserNameFromTenantId(context.getTenantInfo());
	}

	protected String getCoreName() {
		return getTenantName() + AuditSolrProvisioning.RECENT_ACTIVITIES_SUFFIX;
	}

	@Override
	public String getConfirmationMessage() {
		return "Recent activities solr core re-creation was succesfull";
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TenantInfo info = getTenantInfo();
		String tenantName = getTenantName();
		String coreName = getCoreName();
		try {
			if (auditSolrProvisioning.coreExists(coreName, info)) {
				auditSolrProvisioning.unloadCore(coreName);
			}
			auditSolrProvisioning.provisionRecentActivitiesModel(info, tenantName);
		} catch (RollbackedException | IOException e) {
			throw new CustomChangeException("Error during Recent activities init step!", e);
		}

	}

}
