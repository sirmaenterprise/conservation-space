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
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE),
		@NamedQuery(name = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_KEY, query = DbQueryTemplates.QUERY_USERS_ON_INSTANCE),
		@NamedQuery(name = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY, query = DbQueryTemplates.QUERY_USERS_ON_INSTANCE_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY, query = DbQueryTemplates.DELETE_ASSIGNED_TASKS_BY_WF_ID),
		@NamedQuery(name = DbQueryTemplates.REMOVE_TASK_FROM_CONTEXT_KEY, query = DbQueryTemplates.REMOVE_TASK_FROM_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY, query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_FOR_CASE_AND_USER),
		@NamedQuery(name = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_KEY, query = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_KEY, query = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT_KEY, query = DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_BY_CONTEXT_AND_PARENT),
		@NamedQuery(name = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_AND_STATUS_KEY, query = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_AND_STATUS),
		@NamedQuery(name = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_KEY, query = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT),
		@NamedQuery(name = DbQueryTemplates.ASSIGNED_TASKS_FOR_USER_KEY, query = DbQueryTemplates.ASSIGNED_TASKS_FOR_USER),

		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_KEY, query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH),
		@NamedQuery(name = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY, query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_AND_STATE) })
public class QueryDao {

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id to set
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