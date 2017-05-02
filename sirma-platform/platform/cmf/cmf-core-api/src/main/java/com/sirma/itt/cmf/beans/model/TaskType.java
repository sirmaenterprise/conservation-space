package com.sirma.itt.cmf.beans.model;

/**
 * Defines the task type identifiers. Currently implemented types are workflow tasks and standalone tasks (does not
 * belong to a workflow).
 *
 * @author BBonev
 */
public enum TaskType {

	/** The workflow task. Task that belongs to a workflow and cannot exist without one. */
	WORKFLOW_TASK, /**
					 * The standalone task. Task started without any workflow. These tasks can have subtasks that are
					 * also standalone tasks.
					 */
	STANDALONE_TASK;
}
