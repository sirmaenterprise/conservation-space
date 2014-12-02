package com.sirma.itt.cmf.db;


/**
 * Holds all queries templates for local database access.
 *
 * @author BBonev
 */
public interface DbQueryTemplates {

	/*
	 * DICTIONARY QUERIES
	 */

	/*
	 * INSTANCE QUERIES
	 */
	/** The Constant QUERY_CASE_BY_DMS_ID_KEY. */
	String QUERY_CASE_BY_DMS_ID_KEY = "QUERY_CASE_BY_DMS_ID";
	/** The Constant QUERY_CASE_BY_DMS_ID. */
	String QUERY_CASE_BY_DMS_ID = "select c from CaseEntity c where c.documentManagementId=:documentManagementId";

	/** The Constant QUERY_CASE_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_CASE_ENTITIES_BY_DMS_ID_KEY = "QUERY_CASE_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_CASE_ENTITIES_BY_DMS_ID. */
	String QUERY_CASE_ENTITIES_BY_DMS_ID = "select c from CaseEntity c where c.documentManagementId in (:documentManagementId)";

	/** The Constant QUERY_CASE_ENTITIES_BY_ID_KEY. */
	String QUERY_CASE_ENTITIES_BY_ID_KEY = "QUERY_CASE_ENTITIES_BY_ID";
	/** The Constant QUERY_CASE_ENTITIES_BY_ID. */
	String QUERY_CASE_ENTITIES_BY_ID = "select c from CaseEntity c where c.id in (:id)";

	/** The Constant QUERY_CASES_BY_OWN_REF_KEY. */
	String QUERY_CASES_BY_OWN_REF_KEY = "QUERY_CASES_BY_OWN_REF";
	/** The Constant QUERY_CASES_BY_OWN_REF. */
	String QUERY_CASES_BY_OWN_REF = "select t.id from CaseEntity t where t.owningInstance.sourceId=:sourceId AND t.owningInstance.sourceType.id = :sourceType";

	/** The Constant QUERY_CASES_BY_OWN_REF_KEY. */
	String QUERY_WORKFLOW_BY_OWN_REF_KEY = "QUERY_WORKFLOW_BY_OWN_REF_KEY";
	/** The Constant QUERY_CASES_BY_OWN_REF. */
	String QUERY_WORKFLOW_BY_OWN_REF = "select t.id from WorkflowInstanceContextEntity t where t.owningReference.sourceId=:sourceId AND t.owningReference.sourceType.id = :sourceType";

	/** The Constant QUERY_SECTIONS_BY_OWN_REF_KEY. */
	String QUERY_SECTIONS_BY_OWN_REF_KEY = "QUERY_SECTIONS_BY_OWN_REF_KEY";
	/** The Constant QUERY_SECTIONS_BY_OWN_REF. */
	String QUERY_SECTIONS_BY_OWN_REF = "select t.id from SectionEntity t where t.owningInstance.sourceId=:sourceId AND t.owningInstance.sourceType.id = :sourceType";

	/** The Constant QUERY_SECTION_ENTITIES_BY_ID_KEY. */
	String QUERY_SECTION_ENTITIES_BY_ID_KEY = "QUERY_SECTION_ENTITIES_BY_ID";
	/** The Constant QUERY_SECTION_ENTITIES_BY_ID. */
	String QUERY_SECTION_ENTITIES_BY_ID = "select c from SectionEntity c where c.id in (:id)";

	/** The Constant QUERY_SECTION_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_SECTION_ENTITIES_BY_DMS_ID_KEY = "QUERY_SECTION_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_SECTION_ENTITIES_BY_DMS_ID. */
	String QUERY_SECTION_ENTITIES_BY_DMS_ID = "select c from SectionEntity c where c.documentManagementId in (:documentManagementId)";

	/** The Constant QUERY_SECTION_BY_DMS_ID_KEY. */
	String QUERY_SECTION_BY_DMS_ID_KEY = "QUERY_SECTION_BY_DMS_ID";
	/** The Constant QUERY_SECTION_BY_DMS_ID. */
	String QUERY_SECTION_BY_DMS_ID = "select c from SectionEntity c where c.documentManagementId=:documentManagementId";

