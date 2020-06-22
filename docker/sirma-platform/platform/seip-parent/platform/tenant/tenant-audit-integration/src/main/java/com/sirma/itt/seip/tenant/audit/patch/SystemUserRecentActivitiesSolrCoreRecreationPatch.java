package com.sirma.itt.seip.tenant.audit.patch;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Re-creates the recent activities solr core if system user name is not the default one.
 *
 * @author smustafov
 */
public class SystemUserRecentActivitiesSolrCoreRecreationPatch implements CustomTaskChange {

	private SecurityConfiguration securityConfiguration;

	@Override
	public void setUp() throws SetupException {
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentUserName = securityConfiguration.getSystemUserName().get();
		if (!currentUserName.equals(SecurityContext.SYSTEM_USER_NAME)) {
			try {
				RecentActivitiesSolrCoreRecreationPatch patch = new RecentActivitiesSolrCoreRecreationPatch();
				patch.setUp();
				patch.execute(database);
			} catch (SetupException e) {
				throw new CustomChangeException("Error executing recent activities patch", e);
			}
		}
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public String getConfirmationMessage() {
		return null;
	}

}
