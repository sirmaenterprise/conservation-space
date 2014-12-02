package com.sirma.itt.emf.solr.constants;

/**
 * The SolrQueryConstants holds the used constants in the solr module
 */
public interface SolrQueryConstants {
	/** return no results. */
	String QUERY_DEFAULT_EMPTY = "-*:*";
	/** return all results. */
	String QUERY_DEFAULT_ALL = "*:*";
	/** The field name instance id. */
	String FIELD_NAME_INSTANCE_ID = "uri";

	/** The field name instance type. */
	String FIELD_NAME_INSTANCE_TYPE = "instanceType";

	/** The field name identifier type. */
	String FIELD_NAME_IDENITIFIER_TYPE = "_sort_type";

	String FIELD_NAME_INSTANCE_SEMANTIC_TYPE = "rdfType";
}
