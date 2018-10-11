package com.sirma.sep.email.patch;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.email.ZimbraEmailIntegrationConstants;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.entity.MailboxSupportable;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.DomainAdministrationService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch which get tenant domain COS and update it's property
 *
 * @author S.Djulgerova
 */
@Singleton
public class DisableGalAutocompletePatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DomainAdministrationService domainAdministrationService;

	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	private DbDao dbDao;

	@Override
	public String getConfirmationMessage() {
		return "Tenant domain COS update was sucessful!";
	}

	@Override
	public void setUp() throws SetupException {
		emailIntegrationConfiguration = CDI.instantiateBean(EmailIntegrationConfiguration.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		domainAdministrationService = CDI.instantiateBean(DomainAdministrationService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Nothing to do here
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		Collection<String> mailboxSupportableClasses = dbDao
				.fetchWithNamed(MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY, Collections.emptyList());

		if (mailboxSupportableClasses.isEmpty()) {
			return;
		}

		try {
			String cosId = domainAdministrationService
					.getCosByName(emailIntegrationConfiguration.getTenantClassOfService().get()).getCosId();
			domainAdministrationService.modifyCos(cosId, ZimbraEmailIntegrationConstants.ENABLE_GAL_AUTOCOMPLETE,
					"FALSE");
		} catch (EmailIntegrationException e) {
			LOGGER.error("COS can not be updated " + e.getMessage(), e);
		}
	}

}