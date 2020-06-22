package com.sirma.itt.emf.solr.schema.model;

/**
 * Represents new SchemaEntry row in solr schema.xml or managed-schema.
 *
 * @author bbanchev
 */
public interface SchemaEntry {

	/**
	 * Builds the.
	 *
	 * @return the string
	 */
	String build();

	/**
	 * Gets the next entry id
	 *
	 * @return the id
	 */
	String getId();

	/**
	 * Clone property with specific mode
	 *
	 * @param mode
	 *            is the mode to use
	 * @return a new entry with this mode
	 */
	SchemaEntry createCopy();
}
