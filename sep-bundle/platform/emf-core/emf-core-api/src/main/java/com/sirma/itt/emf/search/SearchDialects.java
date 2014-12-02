package com.sirma.itt.emf.search;

/**
 * Currently supported search dialects in the application.
 *
 * @author BBonev
 */
public interface SearchDialects {

	/** The DMS SOLR dialect. The queries are executed against the DMS solr server */
	String DMS_SOLR = "dmsSolr";

	/** The SOLR dialect. Solr queries that should be executed on the internal SOLR server (currently managed by OWLIM DB) */
	String SOLR = "solr";

	/** The SPARQL dialect. Semantic queries that need to be executed on the currently deployed semantic database. */
	String SPARQL = "sparql";
}
