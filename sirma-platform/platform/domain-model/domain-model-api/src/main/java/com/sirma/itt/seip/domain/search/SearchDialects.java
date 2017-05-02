package com.sirma.itt.seip.domain.search;

/**
 * Currently supported search dialects in the application.
 *
 * @author BBonev
 */
public interface SearchDialects {

	/**
	 * The SOLR dialect. Solr queries that should be executed on the internal SOLR server (currently managed by OWLIM
	 * DB)
	 */
	String SOLR = "solr";

	/** The SPARQL dialect. Semantic queries that need to be executed on the currently deployed semantic database. */
	String SPARQL = "sparql";

	/** The relational database dialect. Used to allow execution of relational database queries. */
	String RELATIONAL = "rdb";
}