	/** The Constant QUERY_DOCUMENT_ENTITY_BY_DMS_KEY. */
	String QUERY_DOCUMENT_ENTITIES_BY_ID_KEY = "QUERY_DOCUMENT_ENTITIES_BY_ID";
	/** The Constant QUERY_DOCUMENT_ENTITY_BY_DMS. */
	String QUERY_DOCUMENT_ENTITIES_BY_ID = "select d from DocumentEntity d where d.id in (:id)";

	/** The Constant QUERY_DOCUMENT_ENTITY_BY_DMS_KEY. */
	String QUERY_DOCUMENT_ENTITY_BY_DMS_KEY = "QUERY_DOCUMENT_ENTITY_BY_DMS";
	/** The Constant QUERY_DOCUMENT_ENTITY_BY_DMS. */
	String QUERY_DOCUMENT_ENTITY_BY_DMS = "select d from DocumentEntity d where d.documentDmsId=:documentDmsId";

	/** The Constant QUERY_DOCUMENT_ENTITIES_BY_DMS_KEY. */
	String QUERY_DOCUMENT_ENTITIES_BY_DMS_KEY = "QUERY_DOCUMENT_ENTITIES_BY_DMS";
	/** The Constant QUERY_DOCUMENT_ENTITIES_BY_DMS. */
	String QUERY_DOCUMENT_ENTITIES_BY_DMS = "select d from DocumentEntity d where d.documentDmsId in (:documentDmsId)";

	/** The Constant QUERY_DOCUMENTS_BY_OWN_REF_KEY. */
	String QUERY_DOCUMENTS_BY_OWN_REF_KEY = "QUERY_DOCUMENTS_BY_OWN_REF_KEY";
	/** The Constant QUERY_DOCUMENTS_BY_OWN_REF. */
	String QUERY_DOCUMENTS_BY_OWN_REF = "select t.id from DocumentEntity t where t.owningReference.sourceId=:sourceId AND t.owningReference.sourceType.id = :sourceType";

	/** The Constant QUERY_DRAFT_INSTANCES_BY_ENTITY_ID finds drafts for given instance. */
	String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_KEY = "QUERY_DRAFT_INSTANCES_BY_ENTITY_ID";
	String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID = "select d from DraftEntity d where d.id.instanceId=:uri";
	/** The Constant QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER finds drafts for given instance and user. */
	String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER_KEY = "QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER";
	String QUERY_DRAFT_INSTANCES_BY_ENTITY_ID_AND_USER = "select d from DraftEntity d where d.id.instanceId=:uri AND d.id.userId=:userId";

	/*
	 * =====================================================================
	 */
	/** The Constant QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID_KEY. */
	String QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID_KEY = "QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID";

	/** The Constant QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID. */
	String QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID = "select w from WorkflowInstanceContextEntity w where w.workflowInstanceId=:workflowInstanceId";

	/** The Constant QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID_KEY. */
	String QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID_KEY = "QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID";

	/** The Constant QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID. */
	String QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID = "select w from WorkflowInstanceContextEntity w where w.workflowInstanceId in (:workflowInstanceId)";

	/** The Constant QUERY_WORKFLOW_ENTITIES_BY_ID_KEY. */
	String QUERY_WORKFLOW_ENTITIES_BY_ID_KEY = "QUERY_WORKFLOW_ENTITIES_BY_ID";

	/** The Constant QUERY_WORKFLOW_ENTITIES_BY_ID. */
	String QUERY_WORKFLOW_ENTITIES_BY_ID = "select w from WorkflowInstanceContextEntity w where w.id in (:id)";

	/** The Constant QUERY_WORKFLOW_HISTORY_KEY. */
	String QUERY_WORKFLOW_HISTORY_KEY = "QUERY_WORKFLOW_HISTORY";

	/** The Constant QUERY_WORKFLOW_HISTORY. */
	String QUERY_WORKFLOW_HISTORY = "select w.workflowInstanceId FROM WorkflowInstanceContextEntity w where w.owningReference.sourceId=:sourceId AND w.owningReference.sourceType.id=:sourceType";

	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY = "DELETE_ASSIGNED_TASKS_BY_WF_ID";

	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID. */
	String DELETE_ASSIGNED_TASKS_BY_WF_ID = "delete from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType";

