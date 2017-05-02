/**
 *
 */
package com.sirma.itt.seip.security.exception;

/**
 * Exception throw to identity a security authentication violation
 *
 * @author BBonev
 */
public class AuthenticationException extends SecurityException {
	private static final long serialVersionUID = 1873196228621384103L;
	private final String failedIdentity;
	private final String ssoSessionId;

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param message
	 *            the message
	 */
	public AuthenticationException(String message) {
		this(null, null, message);
	}

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public AuthenticationException(String message, Throwable causedBy) {
		this(null, null, message, causedBy);
	}

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param message
	 *            the message
	 */
	public AuthenticationException(String failedIdentity, String message) {
		super(message);
		this.failedIdentity = failedIdentity;
		ssoSessionId = null;
	}

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public AuthenticationException(String failedIdentity, String message, Throwable causedBy) {
		super(message, causedBy);
		this.failedIdentity = failedIdentity;
		ssoSessionId = null;
	}

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param ssoSessionId
	 *            the sso session id
	 * @param message
	 *            the message
	 */
	public AuthenticationException(String failedIdentity, String ssoSessionId, String message) {
		super(message);
		this.failedIdentity = failedIdentity;
		this.ssoSessionId = ssoSessionId;
	}

	/**
	 * Instantiates a new authentication exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param ssoSessionId
	 *            the sso session id
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public AuthenticationException(String failedIdentity, String ssoSessionId, String message, Throwable causedBy) {
		super(message, causedBy);
		this.failedIdentity = failedIdentity;
		this.ssoSessionId = ssoSessionId;
	}

	/**
	 * Gets the failed identity.
	 *
	 * @return the failed identity
	 */
	public String getFailedIdentity() {
		return failedIdentity;
	}

	/**
	 * Gets the sso session id if available
	 *
	 * @return the sso session id
	 */
	public String getSsoSessionId() {
		return ssoSessionId;
	}

}
