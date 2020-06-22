package com.sirma.itt.emf.solr.schema.model;

/**
 * The SchemaOperation represents the mode of current schema - readonly, or update, etc..
 *
 * @author bbanchev
 */
enum SolrSchemaMode {

	/** The add. */
	ADD, /** The delete. */
	DELETE, /** The replace. */
	REPLACE, /** The readonly. */
	READONLY;
}
