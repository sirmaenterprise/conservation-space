package org.alfresco.repo.workflow;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * The Interface WorkflowServiceConstants.
 * 
 * @author hlungov
 */
public interface WorkflowServiceConstants {

	// QVI2 Start Task Custom Outcome
	/** The Constant PROP_ACTIVITI_START_TASK_OUTCOME. */
	static final QName PROP_ACTIVITI_START_TASK_OUTCOME = QName.createQName(
			NamespaceService.BPM_MODEL_1_0_URI, "activitiStartTaskOutcome");
}
