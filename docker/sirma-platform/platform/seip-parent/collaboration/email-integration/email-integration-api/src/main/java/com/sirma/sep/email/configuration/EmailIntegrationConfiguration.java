package com.sirma.sep.email.configuration;

import java.io.Serializable;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.resources.User;

/**
 * Expose the email integration subsystem configurations.
 *
 * @author S.Djulgerova
 */
public interface EmailIntegrationConfiguration extends Serializable {
	/**
	 * Gets the protocol that is used to access webmail url.
	 *
	 * @return webmail protocol.
	 */
	ConfigurationProperty<String> getWebmailProtocol();

	/**
	 * Gets the deployed webmail url.
	 *
	 * @return webmail url.
	 */
	ConfigurationProperty<String> getWebmailUrl();

	/**
	 * Gets the port which the web mail is configured to access the client.
	 *
	 * @return web mail port.
	 */
	ConfigurationProperty<String> getWebmailPort();

	/**
	 * Gets the port used to access the webmail admin client.
	 *
	 * @return web mail admin port.
	 */
	ConfigurationProperty<String> getWebmailAdminPort();

	/**
	 * Getter for email domain address configuration.
	 *
	 * @return the tenant domain
	 */
	ConfigurationProperty<String> getEmailDomainAddress();

	/**
	 * Getter for the tenant domain email domain address configuration.
	 *
	 * @return tenant domain address.
	 */
	ConfigurationProperty<String> getTenantDomainAddress();

	/**
	 * Gets the server test email prefix.
	 *
	 * @return the server test email prefix
	 */
	ConfigurationProperty<String> getTestEmailPrefix();

	/**
	 * Gets the server admin account.
	 *
	 * @return server admin
	 */
	public ConfigurationProperty<User> getAdminAccount();

	/**
	 * Gets tenant admin account.
	 *
	 * @return tenant admin
	 */
	public ConfigurationProperty<User> getTenantAdminAccount();

	/**
	 * Tenant specific configured class of service.
	 *
	 * @return tenant CoS name.
	 */
	public ConfigurationProperty<String> getTenantClassOfService();

	/**
	 * Gets calendar feature property. The calendar should be disabled, as it is not needed by the platform.
	 *
	 * @return calendarEnabled property
	 */
	ConfigurationProperty<String> getCalendarEnabled();

	/**
	 * Gets contacts feature property. The contacts property should be disabled, as it is not needed by the platform.
	 *
	 * @return contactsEnabled
	 */
	ConfigurationProperty<String> getContactsEnabled();

	/**
	 * Gets Options feature property. The feature options property should be disabled, as the user should not have
	 * customization control over the mail client.
	 *
	 * @return
	 */
	ConfigurationProperty<String> getFeatureOptionsEnabled();

	/**
	 * Gets Task feature property. The tasks property should be disabled, as it is an obsolete feature.
	 *
	 * @return taskEnabled
	 */
	ConfigurationProperty<String> getFeatureTaskEnabled();

	/**
	 * Gets group calendar feature property. The group calendar property should be disabled, as it is not needed and
	 * will not be consistent with the platform's look and feel.
	 *
	 * @return groupCalendarEnabled
	 */
	ConfigurationProperty<String> getGroupCalendarEnabled();

	/**
	 * Gets briefcase feature property. The briefcase property should be disabled, as it is an obsolete feature and it
	 * will not be consistent with the platform's look and feel.
	 *
	 * @return briefcaseEnabled
	 */
	ConfigurationProperty<String> getBriefcasesEnabled();

	/**
	 * Gets the Tagging feature property. Tagging is an obsolete feature that will not be consistent with the platform's
	 * look and feel.
	 *
	 * @return taggingEnabled
	 */
	ConfigurationProperty<String> getTaggingEnabled();

	/**
	 * Gets the saved searches feature
	 *
	 * @return savedSearches
	 */
	ConfigurationProperty<String> getSavedSearchesEnabled();

	/**
	 * Gets the mail preference view property. Switched to Message View instead of Conversation View.
	 *
	 * @return mailViewPreference property.
	 */
	ConfigurationProperty<String> getMailViewPreference();

	/**
	 * Gets the skin name to be set as default after account creation.
	 *
	 * @return skin that will be applied
	 */
	ConfigurationProperty<String> getSkin();
}
