/**
 * Copyright (c) 2014 29.05.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.rest;

/**
 * Rest client for "/service/emf/identity" rest services.
 * 
 * @author Adrian Mitev
 */
public class IdentityRestClient {

	/**
	 * Identifies an user within the system and returns an http session cookie.
	 * 
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return http cookie with for the user session.
	 */
	public String login(String url, String username, String password) {
		HttpResponse response = Rest.get(url + "/service/identity/login?username=" + username
				+ "&password=" + password, null);

		if (response.getStatus() != 200) {
			throw new IllegalStateException(response.getText());
		}

		return response.getCookie();
	}

}
