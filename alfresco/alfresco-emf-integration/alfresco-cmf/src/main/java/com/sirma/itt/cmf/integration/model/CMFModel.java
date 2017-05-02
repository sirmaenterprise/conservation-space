/**
 *
 */
package com.sirma.itt.cmf.integration.model;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Constants for cmf model.
 *
 * @author borislav banchev
 */
public interface CMFModel {
	/** CMF Model URI. */
	String EMF_MODEL_1_0_URI = "http://www.sirmaitt.com/model/emf/1.0";
	/** CMF Model URI. */
	/** The cmf model prefix. */
	String EMF_MODEL_PREFIX = "emf";
	/** CMF Model URI. */
	String CMF_MODEL_1_0_URI = "http://www.sirmaitt.com/model/cmf/1.0";
	/** CMF Model URI. */
	String CMF_WORKFLOW_MODEL_1_0_URI = "http://www.sirmaitt.com/model/workflow/cmf/1.0";
	/** The cmf model prefix. */
	String CMF_MODEL_PREFIX = "cmf";
	/** CMF Model URI. */
	String CMF_WORKFLOW_MODEL_PREFIX = "cmfwf";

	QName TASK_REPORT_SERVICE_URI = QName.createQName(NamespaceService.ALFRESCO_URI,
			"taskReportService");

	// ############################### EMF ###################################
	/** property constant for state. */
	QName PROP_STATUS = QName.createQName(EMF_MODEL_1_0_URI, "status");
	QName PROP_TYPE = QName.createQName(EMF_MODEL_1_0_URI, "type");
	QName PROP_IDENTIFIER = QName.createQName(EMF_MODEL_1_0_URI, "identifier");
	QName PROP_NAME = QName.createQName(EMF_MODEL_1_0_URI, "name");
	QName PROP_CONTAINER = QName.createQName(EMF_MODEL_1_0_URI, "containerId");

	/** The Constant SYSTEM_QNAME. */
	QName SYSTEM_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
	// ############## Definitions spaces ###############
	/** The type cmf definition space. */
	QName TYPE_CMF_CASE_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "casedefinitionspace");
	/** The type cmf definition space. */
	QName TYPE_CMF_DOCUMENT_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI,
			"documentdefinitionspace");
	QName TYPE_CMF_TEMPLATE_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI,
			"templatedefinitionspace");
	/** The type cmf workflow space. */
	QName TYPE_CMF_WORKFLOW_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI,
			"workflowdefinitionspace");
	/** The type cmf task space. */
	QName TYPE_CMF_TASK_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "taskdefinitionspace");

	QName TYPE_CMF_GENERIC_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI,
			"genericdefinitionspace");
	QName TYPE_CMF_PERMISSIONS_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI,
			"permissiondefinitionspace");
	/** The type cmf instance space. */
	QName TYPE_CMF_CASE_INSTANCES_SPACE = QName
			.createQName(CMF_MODEL_1_0_URI, "caseinstancesspace");
	// ############## Definitions Aspects ###############
	/** The aspect cmf case definition. */
	QName ASPECT_CMF_CASE_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI, "caseDefinition");
	/** The aspect cmf document definition. */
	QName ASPECT_CMF_DOCUMENT_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"documentDefinition");
	/** The aspect cmf task definitions. */
	QName ASPECT_CMF_TASK_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI, "tasksDefinition");
	/** The aspect cmf workflow definitions. */
	QName ASPECT_CMF_WORKFLOW_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"workflowDefinition");
	QName ASPECT_CMF_TEMPLATE_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"templateDefinition");
	QName ASPECT_CMF_GENERIC_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI, "genericDefinition");
	QName ASPECT_CMF_PERMISSIONS_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"permissionDefinition");
	// ############## Type containers ###############
	/** The type cmf case. */
	QName TYPE_CMF_CASE_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "casespace");
	/** The type cmf section case. */
	QName TYPE_CMF_CASE_SECTION_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "sectionspace");

	// ############## Elements aspects ###############
	/** The aspect cmf case. */
	QName ASPECT_CMF_CASE_INSTANCE = QName.createQName(CMF_MODEL_1_0_URI, "case");
	/** The aspect cmf case structured document. */
	QName ASPECT_CMF_CASE_STRUCTURED_DOC = QName.createQName(CMF_MODEL_1_0_URI,
			"structuredDocument");
	/** The aspect cmf case attached document. */
	QName ASPECT_CMF_CASE_ATTACHED_DOC = QName.createQName(CMF_MODEL_1_0_URI, "attachedDocument");
	QName ASPECT_CMF_CASE_COMMON_DOC = QName.createQName(CMF_MODEL_1_0_URI, "commonDocument");
	// ----------- wf and task aspects
	/** The type task node. */
	QName ASPECT_TASK_NODE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "dmsTask");
	/** The type task node. */
	QName ASPECT_WORKFLOW_NODE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"dmsWorkflow");
	QName ASPECT_AUDITABLE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "auditable");

	// ############## case properties ###############
	/** property constant for lastAccessOn. */
	QName PROP_LASTACCESS_ON = QName.createQName(CMF_MODEL_1_0_URI, "lastAccessOn");
	/** property constant for id. */
	QName PROP_CMF_NAMEID = QName.createQName(CMF_MODEL_1_0_URI, "nameId");
	/** property constant for lastAccessFrom. */
	QName PROP_LASTACCESS_FROM = QName.createQName(CMF_MODEL_1_0_URI, "lastAccessFrom");
	/** property constant for archivedFrom. */
	QName PROP_ARCHIVED_FROM = QName.createQName(CMF_MODEL_1_0_URI, "archivedFrom");
	/** property constant for closedFrom. */
	QName PROP_CLOSED_FROM = QName.createQName(CMF_MODEL_1_0_URI, "closedFrom");
	/** property constant for closedFrom. */
	QName PROP_CLOSED_REASON = QName.createQName(CMF_MODEL_1_0_URI, "closedReason");

	/** property constant for archiveDate. */
	QName PROP_ARCHIVE_DATE = QName.createQName(CMF_MODEL_1_0_URI, "archiveDate");
	/** property constant for archiveID. */
	QName PROP_ARCHIVE_ID = QName.createQName(CMF_MODEL_1_0_URI, "archiveId");

	/** property constant for closedOn. */
	QName PROP_CLOSED_ON = QName.createQName(CMF_MODEL_1_0_URI, "closedOn");

	// //////// ------------ workflow properties -------------------
	/** property constant for closedOn. */
	QName PROP_TRANSITION_OUTCOME = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI, "taskOutcome");
	QName PROP_WF_CONTEXT_TYPE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"contextType");
	QName PROP_WF_CONTEXT_ID = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "contextId");
	QName PROP_WF_TASK_STATE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "state");

	QName PROP_SRART_TRANSITION_OUTCOME = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"startTaskOutcome");
	QName PROP_TASK_MODIFIED = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "modified");
	/** property constant for closedFrom. */
	QName PROP_WF_ARCHIVE_REASON = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI, "archiveReason");

	/** reference */
	QName ASSOC_CHILD_PARENT_REF = QName.createQName(CMFModel.EMF_MODEL_1_0_URI, "children");

	QName ASSOC_TASK_MULTI_ASSIGNEES = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI,
			"multiAssignees");
	QName ASPECT_TASK_MULTI_ASSIGNEES = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI,
			"multiAssignees");
}