	/** The Constant REMOVE_TASK_FROM_CONTEXT_KEY. */
	String REMOVE_TASK_FROM_CONTEXT_KEY = "REMOVE_TASK_FROM_CONTEXT";

	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID. */
	String REMOVE_TASK_FROM_CONTEXT = "delete from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType AND a.taskInstanceId in (:taskInstanceId)";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS_KEY = "QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS = "select a.taskInstanceId from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType and a.active=:active";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_KEY = "QUERY_ASSIGNED_TASKS_BY_CONTEXT";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT = "select a.taskInstanceId from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS_KEY = "QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS = "select a.taskInstanceId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.active=:active and a.taskType in (:taskType)";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_KEY = "QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE = "select a.taskInstanceId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.taskType in (:taskType)";

	/** The Constant QUERY_USERS_ON_INSTANCE_KEY. */
	String QUERY_USERS_ON_INSTANCE_KEY = "QUERY_USERS_ON_INSTANCE";
	/** The Constant QUERY_USERS_ON_INSTANCE. */
	String QUERY_USERS_ON_INSTANCE = "select distinct a.userId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.userId is not null";

	/** The Constant QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY. */
	String QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY = "QUERY_USERS_ON_INSTANCE_AND_STATUS";
	/** The Constant QUERY_USERS_ON_INSTANCE_AND_STATUS. */
	String QUERY_USERS_ON_INSTANCE_AND_STATUS = "select distinct a.userId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and active=:active and a.userId is not null";

	/** The Constant COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY. */
	String COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY = "COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER";
	/** The Constant COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER. */
	String COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER = "select count(*) from AssignedUserTasks a where a.userId=:userId and a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.active=:active";

	/** The Constant ASSIGNED_TASKS_FOR_USER_KEY access the query for obtaining user tasks. */
	String ASSIGNED_TASKS_FOR_USER_KEY = "ASSIGNED_TASKS_FOR_USER";
	/** The Constant ASSIGNED_TASKS_FOR_USER. */
	String ASSIGNED_TASKS_FOR_USER = "select a.owningInstance from AssignedUserTasks a where a.userId=:userId and a.owningInstance.sourceType.id=:sourceType";

	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_KEY. */
	String UPDATE_ASSIGNED_TASK_STATUS_KEY = "UPDATE_ASSIGNED_TASK_STATUS";
	/** The Constant UPDATE_ASSIGNED_TASK_STATUS. */
	String UPDATE_ASSIGNED_TASK_STATUS = "update AssignedUserTasks a set a.active=:active, a.userId=:userId  where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType AND a.contextReference.sourceId=:contextSourceId AND a.contextReference.sourceType.id=:contextSourceType AND a.taskInstanceId=:taskInstanceId";

	/** The key for {@link #UNSSIGNED_TASKS_FOR_CONTEXT} */
	String UNSSIGNED_TASKS_FOR_CONTEXT_KEY = "UNSSIGNED_TASKS_FOR_CONTEXT";
	/** The query for UNSSIGNED_TASKS_FOR_CONTEXT_KEY. */
	String UNSSIGNED_TASKS_FOR_CONTEXT = "select a from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.userId is null";
	/** The key for {@link #UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS} */
	String UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS_KEY = "UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS";
	/** The query for UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS_KEY. */
	String UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS = "select a from AssignedUserTasks a where a.active=:active and a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.userId is null";
	/** Find pool resources grouped by tasks for given context and status. */
	String POOL_TASKS_FOR_CONTEXT_AND_STATUS_KEY = "POOL_TASKS_FOR_CONTEXT_AND_STATUS";
	/** The query for POOL_TASKS_FOR_CONTEXT_AND_STATUS_KEY. */
	String POOL_TASKS_FOR_CONTEXT_AND_STATUS = "select a from AssignedUserTasks a where a.active=:active and a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and (a.poolUsers is not null or a.poolGroups is not null)";
	/** Find pool resources grouped by tasks for given context */
	String POOL_TASKS_FOR_CONTEXT_KEY = "POOL_TASKS_FOR_CONTEXT";
	/** The query for POOL_TASKS_FOR_CONTEXT_KEY. */
	String POOL_TASKS_FOR_CONTEXT = "select a from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and (a.poolUsers is not null or a.poolGroups is not null)";

