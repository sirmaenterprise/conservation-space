package com.sirma.itt.cmf.constants;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Base workflow properties
 * 
 * @author BBonev
 */
public interface WorkflowProperties extends DefaultProperties {

	String TYPE_WORKFLOW_TASK = "workflowTask";

	/** The Constant STARTED_BY. */
	String STARTED_BY = "startedBy";

	/** The Constant COMPLETED_BY. */
	String COMPLETED_BY = "completedBy";

	/** The Constant CONTEXT_TYPE. */
	String CONTEXT_TYPE = "contextType";

	/** The Constant PARENT_CONTAINER. */
	String PARENT_CONTAINER = "container";

	/** The Constant MESSAGE. */
	String MESSAGE = "message";

	/** The Constant PRIORITY. */
	String PRIORITY = "priority";

	/** The Constant REVISION. */
	String REVISION = "revision";

	/** The Constant TRANSITION. */
	String TRANSITION = "transitionOutcome";

	/** The Constant CANCEL_REASON. */
	String CANCEL_REASON = "cancelReason";
	/** properties for deleted wf. */
	String ARCHIVED_WORKFLOW_TASKS = "dmsArchivedWorkflow";

	// String ACTIVE_TASKS = "activeTasks";
	// String COMPLETED_TASKS = "completedTasks";
}
