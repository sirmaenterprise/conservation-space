/**
 *
 */
package com.sirma.seip.semantic.management;

/**
 * Provides means of managing semantic repositories and full text search (Solr) connectors if supported
 *
 * @author BBonev
 */
public interface RepositoryManagement {

	/**
	 * Creates the repository.
	 *
	 * @param configuration
	 *            the configuration
	 */
	void createRepository(RepositoryConfiguration configuration);

	/**
	 * Delete repository.
	 *
	 * @param configuration
	 *            the configuration
	 */
	void deleteRepository(RepositoryInfo configuration);

	/**
	 * Creates the access user for repo.
	 *
	 * @param configuration
	 *            the configuration
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 */
	void createAccessUserForRepo(RepositoryInfo configuration, String userName, String password);

	/**
	 * Checks if is repository exists.
	 *
	 * @param configuration
	 *            the configuration
	 * @return true, if is repository exists
	 */
	boolean isRepositoryExists(RepositoryInfo configuration);
}