	/*
	 * WORKFLOW TASKS
	 */

	/** The Constant QUERY_TASK_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_TASK_ENTITIES_BY_DMS_ID_KEY = "QUERY_TASK_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_TASK_ENTITIES_BY_DMS_ID. */
	String QUERY_TASK_ENTITIES_BY_DMS_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.documentManagementId in (:documentManagementId) AND t.workflowInstanceId is NOT null";

	/** The Constant QUERY_TASK_ENTITIES_BY_ID_KEY. */
	String QUERY_TASK_ENTITIES_BY_ID_KEY = "QUERY_TASK_ENTITIES_BY_ID";
	/** The Constant QUERY_TASK_ENTITIES_BY_ID. */
	String QUERY_TASK_ENTITIES_BY_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.id in (:id) AND t.workflowInstanceId is NOT null";

	/** The Constant QUERY_TASK_BY_DMS_ID_KEY. */
	String QUERY_TASK_BY_DMS_ID_KEY = "QUERY_TASK_BY_DMS_ID";
	/** The Constant QUERY_TASK_BY_DMS_ID. */
	String QUERY_TASK_BY_DMS_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.documentManagementId=:documentManagementId AND t.workflowInstanceId is NOT null";

	/** The Constant QUERY_TASK_BY_STATE_KEY. */
	String QUERY_TASK_BY_STATE_KEY = "QUERY_TASK_BY_STATE";
	/** The Constant QUERY_TASK_BY_STATE. */
	String QUERY_TASK_BY_STATE = "select t.id from TaskEntity t where t.workflowInstanceId=:workflowInstanceId AND t.state=:state";

	/** The Constant QUERY_ALL_TASKS_KEY. */
	String QUERY_ALL_TASKS_KEY = "QUERY_ALL_TASKS";
	/** The Constant QUERY_ALL_TASKS. */
	String QUERY_ALL_TASKS = "select t.id from TaskEntity t where t.workflowInstanceId=:workflowInstanceId";

	/** The Constant QUERY_ALL_TASKS_KEY. */
	String QUERY_TASKS_BY_OWN_REF_KEY = "QUERY_TASKS_BY_OWN_REF";
	/** The Constant QUERY_ALL_TASKS. */
	String QUERY_TASKS_BY_OWN_REF = "select t.id from TaskEntity t where t.owningInstance.sourceId=:sourceId AND t.owningInstance.sourceType.id = :sourceType";

	/** The Constant QUERY_TASK_ENTITY_BY_ID_AND_TYPE_KEY. */
	String QUERY_TASK_ENTITY_BY_ID_AND_TYPE_KEY = "QUERY_TASK_ENTITY_BY_ID_AND_TYPE";
	/** The Constant QUERY_TASK_ENTITY_BY_ID_AND_TYPE. */
	String QUERY_TASK_ENTITY_BY_ID_AND_TYPE = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.id=:id AND t.taskType=:taskType";

	/*
	 * STANDALONE TASKS
	 */

	/** The Constant QUERY_ALL_TASKS_KEY. */
	String QUERY_STASKS_BY_OWN_REF_KEY = "QUERY_STASKS_BY_OWN_REF";
	/** The Constant QUERY_ALL_TASKS. */
	String QUERY_STASKS_BY_OWN_REF = "select t.id from TaskEntity t where t.owningInstance.sourceId=:sourceId AND t.owningInstance.sourceType.id=:sourceType AND t.taskType=:taskType";

	/** The Constant QUERY_TASK_ENTITIES_BY_ID_KEY. */
	String QUERY_STASK_ENTITIES_BY_ID_KEY = "QUERY_STASK_ENTITIES_BY_ID";
	/** The Constant QUERY_TASK_ENTITIES_BY_ID. */
	String QUERY_STASK_ENTITIES_BY_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.id in (:id) AND t.taskType=:taskType";

