package com.sirma.itt.seip.search;

/**
 * Constants for query parameters used in basic search.
 *
 * @author svelikov
 */
public interface SearchQueryParameters {

	// Basic search related constants
	String OBJECT_TYPE = "objectType[]";
	String SUB_TYPE = "subType[]";
	String OBJECT_RELATIONSHIP = "objectRelationship[]";
	String LOCATION = "location[]";
	String ORDER_BY = "orderBy";
	String ORDER_BY_CODELIST_NUMBERS = "orderByCodelistNumbers";
	String ORDER_DIRECTION = "orderDirection";
	String CREATED_FROM_DATE = "createdFromDate";
	String CREATED_TO_DATE = "createdToDate";
	String CREATED_BY = "createdBy[]";
	String META_TEXT = "metaText";
	String PAGE_NUMBER = "pageNumber";
	String PAGE_SIZE = "pageSize";
	String MAX_SIZE = "maxSize";
	String GROUP_BY = "groupBy";
	String SELECTED_OBJECTS = "selectedObjects";

	String FQ = "fq";
	String MIMETYPE = "mimetype";
	String IDENTIFIER = "identifier";

	// Definition query related constants
	String QUERY_FROM_DEFINITION = "definition";
	String QUERY_TEXT = "queryText";
	String QUERY_NAME = "queryName";
	String INSTANCE_TYPE = "type";
	String INSTANCE_ID = "instanceId";
	String FILTER_BY_PERMISSION = "filterByWritePermissions";
	String USER_URI = "userURI";
	String CURRENT_USER = "currentUser";

	// Context related constants
	String CONTEXT_URI = "contexturi";
	String AUTHORITIES = "userAuthorities";
	String OWNER = "owner";
	String ASSIGNEE = "assignee";
	String USER_ID = "userid";

	// Library related constants
	String LIBRARY = "library";
	String LIBRARY_TITLE = "libraryTitle";

}
