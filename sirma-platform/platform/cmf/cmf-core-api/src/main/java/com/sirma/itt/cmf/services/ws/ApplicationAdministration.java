package com.sirma.itt.cmf.services.ws;

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
	 * Gets the max revision definition as XML of given type by id. Supported type values are: case, workflow. All
	 * errors are returned as XML also.
	 *
	 * @param id
	 *            the id
	 * @return the definition
	 */
	String getDefinition(String id);

	/**
	 * Clear definitions cache.
	 */
	void clearDefinitionsCache();
}
