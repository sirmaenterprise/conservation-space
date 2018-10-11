package com.sirma.sep.email.patch;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.email.ZimbraEmailIntegrationConstants;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.service.EmailAccountAdministrationService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch which get all records in email address resolver for given tenant and update it's mailbox account properties
 *
 * @author S.Djulgerova
 */
@Singleton
public class EnableSharedAddressBookPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private EmailAccountAdministrationService emailAccountAdministrationService;

	private SecurityContext securityContext;

	private EmailAddressResolver emailAddressResolver;

	@Override
	public String getConfirmationMessage() {
		return "User account update was sucessful!";
	}

	@Override
	public void setUp() throws SetupException {
		emailAccountAdministrationService = CDI.instantiateBean(EmailAccountAdministrationService.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		emailAddressResolver = CDI.instantiateBean(EmailAddressResolver.class, CDI.getCachedBeanManager(),
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
			for (EmailAddress address : tenantEmails) {
				EmailAccountInformation accountInfo = emailAccountAdministrationService
						.getAccount(address.getEmailAddress());
				List<GenericAttribute> accountAttributes = new LinkedList<>();
				accountAttributes
						.add(new GenericAttribute(ZimbraEmailIntegrationConstants.ENABLE_SHARED_ADDRESS_BOOK, "TRUE"));
				accountAttributes
						.add(new GenericAttribute(ZimbraEmailIntegrationConstants.ENABLE_AUTO_ADD_ADDRESS, "FALSE"));
				emailAccountAdministrationService.modifyAccount(accountInfo.getAccountId(), accountAttributes);
			}
		} catch (EmailIntegrationException e) {
			LOGGER.error("Account can not be updated " + e.getMessage(), e);
		}
	}

}