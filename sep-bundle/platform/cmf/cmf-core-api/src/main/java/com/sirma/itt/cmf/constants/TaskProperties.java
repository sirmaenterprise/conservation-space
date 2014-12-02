package com.sirma.itt.cmf.constants;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Defines the task constants such as purposes, transitions etc.
 *
 * @author BBonev
 */
public interface TaskProperties extends DefaultProperties {

	/** The task assignee. */
	String TASK_ASSIGNEE = "assignee";
	/** The task group assignee. */
	String TASK_GROUP_ASSIGNEE = "groupAssignee";
	/** The task assignees. */
	String TASK_ASSIGNEES = "assignees";
	/** The task multi assignees (group,users). */
	String TASK_MULTI_ASSIGNEES = "multiAssignees";
	/** The task owner. */
	String TASK_OWNER = "owner";
	/** The start task. */
	String PURPOSE_START_TASK = "startTask";

	/** The workflow preview. */
	String PURPOSE_WORKFLOW_PREVIEW = "workflowPreview";

	/** The Constant WORKFLOW_HISTORY. */
	String PURPOSE_WORKFLOW_HISTORY = "workflowHistory";

	/** The Constant TASK_METADATA. */
	String PURPOSE_TASK_METADATA = "taskMetadata";

	/** The start by. */
	String START_BY = "startedBy";

	/** The transition start workflow. */
	String TRANSITION_START_WORKFLOW = "startWorkflow";

	/** The transition cancel. */
	String TRANSITION_CANCEL = "cancel";

	String SEARCH_ASSIGNEE = "authority";
	/** The search state. */
	String SEARCH_STATE = "state";

	/** The search priority. */
	String SEARCH_PRIORITY = "priority";

	/** The search pooled tasks. */
	String SEARCH_POOLED_TASKS = "pooledTasks";

	/** The search due before. */
	String SEARCH_DUE_BEFORE = "dueBefore";

	/** The search due after. */
	String SEARCH_DUE_AFTER = "dueAfter";

	/** The search properties. */
	String SEARCH_PROPERTIES = "properties";

	/** The search exclude. */
	String SEARCH_EXCLUDE = "exclude";
	/** The transition outcome. */
	String TRANSITION_OUTCOME = "transitionOutcome";
	/** map of properties to set in next state tasks. */
	String NEXT_STATE_PROP_MAP = "nextStateProperties";
	/** map of properties to set in current state tasks. */
	String CURRENT_STATE_PROP_MAP = "currentStateProperties";
	String NULLABLE_PROPS = "nullableProperties";
	/** the comment metadata. */
	String TASK_COMMENT = "comment";
	/** The transition workflow start. */
	String TRANSITION_WORKFLOW_START = "startWorkflow";
	/** the task id. */
	String TASK_ID = "taskId";

	// /** The task instance number. The task id with engine prefix */
	// String TASK_NUMBER = "taskNumber";

	/** The Constant COMPLETED_BY. */
	String COMPLETED_BY = "completedBy";

	String POOL_ACTORS = "pooledActors";
	/** pool task group. */
	String POOL_GROUP = "pooledGroup";
	/** the dms reference id. */
	String WORKING_INSTANCE_ON_TASK = "processReference";

	/** Logged work properties */
	String TIME_SPENT = "emf:timeSpent";
	String WORK_DESCRIPTION = "emf:workDescription";
	String START_DATE = "emf:startDate";
	/** Is active flag for task. */
	String TASK_ACTIVE_STATE = "emf:isActive";
	/** The logged by. */
	String LOGGED_BY = "emf:loggedBy";
}