	/** The Constant QUERY_TASK_ENTITIES_BY_DMS_ID_KEY. */
	String QUERY_STASK_ENTITIES_BY_DMS_ID_KEY = "QUERY_STASK_ENTITIES_BY_DMS_ID";
	/** The Constant QUERY_TASK_ENTITIES_BY_DMS_ID. */
	String QUERY_STASK_ENTITIES_BY_DMS_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.documentManagementId in (:documentManagementId) AND t.taskType=:taskType";

	/** The Constant QUERY_TASK_BY_DMS_ID_KEY. */
	String QUERY_STASK_BY_DMS_ID_KEY = "QUERY_STASK_BY_DMS_ID";
	/** The Constant QUERY_TASK_BY_DMS_ID. */
	String QUERY_STASK_BY_DMS_ID = "select t from TaskEntity t left join fetch t.owningInstance.sourceType where t.documentManagementId=:documentManagementId AND t.taskType=:taskType";

	/** The Constant QUERY_TASK_BY_PARENT_PATH_KEY. */
	String QUERY_TASK_BY_PARENT_PATH_KEY = "QUERY_TASK_BY_PARENT_PATH";
	/** The Constant QUERY_TASK_BY_PARENT_PATH. */
	String QUERY_TASK_BY_PARENT_PATH = "select t.id from TaskEntity t where t.treePath like :treePath AND t.taskType=:taskType and t.id<>:id";

	/** The Constant QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY. */
	String QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY = "QUERY_TASK_BY_PARENT_PATH_AND_STATE";
	/** The Constant QUERY_TASK_BY_PARENT_PATH_AND_STATE. */
	String QUERY_TASK_BY_PARENT_PATH_AND_STATE = "select t.id from TaskEntity t where t.treePath like :treePath AND t.taskType=:taskType and t.state=:state and t.id<>:id";

	/*
	 * TEMPLATES
	 */

	/** The Constant QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY. */
	String QUERY_TEMPLATES_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_BY_TEMPLATE_IDS";
	/** The Constant QUERY_TEMPLATES_BY_TEMPLATE_IDS. */
	String QUERY_TEMPLATES_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId in (:templateId)";

	/** The Constant QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY. */
	String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS_KEY = "QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE";
	/** The Constant QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE. */
	String QUERY_TEMPLATES_NOT_IN_BY_TEMPLATE_IDS = "select t from TemplateEntity t where t.templateId not in (:templateId)";

	/** The Constant QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY. */
	String QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS_KEY = "QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS";
	/** The Constant QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS. */
	String QUERY_TEMPLATES_NOT_IN_BY_DMS_IDS = "select t from TemplateEntity t where t.dmsId not in (:dmsId)";

	/** The Constant QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY. */
	String QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID_KEY = "QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID";
	/** The Constant QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID. */
	String QUERY_TEMPLATE_BY_TEMPLATE_OR_DMS_ID = "select t from TemplateEntity t where t.templateId = :templateId or t.dmsId = :templateId";

	/** The Constant QUERY_TEMPLATES_BY_ID_KEY. */
	String QUERY_TEMPLATES_BY_ID_KEY = "QUERY_TEMPLATES_BY_ID";
	/** The Constant QUERY_TEMPLATES_BY_ID. */
	String QUERY_TEMPLATES_BY_ID = "select t from TemplateEntity t where t.id in (:ids)";

	/** The Constant QUERY_TEMPLATES_FOR_USER_KEY. */
	String QUERY_TEMPLATES_FOR_USER_KEY = "QUERY_TEMPLATES_FOR_USER";
	/** The Constant QUERY_TEMPLATES_FOR_USER. */
	String QUERY_TEMPLATES_FOR_USER = "select t.id from TemplateEntity t where t.visibleTo is null OR t.visibleTo in (:users)";

	/** The Constant QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY. */
	String QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID_KEY = "QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID";
	/** The Constant QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID. */
	String QUERY_TEMPLATES_FOR_USER_AND_GROUP_ID = "select t.id from TemplateEntity t where t.groupId=:groupId";

