package com.sirma.sep.email.patch;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch which set (or update) class of service configuration if tenant has mailbox supportable enabled
 *
 * @author S.Djulgerova
 */
@Singleton
public class SetCOSToExistingTenantsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DomainAdministrationService domainAdministrationService;

	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	private SecurityContext securityContext;

	private EmailAddressResolver emailAddressResolver;

	private ConfigurationManagement configurationManagement;

	@Override
	public String getConfirmationMessage() {
		return "Tenant class of service configuration update was sucessful!";
	}

	@Override
	public void setUp() throws SetupException {
		emailIntegrationConfiguration = CDI.instantiateBean(EmailIntegrationConfiguration.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		domainAdministrationService = CDI.instantiateBean(DomainAdministrationService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		configurationManagement = CDI.instantiateBean(ConfigurationManagement.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
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
			Optional<DomainInformation> domainInfo = domainAdministrationService
					.getDomain(tenantEmails.get(0).getMailDomain());
			Configuration tenantDomainClassOfServiceConfig = new Configuration(
					emailIntegrationConfiguration.getTenantClassOfService().getName(),
					domainAdministrationService.extractCosFromDomainAddress(domainInfo),
					securityContext.getCurrentTenantId());
			configurationManagement.updateConfiguration(tenantDomainClassOfServiceConfig);
		} catch (EmailIntegrationException e) {
			LOGGER.error("Tenant class of service configuration can not be updated " + e.getMessage(), e);
		}
	}

}