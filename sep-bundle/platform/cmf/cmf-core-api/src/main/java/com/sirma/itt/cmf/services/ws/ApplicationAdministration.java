package com.sirma.itt.cmf.services.ws;

import javax.ws.rs.core.Response;

/**
 * Web service for administration of the internal services
 *
 * @author BBonev
 */
public interface ApplicationAdministration {

	/**
	 * Reload definitions.
	 */
	void reloadDefinitions();

	/**
	 * Reload templates.
	 */
	void reloadTemplates();

	/**
	 * Reload semantic definitions
	 */
	void reloadSemanticDefinitions();

	/**
	 * Reset codelists.
	 */
	void resetCodelists();

	/**
	 * Clear internal cache.
	 */
	void clearInternalCache();

	/**
	 * Gets the max revision definition as XML of given type by id. Supported type values are: case,
	 * workflow. All errors are returned as XML also.
	 *
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @return the definition
	 */
	String getDefinition(String type, String id);

	/**
	 * Clear definitions cache.
	 */
	void clearDefinitionsCache();

	/**
	 * Migrate definitions.
	 * 
	 * @param type
	 *            the type
	 * @param definitionIds
	 *            the definition ids
	 * @return the response
	 */
	Response migrateDefinitions(String type, String definitionIds);

	/**
	 * Migrate instances. The method loads the instances and saves them back.
	 * 
	 * @param type
	 *            the type
	 * @param ids
	 *            the ids
	 * @param threads
	 *            the threads
	 * @param batchSize
	 *            the batch size
	 * @param query
	 *            the query
	 * @param dialect
	 *            the dialect
	 * @return the response
	 */
	Response migrateInstances(String type, String ids, Integer threads, Integer batchSize,
			String query, String dialect);

}
