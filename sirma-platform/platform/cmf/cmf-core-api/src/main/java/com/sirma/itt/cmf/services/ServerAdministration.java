package com.sirma.itt.cmf.services;

/**
 * Provides methods for managing the internal state of the CMF server and synchronizing it with the external sub
 * systems.
 */
public interface ServerAdministration {

	/** The service name. */
	String SERVICE_NAME = "ServerConfiguration";

	/**
	 * Refresh case definitions from DMS subsystem. If a differences are found in the last revisions of the particular
	 * case definition then the revision will be increased and definition will be persisted. If no changes are found
	 * then database is not modified. The method effectively updates the case definition cache.
	 * <P>
	 * Refresh workflow definitions from DMS subsystem.The method effectively updates the workflow definition cache.
	 */
	void refreshDefinitions();

	/**
	 * Refresh type definitions from DMS subsystem. The method effectively updates the type definition cache.
	 */
	void refreshTypeDefinitions();

	/**
	 * Insert base definition types that will handle saving properties without definition.
	 */
	void insertBaseDefinitions();

	/**
	 * Reset codelists cache and forces the application to fetch all codelists from the server.
	 */
	void resetCodelists();
}