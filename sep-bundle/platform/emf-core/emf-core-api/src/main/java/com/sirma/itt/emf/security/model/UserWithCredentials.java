package com.sirma.itt.emf.security.model;

/**
 * Provides credentials for the user
 *
 * @author BBonev
 */
public interface UserWithCredentials extends User {

	/**
	 * Gets the credentials.
	 *
	 * @return the credentials
	 */
	public Object getCredentials();

	/**
	 * Return the ticket
	 *
	 * @return ticket
	 */
	String getTicket();

	/**
	 * Sets the saml ticket
	 *
	 * @param saml
	 *            saml token
	 */
	void setTicket(String saml);

}
