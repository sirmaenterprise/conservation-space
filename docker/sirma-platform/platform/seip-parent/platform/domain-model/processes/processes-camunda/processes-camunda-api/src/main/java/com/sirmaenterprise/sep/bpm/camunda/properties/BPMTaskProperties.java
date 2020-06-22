
package com.sirmaenterprise.sep.bpm.camunda.properties;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * {@link BPMTaskProperties} provides all properties used in camunda tasks.
 * 
 * @author Hristo Lungov
 */
public interface BPMTaskProperties extends DefaultProperties {

	/** The property to hold the emf instance id in camunda task variables.	 */
	String TASK_ID = "emfInstanceTaskId";
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
	/** Task assignee */
	String HAS_ASSIGNEE = "emf:hasAssignee";
	/** Pool Task Assignee */
	String HAS_POOL_ASSIGNEE = "emf:hasPoolAssignee";
	/** The parallel assignees */
	String TASK_PARALLEL_ASSIGNEE = "parallelAssignees";
	/** The pool assignees */
	String TASK_POOL_ASSIGNEES = "poolAssignees";


}
