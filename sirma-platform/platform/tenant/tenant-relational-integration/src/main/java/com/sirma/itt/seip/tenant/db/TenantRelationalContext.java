/**
 *
 */
package com.sirma.itt.seip.tenant.db;

import java.net.URI;

/**
 * Represents a relational tenant data;
 *
 * @author BBonev
 */
public class TenantRelationalContext {

	/** The server address. */
	private URI serverAddress;
	/** The access user. */
	private String accessUser;
	/** The access user password. */
	private String accessUserPassword;
	/** The database name. */
	private String databaseName;
	/** The datasource name. */
	private String datasourceName;

	/**
	 * Instantiates a new tenant relational context.
	 */
	public TenantRelationalContext() {
		// default
	}

	/**
	 * Instantiates a new tenant relational context.
	 *
	 * @param serverAddress
	 *            the server address
	 * @param accessUser
	 *            the access user
	 * @param accessUserPassword
	 *            the access user password
	 * @param databaseName
	 *            the database name
	 * @param datasourceName
	 *            the datasource name
	 */
	public TenantRelationalContext(URI serverAddress, String accessUser, String accessUserPassword, String databaseName,
			String datasourceName) {
		this.serverAddress = serverAddress;
		this.accessUser = accessUser;
		this.accessUserPassword = accessUserPassword;
		this.databaseName = databaseName;
		this.datasourceName = datasourceName;
	}

	/**
	 * Gets the server address.
	 *
	 * @return the serverAddress
	 */
	public URI getServerAddress() {
		return serverAddress;
	}

	/**
	 * Sets the server address.
	 *
	 * @param serverAddress
	 *            the serverAddress to set
	 */
	public void setServerAddress(URI serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Gets the access user.
	 *
	 * @return the accessUser
	 */
	public String getAccessUser() {
		return accessUser;
	}

	/**
	 * Sets the access user.
	 *
	 * @param accessUser
	 *            the accessUser to set
	 */
	public void setAccessUser(String accessUser) {
		this.accessUser = accessUser;
	}

	/**
	 * Gets the access user password.
	 *
	 * @return the accessUserPassword
	 */
	public String getAccessUserPassword() {
		return accessUserPassword;
	}

	/**
	 * Sets the access user password.
	 *
	 * @param accessUserPassword
	 *            the accessUserPassword to set
	 */
	public void setAccessUserPassword(String accessUserPassword) {
		this.accessUserPassword = accessUserPassword;
	}

	/**
	 * Gets the database name.
	 *
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Sets the database name.
	 *
	 * @param databaseName
	 *            the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * Gets the datasource name.
	 *
	 * @return the datasourceName
	 */
	public String getDatasourceName() {
		return datasourceName;
	}

	/**
	 * Sets the datasource name.
	 *
	 * @param datasourceName
	 *            the datasourceName to set
	 */
	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

}
