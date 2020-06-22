package com.sirma.itt.emf.solr.schema;

import com.sirma.itt.emf.solr.schema.model.SolrSchemaModel;

/**
 * The Interface SolrSchemaPatch.
 */
public interface SolrSchemaPatch {

	/**
	 * Update the solr model.
	 *
	 * @return the schema model
	 */
	SolrSchemaModel updateModel();
}
