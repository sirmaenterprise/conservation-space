package com.sirma.itt.emf.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Retrieve test configuration of the test repository from configuration file
 *
 * @author kirq4e
 */
public class RepositoryConfiguration {

	public static final String REPOSITORY_ADDRESS = "repository.address";
	public static final String REPOSITORY_NAME = "repository.name";

	private Properties properties;

	/**
	 * Private constructor
	 */
	private RepositoryConfiguration() {

	}

	/**
	 * Gets instance of the configuration
	 *
	 * @return Instance of repository configuration
	 */
	public static RepositoryConfiguration getInstance() {
		RepositoryConfiguration configuration = new RepositoryConfiguration();
		configuration.initialize();
		return configuration;

	}

	/**
	 * Initializes repository properties
	 */
	private void initialize() {
		properties = new Properties();
		try {
			properties.load(
					RepositoryConfiguration.class.getClassLoader().getResourceAsStream("repository_config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the repository address from the configuration file
	 *
	 * @return Repository address
	 */
	public String getRepositoryAddress() {
		return (String) properties.get(REPOSITORY_ADDRESS);
	}

	/**
	 * Gets the repository name from the configuration file
	 *
	 * @return Repository name
	 */
	public String getRepositoryName() {
		return properties.getProperty(REPOSITORY_NAME);
	}

}
