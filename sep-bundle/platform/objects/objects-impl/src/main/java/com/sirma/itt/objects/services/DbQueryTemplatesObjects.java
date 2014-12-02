package com.sirma.itt.objects.services;

/**
 * Holds all queries templates for local database access that are specific to the Objects module.
 * 
 * @author BBonev
 */
public interface DbQueryTemplatesObjects {

	/** The Constant QUERY_PROJECT_BY_DMS_ID_KEY. */
	String QUERY_OBJECT_BY_DMS_ID_KEY = "QUERY_OBJECT_BY_DMS_ID";
	/** The Constant QUERY_PROJECT_BY_DMS_ID. */
	// String QUERY_OBJECT_BY_DMS_ID =
	// "select p from ObjectEntity p where p.documentManagementId=:documentManagementId";

	/** The Constant QUERY_PROJECT_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_OBJECT_ENTITIES_BY_DMS_ID_KEY = "QUERY_OBJECT_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_PROJECT_ENTITIES_BY_DMS_ID. */
	// String QUERY_OBJECT_ENTITIES_BY_DMS_ID =
	// "select c from ObjectEntity c where c.documentManagementId in (:documentManagementId)";

	/** The Constant QUERY_PROJECT_ENTITIES_BY_ID_KEY. */
	String QUERY_OBJECT_ENTITIES_BY_ID_KEY = "QUERY_OBJECT_ENTITIES_BY_ID";
	/** The Constant QUERY_PROJECT_ENTITIES_BY_ID. */
	// String QUERY_OBJECT_ENTITIES_BY_ID = "select c from ObjectEntity c where c.id in (:id)";

	/** The Constant QUERY_ALL_PROJECT_ENTITY_IDS_KEY. */
	String QUERY_ALL_OBJECT_ENTITY_IDS_KEY = "QUERY_ALL_OBJECT_ENTITY_IDS";
	/** The Constant QUERY_ALL_PROJECT_ENTITY_IDS. */
	// String QUERY_ALL_OBJECT_ENTITY_IDS = "select p.id from ObjectEntity p order by p.id desc";

}
