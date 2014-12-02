package com.sirma.itt.pm.services;

import com.sirma.itt.cmf.db.DbQueryTemplates;

/**
 * Holds all queries templates for local database access that are specific to the PM module.
 * 
 * @author BBonev
 */
public interface DbQueryTemplatesPm extends DbQueryTemplates {

	/** The Constant QUERY_PROJECT_BY_DMS_ID_KEY. */
	String QUERY_PROJECT_BY_DMS_ID_KEY = "QUERY_PROJECT_BY_DMS_ID";
	/** The Constant QUERY_PROJECT_BY_DMS_ID. */
	String QUERY_PROJECT_BY_DMS_ID = "select p from ProjectEntity p where p.documentManagementId=:documentManagementId";

	/** The Constant QUERY_PROJECT_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_PROJECT_ENTITIES_BY_DMS_ID_KEY = "QUERY_PROJECT_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_PROJECT_ENTITIES_BY_DMS_ID. */
	String QUERY_PROJECT_ENTITIES_BY_DMS_ID = "select c from ProjectEntity c where c.documentManagementId in (:documentManagementId)";

	/** The Constant QUERY_PROJECT_ENTITIES_BY_ID_KEY. */
	String QUERY_PROJECT_ENTITIES_BY_ID_KEY = "QUERY_PROJECT_ENTITIES_BY_ID";
	/** The Constant QUERY_PROJECT_ENTITIES_BY_ID. */
	String QUERY_PROJECT_ENTITIES_BY_ID = "select c from ProjectEntity c where c.id in (:id)";

	/** The Constant QUERY_ALL_PROJECT_ENTITY_IDS_KEY. */
	String QUERY_ALL_PROJECT_ENTITY_IDS_KEY = "QUERY_ALL_PROJECT_ENTITY_IDS";
	/** The Constant QUERY_ALL_PROJECT_ENTITY_IDS. */
	String QUERY_ALL_PROJECT_ENTITY_IDS = "select p.id from ProjectEntity p order by p.id desc";

	/*
	 * DEFINITION MIGRATION QUERIES
	 */
	String UPDATE_ALL_PROJECT_DEFINITIONS_KEY = "UPDATE_ALL_PROJECT_DEFINITIONS";
	String UPDATE_ALL_PROJECT_DEFINITIONS = "update ProjectEntity p set p.revision = (select max(d.revision) from DefinitionEntry d where p.definitionId = d.identifier and p.container=d.container and d.targetType.id=:type)";
	String UPDATE_PROJECT_DEFINITIONS_KEY = "UPDATE_PROJECT_DEFINITIONS";
	String UPDATE_PROJECT_DEFINITIONS = "update ProjectEntity p set p.revision = (select max(d.revision) from DefinitionEntry d where p.definitionId = d.identifier and p.container=d.container and d.targetType.id=:type) where p.definitionId in (:definitions)";

	String QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION";
	String QUERY_PROJECT_DEFINITIONS_FOR_MIGRATION = "select distinct c.definitionId from ProjectEntity c where c.revision <> (select max(d.revision) from DefinitionEntry d where c.definitionId = d.identifier and c.container=d.container and d.targetType.id=:type)";

}