	/** The Constant QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY. */
	String QUERY_PRIMARY_TEMPLATE_FOR_GROUP_KEY = "QUERY_PRIMARY_TEMPLATE_FOR_GROUP";
	/** The Constant QUERY_PRIMARY_TEMPLATE_FOR_GROUP. */
	String QUERY_PRIMARY_TEMPLATE_FOR_GROUP = "select t.id from TemplateEntity t where t.primary=1 AND t.groupId=:groupId";

	/** The Constant QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER_KEY. */
	String QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER_KEY = "QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER";
	/** The Constant QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER. */
	String QUERY_TEMPLATES_BY_TEMPLATE_CONTAINER = "select t.id from TemplateEntity t where t.templateId=:templateId AND t.container=:container";

	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_KEY. */
	String UPDATE_MISSING_TEMPLATE_DEFAULT_KEY = "UPDATE_MISSING_TEMPLATE_DEFAULT";
	/** The Constant UPDATE_ASSIGNED_TASK_STATUS. */
	String UPDATE_MISSING_TEMPLATE_DEFAULT = "update TemplateEntity a set a.primary= 1 where a.id in (select t.id from TemplateEntity t where t.groupId=a.groupId and t.container=a.container having t.primary = 2) and a.primary = 2";

	/*
	 * DEFINITION MIGRATION QUERIES
	 */

	String QUERY_CASE_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_CASE_DEFINITIONS_FOR_MIGRATION";
	String QUERY_CASE_DEFINITIONS_FOR_MIGRATION = "select distinct c.caseDefinitionId from CaseEntity c where c.caseRevision <> (select max(d.revision) from DefinitionEntry d where c.caseDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type)";

	String QUERY_WF_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_WF_DEFINITIONS_FOR_MIGRATION";
	String QUERY_WF_DEFINITIONS_FOR_MIGRATION = "select distinct c.workflowDefinitionId from WorkflowInstanceContextEntity c where c.revision <> (select max(d.revision) from DefinitionEntry d where c.workflowDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type)";

	String QUERY_STANDALONE_TASK_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_STANDALONE_TASK_DEFINITIONS_FOR_MIGRATION";
	String QUERY_STANDALONE_TASK_DEFINITIONS_FOR_MIGRATION = "select distinct c.definitionId from TaskEntity c where c.revision <> (select max(d.revision) from DefinitionEntry d where c.definitionId = d.identifier and c.container=d.container and d.targetType.id=:type) AND c.taskType=1";

	String UPDATE_ALL_CASE_DEFINITIONS_KEY = "UPDATE_ALL_CASE_DEFINITIONS";
	String UPDATE_ALL_CASE_DEFINITIONS = "update CaseEntity c set c.caseRevision = (select max(d.revision) from DefinitionEntry d where c.caseDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type)";
	String UPDATE_CASE_DEFINITIONS_KEY = "UPDATE_CASE_DEFINITIONS";
	String UPDATE_CASE_DEFINITIONS = "update CaseEntity c set c.caseRevision = (select max(d.revision) from DefinitionEntry d where c.caseDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type) where c.caseDefinitionId in (:definitions)";

	String UPDATE_ALL_SECTION_DEFINITIONS_KEY = "UPDATE_ALL_SECTION_DEFINITIONS";
	String UPDATE_ALL_SECTION_DEFINITIONS = "update SectionEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = substring(s.definitionPath, 0, locate('/', s.definitionPath)) and s.container=d.container and d.targetType.id=:type) where s.owningInstance.sourceType.id = (select d.id from DataType d where d.name='caseinstance')";
	String UPDATE_SECTION_DEFINITIONS_KEY = "UPDATE_SECTION_DEFINITIONS";
	String UPDATE_SECTION_DEFINITIONS = "update SectionEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = substring(s.definitionPath, 0, locate('/', s.definitionPath)) and s.container=d.container and d.targetType.id=:type) where s.owningInstance.sourceType.id = (select d.id from DataType d where d.name='caseinstance') AND substring(s.definitionPath, 0, locate('/', s.definitionPath)) in (:definitions)";

