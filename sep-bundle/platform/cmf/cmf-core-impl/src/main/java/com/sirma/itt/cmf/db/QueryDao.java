package com.sirma.itt.cmf.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Holder class for all prepared statements.
 */
@Entity
@Table(name = "emf_dataTypeDefinition")
@NamedQueries(value = {

		@NamedQuery(name = DbQueryTemplates.QUERY_WF_DEFINITIONS_FOR_MIGRATION_KEY, query = DbQueryTemplates.QUERY_WF_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DbQueryTemplates.QUERY_STANDALONE_TASK_DEFINITIONS_FOR_MIGRATION_KEY, query = DbQueryTemplates.QUERY_STANDALONE_TASK_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DbQueryTemplates.QUERY_CASE_DEFINITIONS_FOR_MIGRATION_KEY, query = DbQueryTemplates.QUERY_CASE_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_CASE_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_CASE_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_CASE_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_CASE_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_SECTION_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_SECTION_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_SECTION_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_SECTION_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_DOCUMENT_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_DOCUMENT_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_DOCUMENT_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_DOCUMENT_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_WORKFLOW_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_WORKFLOW_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_WORKFLOW_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_WORKFLOW_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_WF_TASK_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_WF_TASK_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_WF_TASK_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_WF_TASK_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_STANALONE_TASK_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_STANALONE_TASK_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_STANALONE_TASK_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_STANALONE_TASK_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION_KEY, query = DbQueryTemplates.QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ALL_FOLDER_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_ALL_FOLDER_DEFINITIONS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_FOLDER_DEFINITIONS_KEY, query = DbQueryTemplates.UPDATE_FOLDER_DEFINITIONS),

		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_IDS),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY, query = DbQueryTemplates.QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_BY_ID_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY, query = DbQueryTemplates.QUERY_PRIMARY_TEMPLATE_FOR_GROUP),
		@NamedQuery(name = DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER_KEY, query = DbQueryTemplates.QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER),

		@NamedQuery(name = DbQueryTemplates.QUERY_SECTIONS_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_SECTIONS_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_CASES_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_CASES_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_WORKFLOW_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_WORKFLOW_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_DOCUMENTS_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_DOCUMENTS_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY, query = DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY, query = DbQueryTemplates.QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER),
		@NamedQuery(name = DbQueryTemplates.QUERY_SECTION_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_SECTION_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_CASE_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_CASE_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_CASE_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_CASE_ENTITIES_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_CASE_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_CASE_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_DOCUMENT_ENTITY_BY_DMS_KEY, query = DbQueryTemplates.QUERY_DOCUMENT_ENTITY_BY_DMS),
		@NamedQuery(name = DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_DMS_KEY, query = DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_DMS),
		@NamedQuery(name = DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID_KEY, query = DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID_KEY, query = DbQueryTemplates.QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_WORKFLOW_HISTORY_KEY, query = DbQueryTemplates.QUERY_WORKFLOW_HISTORY),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE),
		@NamedQuery(name = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_KEY, query = DbQueryTemplates.QUERY_USERS_ON_INSTANCE),
		@NamedQuery(name = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY, query = DbQueryTemplates.DELETE_ASSIGNED_TASKS_BY_WF_ID),
		@NamedQuery(name = DbQueryTemplates.REMOVE_TASK_FROM_CONTEXT_KEY, query = DbQueryTemplates.REMOVE_TASK_FROM_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY, query = DbQueryTemplates.COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER),
		@NamedQuery(name = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_KEY, query = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_KEY, query = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS),
		@NamedQuery(name = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_KEY, query = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.ASSIGNED_TASKS_FOR_USER_KEY, query = DbQueryTemplates.ASSIGNED_TASKS_FOR_USER),

		@NamedQuery(name = DbQueryTemplates.QUERY_STASKS_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_STASKS_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_STASK_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_STASK_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_STASK_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_STASK_ENTITIES_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_STASK_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_STASK_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_KEY, query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY, query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_AND_STATE),

		@NamedQuery(name = DbQueryTemplates.QUERY_TASKS_BY_OWN_REF_KEY, query = DbQueryTemplates.QUERY_TASKS_BY_OWN_REF),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_ENTITIES_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_TASK_ENTITIES_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_ENTITIES_BY_ID_KEY, query = DbQueryTemplates.QUERY_TASK_ENTITIES_BY_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_DMS_ID_KEY, query = DbQueryTemplates.QUERY_TASK_BY_DMS_ID),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_STATE_KEY, query = DbQueryTemplates.QUERY_TASK_BY_STATE),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_ENTITY_BY_ID_AND_TYPE_KEY, query = DbQueryTemplates.QUERY_TASK_ENTITY_BY_ID_AND_TYPE),
		@NamedQuery(name = DbQueryTemplates.QUERY_ALL_TASKS_KEY, query = DbQueryTemplates.QUERY_ALL_TASKS) })
public class QueryDao {

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		// empty method. Nothing to set
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return 0L;
	}
}