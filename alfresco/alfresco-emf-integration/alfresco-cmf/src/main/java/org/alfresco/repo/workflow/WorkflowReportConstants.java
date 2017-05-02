package org.alfresco.repo.workflow;

import org.alfresco.service.namespace.QName;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * Constants for workflow reports - contains the new custom data.
 *
 * @author Borislav Banchev
 */
public interface WorkflowReportConstants {

	/** The prop package. */
	QName PROP_PACKAGE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "package");

	/** The prop assigned. */
	QName PROP_ASSIGNED = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "assignee");

	QName PROP_POOLED_ACTORS = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"pooledActors");
	QName PROP_POOLED_GROUPS = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"pooledGroups");

	/** The Constant COMPLETED. */
	String COMPLETED = "Completed";

	/** The Constant CANCELLED. */
	String CANCELLED = "Cancelled";

	/** The Constant IN_PROGRESS. */
	String IN_PROGRESS = "In Progress";

	/** The task space id. */
	String TASK_SPACE_ID = "taskIndexesSpace";

}
