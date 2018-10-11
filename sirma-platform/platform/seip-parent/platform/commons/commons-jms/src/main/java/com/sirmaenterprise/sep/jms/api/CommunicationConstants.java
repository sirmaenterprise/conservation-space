package com.sirmaenterprise.sep.jms.api;

/**
 * Common place for constants used in the module
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2017
 */
public final class CommunicationConstants {
	/**
	 * Key used to specify the tenant identifier in the messages what is the target tenant if any.
	 */
	public static final String TENANT_ID_KEY = "tenant_id";
	/**
	 * Key used to specify the originating request correlation id
	 */
	public static final String REQUEST_ID_KEY = "request_id";
	/**
	 * Key used to specify the authenticated user that send the service and that need to be initialized on received
	 * message
	 */
	public static final String AUTHENTICATED_USER_KEY = "authenticated_user";
	/**
	 * Key used to specify the effective authenticated user to be set when receiving messages.
	 */
	public static final String EFFECTIVE_USER_KEY = "effective_user";
	/**
	 * Key referring to large binary content in {@link javax.jms.Message}. Can be used to obtain a stream of the content.
	 */
	public static final String JMS_SAVE_STREAM = "JMS_HQ_SaveStream";

	private CommunicationConstants() {
		// utility class
	}
}
