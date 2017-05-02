package org.alfresco.repo.workflow;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import com.sirma.itt.cmf.integration.model.CMFModel;

// TODO: Auto-generated Javadoc
/**
 * Constants for workflow reports - contains the new custom data.
 * 
 * @author Borislav Banchev
 */
public interface WorkflowReportConstants {

	/** The prop package. */
	QName PROP_PACKAGE = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "packageid");

	/** The prop assigned. */
	QName PROP_ASSIGNED = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "assigned");

	/** The prop workflow items. */
	QName PROP_WORKFLOW_ITEMS = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"workflowItems");


	/** The Constant COMPLETED. */
	String COMPLETED = "Completed";

	/** The Constant CANCELLED. */
	String CANCELLED = "Cancelled";

	/** The Constant IN_PROGRESS. */
	String IN_PROGRESS = "In Progress";

	/** The task space id. */
	String TASK_SPACE_ID = "taskIndexesSpace";
}
