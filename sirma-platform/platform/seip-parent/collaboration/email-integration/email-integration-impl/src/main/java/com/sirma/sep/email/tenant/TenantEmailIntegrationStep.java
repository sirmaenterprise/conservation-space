package com.sirma.sep.email.tenant;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.sep.account.administration.EmailAccountAdministrationServiceImpl;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;

/**
 * Tenant domain creation step used to initialize mail server domain for created tenant.
 *
 * @author S.Djulgerova
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 12)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 9)
public class TenantEmailIntegrationStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String EMAIL_DOMAIN_ADDRESS = "tenantdomainaddress";
	private static final String WEBMAIL_URL = "webmailurl";
	private static final String WEBMAIL_PORT = "webmailport";

	@Inject
	protected ConfigurationManagement configurationManagement;

	@Inject
	protected SecurityContextManager securityContextManager;

	@Inject
	private DomainAdministrationService domainAdministrationService;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	@Inject
	private EmailAccountAdministrationServiceImpl accoundAdministrationService;

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Override
	public String getIdentifier() {
		return "EmailIntegrationInitialization";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		TenantInfo tenantInfo = context.getTenantInfo();
		String tenantId = tenantInfo.getTenantId();

		Optional<String> domainAddress = getTenantDomainAddress(data);
		String tenantWebmailUrl = getWebmailUrl(data);
		String tenantWebmailPort = getWebmailPort(data);

		// if any of the configurations are set, continue with domain initialization
		// if needed configurations are not set by mistake
		// at least an exception will be thrown to help indicate the problem
		if (domainAddress.isPresent() || tenantWebmailUrl != null || tenantWebmailPort != null) {
			try {
				securityContextManager.initializeTenantContext(tenantId);

				addConfigurations(tenantId, tenantWebmailUrl, tenantWebmailPort);

				String tenantDomainAddress = domainAddress.orElse(tenantId);
				Configuration tenantDomainClassOfServiceConfig = buildDomainClassOfServiceConfiguration(tenantId,
						tenantDomainAddress);
				Configuration tenantDomainAddressConfig = new Configuration(
						emailIntegrationConfiguration.getTenantDomainAddress().getName(), tenantDomainAddress,
						tenantId);

				configurationManagement
						.addConfigurations(Arrays.asList(tenantDomainAddressConfig, tenantDomainClassOfServiceConfig));
				return true;
			} catch (Exception e) {
				throw new TenantCreationException("Error during tenant domain init step!", e);
			} finally {
				securityContextManager.endContextExecution();
			}
		}

		LOGGER.info("Skipping EmailIntegration Initialization because no configuration is set");

		return true;
	}

	private Configuration buildDomainClassOfServiceConfiguration(String tenantId, String tenantDomainAddress)
			throws EmailIntegrationException {
		Configuration tenantDomainClassOfServiceConfig;
		Optional<DomainInformation> domainInfo = domainAdministrationService.getDomain(tenantDomainAddress);
		if (!domainInfo.isPresent()) {
			domainAdministrationService.createDomain(tenantDomainAddress);
			tenantDomainClassOfServiceConfig = setTenantDomainConfiguration(tenantDomainAddress, tenantId);
		} else {
			tenantDomainClassOfServiceConfig = setTenantDomainConfiguration(
					domainAdministrationService.extractCosFromDomainAddress(domainInfo), tenantId);
		}
		return tenantDomainClassOfServiceConfig;
	}

	private void addConfigurations(String tenant, String tenantWebmailUrl, String tenantWebmailPort) {
		if (StringUtils.isBlank(tenantWebmailUrl)) {
			throw new ConfigurationException(
					"Please set configuration:" + emailIntegrationConfiguration.getWebmailUrl().getName());
		}
		if (StringUtils.isBlank(tenantWebmailPort)) {
			throw new ConfigurationException(
					"Please set configuration:" + emailIntegrationConfiguration.getWebmailPort().getName());
		}
		Configuration webmailUrlConfig = new Configuration(
				emailIntegrationConfiguration.getWebmailUrl().getName(), tenantWebmailUrl, tenant);
		Configuration webmailPortConfig = new Configuration(
				emailIntegrationConfiguration.getWebmailPort().getName(), tenantWebmailPort, tenant);
		configurationManagement.addConfigurations(Arrays.asList(webmailUrlConfig, webmailPortConfig));
	}

	private Configuration setTenantDomainConfiguration(String cosName, String tenantId) {
		return new Configuration(emailIntegrationConfiguration.getTenantClassOfService().getName(), cosName, tenantId);
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		try {
			TenantInfo tenantInfo = context.getTenantInfo();

			securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
			String tenantDomainAddress = Optional
					.ofNullable(emailIntegrationConfiguration.getTenantDomainAddress().get())
					.orElse(getTenantDomainAddress(data).orElseGet(tenantInfo::getTenantId));

			domainAdministrationService.getDomain(tenantDomainAddress)
					.ifPresent(domainInfo -> deleteDomain(domainInfo, tenantInfo.getTenantId()));

			configurationManagement
					.removeConfiguration(emailIntegrationConfiguration.getTenantDomainAddress().getName());
			configurationManagement
					.removeConfiguration(emailIntegrationConfiguration.getTenantClassOfService().getName());
			configurationManagement
					.removeConfiguration(emailIntegrationConfiguration.getTenantAdminAccount().getName());
		} catch (Exception e) {
			LOGGER.warn("Error during tenant domain rollback step {}!", e.getMessage());
			LOGGER.trace("Error during tenant domain rollback step!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
		return false;
	}

	private void deleteDomain(DomainInformation domainInfo, String tenantId) {
		try {
			// check if domain has been populated with tenant accounts
			List<EmailAddress> tenantEmails = emailAddressResolver.getAllEmailsByTenant(tenantId);
			List<String> tenantsInDomain = emailAddressResolver.getAllTenantsInDomain(domainInfo.getDomainName());
			securityContextManager.initializeTenantContext(tenantId);

			if (!tenantEmails.isEmpty()) {
				for (EmailAddress tenantEmail : tenantEmails) {
					accoundAdministrationService.deleteAccount(
							accoundAdministrationService.getAccount(tenantEmail.getEmailAddress()).getAccountId());
					emailAddressResolver.deleteEmailAddress(tenantEmail.getEmailAddress());
				}
			}
			// if tenant creation rollbacks, there would still be nothing in the tables.
			if (tenantsInDomain.size() <= 1) {
				domainAdministrationService.deleteDomain(domainInfo);
			}

		} catch (EmailIntegrationException e) {
			LOGGER.warn("Error during tenant domain rollback step {}!", e.getMessage());
			LOGGER.trace("Error during tenant domain rollback step!", e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private static Optional<String> getTenantDomainAddress(TenantStepData data) {
		return Optional.ofNullable(data.getPropertyValue(EMAIL_DOMAIN_ADDRESS, false));
	}

	private static String getWebmailUrl(TenantStepData data) {
		return StringUtils.trimToNull(data.getPropertyValue(WEBMAIL_URL, false));
	}

	private static String getWebmailPort(TenantStepData data) {
		return StringUtils.trimToNull(data.getPropertyValue(WEBMAIL_PORT, false));
	}

}