	String QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION";
	String QUERY_FOLDER_DEFINITIONS_FOR_MIGRATION = "select distinct c.sectionId from SectionEntity c where c.standalone=1 AND c.revision <> (select max(d.revision) from DefinitionEntry d where c.sectionId = d.identifier and c.container=d.container and d.targetType.id=:type)";

	String UPDATE_ALL_FOLDER_DEFINITIONS_KEY = "UPDATE_ALL_FOLDER_DEFINITIONS";
	String UPDATE_ALL_FOLDER_DEFINITIONS = "update SectionEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.definitionPath and s.container=d.container and d.targetType.id=:type) where s.standalone=1";
	String UPDATE_FOLDER_DEFINITIONS_KEY = "UPDATE_FOLDER_DEFINITIONS";
	String UPDATE_FOLDER_DEFINITIONS = "update SectionEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.definitionPath and s.container=d.container and d.targetType.id=:type) where  s.standalone=1 AND s.definitionPath in (:definitions)";

	String UPDATE_ALL_DOCUMENT_DEFINITIONS_KEY = "UPDATE_ALL_DOCUMENT_DEFINITIONS";
	String UPDATE_ALL_DOCUMENT_DEFINITIONS = "update DocumentEntity e set e.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = substring(e.parentPath, 0, locate('/', e.parentPath)) and e.container=d.container and d.targetType.id=:type) where e.revision > 0 and standalone=2";
	String UPDATE_DOCUMENT_DEFINITIONS_KEY = "UPDATE_DOCUMENT_DEFINITIONS";
	String UPDATE_DOCUMENT_DEFINITIONS = "update DocumentEntity e set e.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = substring(e.parentPath, 0, locate('/', e.parentPath)) and e.container=d.container and d.targetType.id=:type) where e.revision > 0 AND standalone=2 AND substring(e.parentPath, 0, locate('/', e.parentPath)) in (:definitions)";

	String UPDATE_ALL_WORKFLOW_DEFINITIONS_KEY = "UPDATE_ALL_WORKFLOW_DEFINITIONS";
	String UPDATE_ALL_WORKFLOW_DEFINITIONS = "update WorkflowInstanceContextEntity c set c.revision = (select max(d.revision) from DefinitionEntry d where c.workflowDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type)";
	String UPDATE_WORKFLOW_DEFINITIONS_KEY = "UPDATE_WORKFLOW_DEFINITIONS";
	String UPDATE_WORKFLOW_DEFINITIONS = "update WorkflowInstanceContextEntity c set c.revision = (select max(d.revision) from DefinitionEntry d where c.workflowDefinitionId = d.identifier and c.container=d.container and d.targetType.id=:type) where c.workflowDefinitionId in (:definitions)";

	String UPDATE_ALL_WF_TASK_DEFINITIONS_KEY = "UPDATE_ALL_WF_TASK_DEFINITIONS";
	String UPDATE_ALL_WF_TASK_DEFINITIONS = "update TaskEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.parentPath and s.container=d.container and d.targetType.id=:type) where s.taskType=0";
	String UPDATE_WF_TASK_DEFINITIONS_KEY = "UPDATE_WF_TASK_DEFINITIONS";
	String UPDATE_WF_TASK_DEFINITIONS = "update TaskEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.parentPath and s.container=d.container and d.targetType.id=:type) where s.taskType=0 AND s.parentPath in (:definitions)";

	String UPDATE_ALL_STANALONE_TASK_DEFINITIONS_KEY = "UPDATE_ALL_STANALONE_TASK_DEFINITIONS";
	String UPDATE_ALL_STANALONE_TASK_DEFINITIONS = "update TaskEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.definitionId and s.container=d.container and d.targetType.id=:type) where s.taskType=1";
	String UPDATE_STANALONE_TASK_DEFINITIONS_KEY = "UPDATE_STANALONE_TASK_DEFINITIONS";
	String UPDATE_STANALONE_TASK_DEFINITIONS = "update TaskEntity s set s.revision = (select max(d.revision) from DefinitionEntry d where d.identifier = s.definitionId and s.container=d.container and d.targetType.id=:type) where s.taskType=1 AND s.definitionId in (:definitions)";
}

