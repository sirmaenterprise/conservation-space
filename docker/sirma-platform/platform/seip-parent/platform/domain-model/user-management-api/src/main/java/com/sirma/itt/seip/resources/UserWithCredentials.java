package com.sirma.itt.seip.resources;

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
	@Override
	Object getCredentials();

	/**
	 * Return the ticket
	 *
	 * @return ticket
	 */
	@Override
	String getTicket();

	/**
	 * Sets the saml ticket
	 *
	 * @param saml
	 *            saml token
	 */
	void setTicket(String saml);

}
