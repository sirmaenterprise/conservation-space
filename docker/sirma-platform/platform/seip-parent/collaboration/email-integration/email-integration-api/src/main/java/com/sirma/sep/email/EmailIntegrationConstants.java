package com.sirma.sep.email;

/**
 * Contains constants, used in email integration modules.
 *
 * @author S.Djulgerova
 */
public final class EmailIntegrationConstants {

	/**
	 * Statuses in which an email account could be found or put into.
	 *
	 * @author svelikov
	 */
	public enum EmailAccountStatus {

		// Active is the normal status for a mailbox account. Mail is delivered and users can log into the client
		// interface.
		ACTIVE("active"),
		// When a mailbox status is set to maintenance, login is disabled, and mail addressed to the account is queued
		// at the MTA. An account can be set to maintenance mode for backing up, importing or restoring the mailbox.
		MAINTENANCE("maintenance"),
		// When a mailbox status is locked, the user cannot log in, but mail is still delivered to the account. The
		// locked status can be set, if you suspect that a mail account has been hacked or is being used in an
		// unauthorized manner.
		LOCKED("locked"),
		// When a mailbox status is closed, the login is disabled, and messages are bounced. This status is used to
		// soft-delete an account before deleting it from the server.
		CLOSED("closed");

		private String status;

		EmailAccountStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

	}

	public static final String EMAIL_ADDRESS = "emailAddress";

	public static final String INSTANCE_ID = "instanceId";

	public static final String TENANT_ID = "tenantId";

	public static final String FIRST_NAME = "firstName";

	public static final String LAST_NAME = "lastName";

	public static final String FULL_NAME = "fullName";

	public static final String GIVEN_NAME = "givenName";

	public static final String DISPLAY_NAME = "displayName";

	public static final String CLASS_NAME = "className";

	public static final String SN = "sn";

	public static final String ZIMBRA_ACCOUNT_STATUS = "zimbraAccountStatus";

	public static final String EMAIL = "email";

	public static final String ATTR = "attributes";

	public static final String TENANT_ADMIN_ACCOUNT_PREF = "tenant-admin-";

	public static final String USER_FULL_URI = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User";

	// used to tell the respective queue how to dispatch requests for mailboxSupportable update
	public static final String ACTIVATE = "activate";

	private EmailIntegrationConstants() {
		// utility class
	}
}