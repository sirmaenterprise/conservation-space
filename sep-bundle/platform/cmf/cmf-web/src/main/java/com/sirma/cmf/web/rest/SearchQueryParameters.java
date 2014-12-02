package com.sirma.cmf.web.rest;

import com.sirma.itt.emf.web.rest.EmfQueryParameters;

/**
 * Constants for query parameters used in basic search.
 * 
 * @author svelikov
 */
public interface SearchQueryParameters extends EmfQueryParameters {

	public static final String OBJECT_TYPE = "objectType";
	public static final String OBJECT_RELATIONSHIP = "objectRelationship";
	public static final String LOCATION = "location";
	public static final String ORDER_BY = "orderBy";
	public static final String ORDER_DIRECTION = "orderDirection";
	public static final String CREATED_FROM_DATE = "createdFromDate";
	public static final String CREATED_TO_DATE = "createdToDate";
	public static final String CREATED_BY = "createdBy";
	public static final String META_TEXT = "metaText";

}
