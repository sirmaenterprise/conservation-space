package com.sirma.sep.email;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains common methods used in email integration modules.
 *
 * @author S.Djulgerova
 */
public final class EmailIntegrationHelper {

	private EmailIntegrationHelper() {
		// utility class
	}

	/**
	 * Generates email address by given account and domain names
	 *
	 * @param accountName
	 *            the account name
	 * @param domainName
	 *            predefined domain address
	 * @param suffix
	 *            an optional suffix that is added only in test mode in order to distinguish email addresses created
	 *            from different platform installations in single zimbra server
	 * @return generated email address
	 */
	public static String generateEmailAddress(String accountName, String domainName, String suffix) {
		StringBuilder emailAddress = new StringBuilder();
		emailAddress.append(accountName.replace('@', '-'));
		if (StringUtils.isNotBlank(suffix)) {
			emailAddress.append("-").append(suffix);
		}
		emailAddress.append("@");
		emailAddress.append(domainName);
		return emailAddress.toString();
	}

	/**
	 * Generates display name from list of properties
	 *
	 * @param properties
	 *            values participating in display name
	 * @return mailbox display name
	 */
	public static String generateDisplayName(Object... properties) {
		StringBuilder displayName = new StringBuilder();
		for (Object property : properties) {
			displayName.append(Objects.toString(property, ""));
			displayName.append(" ");
		}
		return displayName.toString();
	}
}