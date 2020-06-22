package com.sirma.itt.seip.db;

import java.io.Serializable;

/**
 * Generator for database identifiers.
 *
 * @author BBonev
 */
public interface DbIdGenerator {

	/**
	 * Generate random identifier.
	 *
	 * @return the serializable
	 */
	Serializable generateId();

	/**
	 * Generate id that is specific for the given type. If not type is provided then the result will be the same as
	 * calling the {@link #generateId()} method.
	 *
	 * @param type
	 *            the type
	 * @return the serializable
	 */
	Serializable generateIdForType(String type);

	/**
	 * Checks if the given id is valid and if not modifies it and returns the valid identifier. If the given id is valid
	 * the same id will be returned.
	 *
	 * @param id
	 *            the id to check
	 * @return the valid id based on the argument
	 */
	Serializable getValidId(Serializable id);

	/**
	 * Generate revision id based on the given source id and revision number. The implementation could use the source
	 * information to form new id or to generate something completely new.
	 *
	 * @param src
	 *            the src
	 * @param revision
	 *            the revision
	 * @return the revision id
	 */
	Serializable generateRevisionId(Serializable src, String revision);
}
