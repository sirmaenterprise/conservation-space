package com.sirma.sep.email.configuration;

import static com.sirma.sep.email.EmailIntegrationConstants.TENANT_ADMIN_ACCOUNT_PREF;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailAccountCreationException;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.MailboxSupportableService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Expose the email integration subsystem configurations.
 *
 * @author S.Djulgerova
 */
@Singleton
public class EmailIntegrationConfigurationImpl implements EmailIntegrationConfiguration {

	private static final long serialVersionUID = -3203224254824171559L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailIntegrationConfigurationImpl.class);

	@ConfigurationPropertyDefinition(defaultValue = "60000", subSystem = "application", label = "The time interval on which the UI will poll the mailbox through the zimbra backend SOAP API for changes in the messages. Interval is in milliseconds. Set as a minimum 60sec")
	private static final String MAILBOX_STATUS_POLL_INTERVAL = "subsystem.emailintegration.mailbox.status.poll.interval";

	@ConfigurationPropertyDefinition(system = true, subSystem = "application", label = "System admin account name used in log in.")
	private static final String ADMIN_NAME = "subsystem.emailintegration.email.admin.name";

	@ConfigurationPropertyDefinition(system = true, password = true, subSystem = "application", label = "System admin account password used in log in")
	private static final String ADMIN_PASSWORD = "subsystem.emailintegration.email.admin.password"; // NOSONAR

	@ConfigurationPropertyDefinition(subSystem = "application", label = "WARNING: This configuration is mandatory for email integration and represents tenant admin account name used to log in. Deleting this configuration will result in a malfunctioning email integration. Be very careful when editing this configuration. Make sure the new value is valid!")
	private static final String TENANT_ADMIN_NAME = "subsystem.emailintegration.email.tenant.admin.name";

	@ConfigurationPropertyDefinition(password = true, subSystem = "application", label = "WARNING: This configuration is mandatory for email integration and represents tenant admin account password used to log in. Deleting this configuration will result in a malfunctioning email integration. Be very careful when editing this configuration. Make sure the new value is valid!")
	private static final String TENANT_ADMIN_PASSWORD = "subsystem.emailintegration.email.tenant.admin.password"; // NOSONAR

	@ConfigurationGroupDefinition(properties = { ADMIN_NAME, ADMIN_PASSWORD }, type = User.class)
	private static final String ADMIN_ACCOUNT = "subsystem.emailintegration.admin.account";

	@ConfigurationGroupDefinition(properties = { TENANT_ADMIN_NAME, TENANT_ADMIN_PASSWORD }, type = User.class)
	private static final String TENANT_ADMIN_ACCOUNT = "subsystem.emailintegration.tenant.admin.account";

	@ConfigurationPropertyDefinition(defaultValue = "", system = true, subSystem = "application", label = "If provided, this suffix is appended to all generated emails in the system in order to guarantee the uniqueness of each generated email for each tenant")
	private static final String TEST_EMAIL_PREFIX = "subsystem.emailintegration.test.email.prefix";

	@Inject
	@Configuration(ADMIN_ACCOUNT)
	private ConfigurationProperty<User> adminAccount;

	@Inject
	@Configuration(TENANT_ADMIN_ACCOUNT)
	private ConfigurationProperty<User> tenantAdminAccount;

	@Inject
	@Configuration(TEST_EMAIL_PREFIX)
	private ConfigurationProperty<String> testEmailPrefix;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.webmail.protocol", defaultValue = "https", subSystem = "application", label = "Protocol used when accessing the webmail")
	private ConfigurationProperty<String> webmailProtocol;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.webmail.url", subSystem = "application", label = "WARNING: This configuration is mandatory for email integration and represents exposed web mail url. Deleting this configuration will result in a malfunctioning email integration. Be very careful when editing this configuration. Make sure the new value is valid!")
	private ConfigurationProperty<String> webmailUrl;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.webmail.port", subSystem = "application", label = "Exposed web mail port")
	private ConfigurationProperty<String> webmailPort;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.domain.address", system = true, subSystem = "application", label = "Email address domain name")
	private ConfigurationProperty<String> emailDomainAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.webmail.admin.port", system = true, defaultValue = "7071", subSystem = "application", label = "Exposed web mail admin port")
	private ConfigurationProperty<String> webmailAdminPort;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.tenant.domain.address", subSystem = "application", label = "WARNING: This configuration is mandatory for email integration and represents tenant email domain address. Deleting this configuration will result in a malfunctioning email integration. Be very careful when editing this configuration. Make sure the new value is valid!")
	private ConfigurationProperty<String> tenantDomainAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.tenant.classofservice", subSystem = "application", label = "Tenant specific class of service created")
	private ConfigurationProperty<String> tenantClassOfService;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.calendar.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra calendar feature enable toggle")
	private ConfigurationProperty<String> calendarEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.contacts.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra contacts feature enable toggle")
	private ConfigurationProperty<String> contactsEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.options.enabled", defaultValue = "TRUE", subSystem = "application", label = "Tenant specific zimbra options feature enable toggle")
	private ConfigurationProperty<String> optionsEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.tasks.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra tasks feature enable toggle")
	private ConfigurationProperty<String> tasksEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.groupcalendar.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra group calendar feature enable toggle")
	private ConfigurationProperty<String> groupCalendarEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.briefcase.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra briefcase feature enable toggle")
	private ConfigurationProperty<String> briefcaseEnabled;
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.tagging.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra tagging feature enable toggle")
	private ConfigurationProperty<String> taggingEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.savedsearch.enabled", defaultValue = "FALSE", subSystem = "application", label = "Tenant specific zimbra saved searches feature enable toggle")
	private ConfigurationProperty<String> savedSearchEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.mail.view", defaultValue = "message", subSystem = "application", label = "Tenant specific mail view configuration")
	private ConfigurationProperty<String> mailViewPreference;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.emailintegration.email.account.skin", defaultValue = "sep", subSystem = "application", label = "Tenant specific zimbra calendar feature enable toggle")
	private ConfigurationProperty<String> skin;

	@Inject
	private EmailAccountAdministrationService emailAccountAdministrationService;

	@Inject
	private AccountAuthenticationService accountAuthenticationService;

	@Inject
	private MailboxSupportableService mailboxSupportableService;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	@Inject
	private ConfigurationManagement configurationManagement;

	@Inject
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@ConfigurationConverter(ADMIN_ACCOUNT)
	User createAdminAccount(GroupConverterContext context) {
		getEmailDomainAddress().requireConfigured();
		context.getValue(ADMIN_NAME).requireConfigured();
		context.getValue(ADMIN_PASSWORD).requireConfigured();

		return new EmfUser(context.getValue(ADMIN_NAME).get().toString(),
				context.getValue(ADMIN_PASSWORD).get().toString());
	}

	@ConfigurationConverter(TENANT_ADMIN_ACCOUNT)
	User createTenantAdminAccount(GroupConverterContext context, SecurityContext securityContext)
			throws EmailAccountCreationException {
		// - If tenant admin configuration is missing, we should create it
		// - If the tenant admin account is not created yet, we should create one:
		// -- If users support mailboxes, then we can create the tenant admin account
		// --- If account (the email address) doesn't exists: generate address, create account, save address in the
		// emailaddress table
		// --- If account exists, we shouldn't try to create new one.
		// -- If users don't support mailboxes we shouldn't create admin account
		String tenantAdminName;
		String tenantAdminPass;
		if (!tenantAdminIsConfigured(context)) {
			Pair<String, String> configuration = createTenantAdminConfiguration(securityContext);
			tenantAdminName = configuration.getFirst();
			tenantAdminPass = configuration.getSecond();
			triggerCreateAccount(tenantAdminName, tenantAdminPass, securityContext);
		} else {
			tenantAdminName = context.getValue(TENANT_ADMIN_NAME).get().toString();
			tenantAdminPass = context.getValue(TENANT_ADMIN_PASSWORD).get().toString();
		}
		return new EmfUser(tenantAdminName, tenantAdminPass);
	}

	private Pair<String, String> createTenantAdminConfiguration(SecurityContext securityContext) {
		LOGGER.debug("Creating tenant email admin configuration");
		String currentTenantId = securityContext.getCurrentTenantId();
		StringBuilder username = new StringBuilder();
		username.append(TENANT_ADMIN_ACCOUNT_PREF);
		username.append(currentTenantId);
		String password = createPassword();

		com.sirma.itt.seip.configuration.db.Configuration tenantAdminName = new com.sirma.itt.seip.configuration.db.Configuration(
				TENANT_ADMIN_NAME, username.toString(), currentTenantId);
		com.sirma.itt.seip.configuration.db.Configuration tenantAdminPass = new com.sirma.itt.seip.configuration.db.Configuration(
				TENANT_ADMIN_PASSWORD, password, currentTenantId);
		configurationManagement.addConfigurations(Arrays.asList(tenantAdminName, tenantAdminPass));

		return new Pair<String, String>(username.toString(), password);
	}

	String createPassword() {
		return RandomStringUtils.randomAlphanumeric(8);
	}

	private void triggerCreateAccount(String tenantAdminName, String tenantAdminPass, SecurityContext securityContext) {
		String tenantAdminAddress = EmailIntegrationHelper.generateEmailAddress(tenantAdminName,
				getTenantDomainAddress().get(), getTestEmailPrefix().get());
		boolean emailAddressExists = emailAddressResolver.getEmailAddress(tenantAdminAddress) != null;
		boolean isMailboxSupportable = mailboxSupportableService.isMailboxSupportable(EMF.USER.toString());

		if (!emailAddressExists && isMailboxSupportable) {
			try {
				LOGGER.info("Creating tenant email admin account: {}", tenantAdminAddress);
				emailAccountAdministrationService.createTenantAdminAccount(tenantAdminAddress, tenantAdminPass);
				accountAuthenticationService.resetTenantAdminPort(tenantAdminName, tenantAdminPass);
				emailAddressResolver.insertEmailAddress(tenantAdminName, securityContext.getCurrentTenantId(),
						tenantAdminAddress, getTenantDomainAddress().get());
				shareFolderAdministrationService.createTenantShareFolder();
			} catch (EmailIntegrationException e) {
				throw new ConfigurationException("There's error during tenant admin accoun creation. " + e.getMessage(),
						e);
			}
		}
	}

	private static boolean tenantAdminIsConfigured(GroupConverterContext context) {
		return context.getValue(TENANT_ADMIN_NAME).isSet() && context.getValue(TENANT_ADMIN_PASSWORD).isSet();
	}

	@Override
	public ConfigurationProperty<String> getWebmailProtocol() {
		return webmailProtocol;
	}

	@Override
	public ConfigurationProperty<String> getWebmailUrl() {
		return webmailUrl;
	}

	@Override
	public ConfigurationProperty<String> getWebmailAdminPort() {
		return webmailAdminPort;
	}

	@Override
	public ConfigurationProperty<String> getWebmailPort() {
		return webmailPort;
	}

	@Override
	public ConfigurationProperty<String> getEmailDomainAddress() {
		return emailDomainAddress;
	}

	@Override
	public synchronized ConfigurationProperty<String> getTenantDomainAddress() {
		return tenantDomainAddress;
	}

	@Override
	public ConfigurationProperty<String> getTenantClassOfService() {
		return tenantClassOfService;
	}

	@Override
	public ConfigurationProperty<String> getTestEmailPrefix() {
		return testEmailPrefix;
	}

	@Override
	public ConfigurationProperty<User> getAdminAccount() {
		return adminAccount;
	}

	@Override
	public ConfigurationProperty<User> getTenantAdminAccount() {
		return tenantAdminAccount;
	}

	@Override
	public ConfigurationProperty<String> getCalendarEnabled() {
		return calendarEnabled;
	}

	@Override
	public ConfigurationProperty<String> getContactsEnabled() {
		return contactsEnabled;
	}

	@Override
	public ConfigurationProperty<String> getFeatureOptionsEnabled() {
		return optionsEnabled;
	}

	@Override
	public ConfigurationProperty<String> getFeatureTaskEnabled() {
		return tasksEnabled;
	}

	@Override
	public ConfigurationProperty<String> getGroupCalendarEnabled() {
		return groupCalendarEnabled;
	}

	@Override
	public ConfigurationProperty<String> getMailViewPreference() {
		return mailViewPreference;
	}

	@Override
	public ConfigurationProperty<String> getBriefcasesEnabled() {
		return briefcaseEnabled;
	}

	@Override
	public ConfigurationProperty<String> getSkin() {
		return skin;
	}

	@Override
	public ConfigurationProperty<String> getTaggingEnabled() {
		return taggingEnabled;
	}

	@Override
	public ConfigurationProperty<String> getSavedSearchesEnabled() {
		return savedSearchEnabled;
	}

}