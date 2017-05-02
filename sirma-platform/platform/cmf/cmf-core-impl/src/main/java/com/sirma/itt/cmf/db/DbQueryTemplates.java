package com.sirma.itt.cmf.db;

/**
 * Holds all queries templates for local database access.
 *
 * @author BBonev
 */
public interface DbQueryTemplates {

	/*
	 * =====================================================================
	 */
	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY = "DELETE_ASSIGNED_TASKS_BY_WF_ID";
	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID. */
	String DELETE_ASSIGNED_TASKS_BY_WF_ID = "delete from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType";

	/** The Constant REMOVE_TASK_FROM_CONTEXT_KEY. */
	String REMOVE_TASK_FROM_CONTEXT_KEY = "REMOVE_TASK_FROM_CONTEXT";
	/** The Constant DELETE_ASSIGNED_TASKS_BY_WF_ID. */
	String REMOVE_TASK_FROM_CONTEXT = "delete from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType AND a.taskDmsId in (:taskDmsId)";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS_KEY = "QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS = "select a.taskDmsId from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType and a.active=:active";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT_KEY = "QUERY_ASSIGNED_TASKS_BY_CONTEXT";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_CONTEXT = "select a.taskDmsId from AssignedUserTasks a where a.contextReference.sourceId=:sourceId AND a.contextReference.sourceType.id=:sourceType";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS_KEY = "QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS = "select a.taskDmsId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.active=:active and a.taskType in (:taskType)";

	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID_KEY. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_KEY = "QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE";
	/** The Constant QUERY_ASSIGNED_TASKS_BY_WF_ID. */
	String QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE = "select a.taskDmsId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.taskType in (:taskType)";

	/** The Constant QUERY_USERS_ON_INSTANCE_KEY. */
	String QUERY_USERS_ON_INSTANCE_KEY = "QUERY_USERS_ON_INSTANCE";
	/** The Constant QUERY_USERS_ON_INSTANCE. */
	String QUERY_USERS_ON_INSTANCE = "select distinct a.userId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.userId is not null";

	/** The Constant QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY. */
	String QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY = "QUERY_USERS_ON_INSTANCE_AND_STATUS";
	/** The Constant QUERY_USERS_ON_INSTANCE_AND_STATUS. */
	String QUERY_USERS_ON_INSTANCE_AND_STATUS = "select distinct a.userId from AssignedUserTasks a where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and active=:active and a.userId is not null";

	/** The Constant QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER key. */
	String QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY = "QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER";
	/** The Constant QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER. */
	String QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER = "select a.taskDmsId from AssignedUserTasks a where a.userId=:userId and a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType and a.active=:active";

	/** The Constant ASSIGNED_TASKS_FOR_USER_KEY access the query for obtaining user tasks. */
	String ASSIGNED_TASKS_FOR_USER_KEY = "ASSIGNED_TASKS_FOR_USER";
	/** The Constant ASSIGNED_TASKS_FOR_USER. */
	String ASSIGNED_TASKS_FOR_USER = "select a.owningInstance from AssignedUserTasks a where a.userId=:userId and a.owningInstance.sourceType.id=:sourceType";

	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_KEY. */
	String UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_KEY = "UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT";
	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT. */
	String UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT = "update AssignedUserTasks a set a.active=:active, a.userId=:userId  where a.contextReference.sourceId=:contextSourceId AND a.contextReference.sourceType.id=:contextSourceType AND a.taskDmsId=:taskDmsId";

	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT_KEY. */
	String UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT_KEY = "UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT";
	/** The Constant UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT. */
	String UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT = "update AssignedUserTasks a set a.active=:active, a.userId=:userId  where a.owningInstance.sourceId=:sourceId AND a.owningInstance.sourceType.id=:sourceType AND a.contextReference.sourceId=:contextSourceId AND a.contextReference.sourceType.id=:contextSourceType AND a.taskDmsId=:taskDmsId";

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
	 * STANDALONE TASKS
	 */

	/** The Constant QUERY_TASK_BY_PARENT_PATH_KEY. */
	String QUERY_TASK_BY_PARENT_PATH_KEY = "QUERY_TASK_BY_PARENT_PATH";
	/** The Constant QUERY_TASK_BY_PARENT_PATH. */
	String QUERY_TASK_BY_PARENT_PATH = "select t.id from InstanceEntity t where t.treePath like :treePath and t.id<>:id";

	/** The Constant QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY. */
	String QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY = "QUERY_TASK_BY_PARENT_PATH_AND_STATE";
	/** The Constant QUERY_TASK_BY_PARENT_PATH_AND_STATE. */
	String QUERY_TASK_BY_PARENT_PATH_AND_STATE = "select t.id from InstanceEntity t, AssignedUserTasks a where t.treePath like :treePath AND a.taskDmsId=t.dmsId AND a.active=:active and t.id<>:id";
}
