package com.sirma.itt.emf.solr.constants;

/**
 * Holds the used constants in the solr module.
 */
public interface SolrQueryConstants {

	/** return no results. */
	String QUERY_DEFAULT_EMPTY = "-*:*";

	/** return all results. */
	String QUERY_DEFAULT_ALL = "*:*";

	/** Prefix for all sort fields */
	@Deprecated
	String FIELD_NAME_SORT_PREFIX = "_sort_";

	/** Prefix for all facet fields */
	String FIELD_NAME_FACET_PREFIX = "facet_";

	/** Used as wild card in some queries. */
	String WILD_CARD = "*";
}
