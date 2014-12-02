package com.sirma.itt.cmf.services.adapter;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;

/**
 * The Interface CMFAuthenticationAdapter.
 * 
 * @author BBonev
 */
public interface CMFAuthenticationAdapter extends CMFAdapterService {

	/**
	 * Log in the given user and returns the authentication ticket.
	 * 
	 * @param userId
	 *            the user id
	 * @param password
	 *            the password
	 * @return the string
	 * @throws DMSException
	 *             the dMS exception
	 */
	String login(String userId, String password) throws DMSException;

	/**
	 * Log outs the given user from the subsystem
	 * 
	 * @param userId
	 *            the user id
	 * @param password
	 *            the password
	 * @param ticket
	 *            the ticket
	 * @throws DMSException
	 *             the dMS exception
	 */
	void logout(String userId, String password, String ticket) throws DMSException;
}
