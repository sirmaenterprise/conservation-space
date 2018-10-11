package com.sirma.seip.semantic.management;

import java.net.URI;

/**
 * Repository information dto
 *
 * @author BBonev
 */
public class RepositoryInfo {

	private String repositoryName;
	private String userName;
	private String password;
	private URI address;

	/**
	 * Instantiates a new repository info.
	 */
	public RepositoryInfo() {
		// default constructor
	}

	/**
	 * Instantiates a new repository info.
	 *
	 * @param repositoryName
	 *            the repository name
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 * @param address
	 *            the address
	 */
	public RepositoryInfo(String repositoryName, String userName, String password, URI address) {
		this.repositoryName = repositoryName;
		this.userName = userName;
		this.password = password;
		this.address = address;
	}

	/**
	 * Gets the repository name.
	 *
	 * @return the repository name
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * Sets the repository name.
	 *
	 * @param repositoryName
	 *            the new repository name
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName
	 *            the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public URI getAddress() {
		return address;
	}

	/**
	 * Sets the address.
	 *
	 * @param address
	 *            the new address
	 */
	public void setAddress(URI address) {
		this.address = address;
	}
}