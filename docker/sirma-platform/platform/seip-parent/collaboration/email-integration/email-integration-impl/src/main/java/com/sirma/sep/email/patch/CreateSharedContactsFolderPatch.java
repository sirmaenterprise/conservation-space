package com.sirma.sep.email.patch;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch which creates shared contacts folder for tenant admin and creates a contact for every existing email account
 * for that tenant.
 *
 * @author S.Djulgerova
 */
@Singleton
public class CreateSharedContactsFolderPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ShareFolderAdministrationService shareFolderAdministrationService;

	private EmailAddressResolver emailAddressResolver;

	private SecurityContext securityContext;

	@Override
	public String getConfirmationMessage() {
		return "Shared contacts folder was created sucessfully!";
	}

	@Override
	public void setUp() throws SetupException {
		shareFolderAdministrationService = CDI.instantiateBean(ShareFolderAdministrationService.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		emailAddressResolver = CDI.instantiateBean(EmailAddressResolver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
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

		List<EmailAddress> tenantEmails = emailAddressResolver
				.getAllEmailsByTenant(securityContext.getCurrentTenantId());

		if (tenantEmails.isEmpty()) {
			return;
		}
		try {
			shareFolderAdministrationService.createTenantShareFolder();
			for (EmailAddress address : tenantEmails) {
				shareFolderAdministrationService.addContactToShareFolder(address.getEmailAddress());
			}
		} catch (EmailIntegrationException e) {
			LOGGER.error("Shared contacts folder can not be created " + e.getMessage(), e);
		}
	}
}