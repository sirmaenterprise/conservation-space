package com.sirma.itt.seip.resources.security;

import java.io.Serializable;

/**
 * Request for changing user's password
 * 
 * @author smustafov
 */
public class UserPasswordChangeRequest implements Serializable {

	private static final long serialVersionUID = 2501470506014906337L;

	private String username;
	private String oldPassword;
	private String newPassword;

	/**
	 * Getter method for username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Setter method for username.
	 *
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Getter method for oldPassword.
	 *
	 * @return the oldPassword
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * Setter method for oldPassword.
	 *
	 * @param oldPassword
	 *            the oldPassword to set
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	/**
	 * Getter method for newPassword.
	 *
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * Setter method for newPassword.
	 *
	 * @param newPassword
	 *            the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}
