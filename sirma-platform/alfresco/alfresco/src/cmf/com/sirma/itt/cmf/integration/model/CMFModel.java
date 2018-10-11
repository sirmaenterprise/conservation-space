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
	public static String CMF_MODEL_1_0_URI = "http://www.sirmaitt.com/model/cmf/1.0";
	/** CMF Model URI. */
	public static String CMF_WORKFLOW_MODEL_1_0_URI = "http://www.sirmaitt.com/model/workflow/cmf/1.0";
	/** The cmf model prefix. */
	public static String CMF_MODEL_PREFIX = "cmf";
	/** CMF Model URI. */
	public static String CMF_WORKFLOW_MODEL_PREFIX = "cmfwf";

	/** The Constant SYSTEM_QNAME. */
	QName SYSTEM_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");

	/** The type cmf definition space. */
	QName TYPE_CMF_CASE_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "definitionspace");
	/** The type cmf definition space. */
	QName TYPE_CMF_DOCUMENT_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "documentspace");
	/** The type cmf workflow space. */
	QName TYPE_CMF_WORKFLOW_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "workflowspace");
	/** The type cmf task space. */
	QName TYPE_CMF_TASK_DEF_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "taskspace");
	/** The type cmf case. */
	QName TYPE_CMF_CASE_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "casespace");
	/** The type cmf instance space. */
	QName TYPE_CMF_INSTANCE_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "instancespace");
	/** The type cmf section case. */
	QName TYPE_CMF_CASE_SECTION_SPACE = QName.createQName(CMF_MODEL_1_0_URI, "sectionspace");
	/** The aspect cmf case. */
	QName ASPECT_CMF_CASE_INSTANCE = QName.createQName(CMF_MODEL_1_0_URI, "case");
	/** The aspect cmf case. */
	QName ASPECT_CMF_CASE_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI, "caseDefinition");

	/** The aspect cmf document definition. */
	QName ASPECT_CMF_DOCUMENT_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"documentDefinition");

	/** The aspect cmf case structured document. */
	QName ASPECT_CMF_CASE_STRUCTURED_DOC = QName.createQName(CMF_MODEL_1_0_URI,
			"caseStructuredDocument");
	/** The aspect cmf case attached document. */
	QName ASPECT_CMF_CASE_ATTACHED_DOC = QName.createQName(CMF_MODEL_1_0_URI,
			"caseAttachedDocument");
	/** The aspect cmf task definitions. */
	QName ASPECT_CMF_TASK_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI, "tasksDefinition");
	/** The aspect cmf workflow definitions. */
	QName ASPECT_CMF_WORKFLOW_DEFINITION = QName.createQName(CMF_MODEL_1_0_URI,
			"workflowDefinition");

	/** property constant for lastAccessOn. */
	QName PROP_LASTACCESS_ON = QName.createQName(CMF_MODEL_1_0_URI, "lastAccessOn");
	/** property constant for state. */
	QName PROP_PRIMARY_STATE = QName.createQName(CMF_MODEL_1_0_URI, "primaryState");
	/** property constant for state. */
	QName PROP_SECONDARY_STATE = QName.createQName(CMF_MODEL_1_0_URI, "secondaryState");
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
	QName PROP_CASE_TYPE = QName.createQName(CMF_MODEL_1_0_URI, "type");

	// //////// ------------ workflow properties -------------------
	/** property constant for closedOn. */
	QName PROP_TRANSITION_OUTCOME = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI, "taskOutcome");

	QName PROP_WF_CASE_TYPE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "caseType");
	QName PROP_WF_TASK_TYPE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "taskType");
	QName PROP_WF_TASK_STATE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "taskState");
	QName PROP_WF_TASK_STATUS = QName
			.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "taskStatus");

	/** The type task node. */
	QName ASPECT_TASK_NODE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "dmsTask");
	/** The type task node. */
	QName ASPECT_WORKFLOW_NODE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"dmsWorkflow");
	QName ASPECT_AUDITABLE = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "auditable");
	QName PROP_SRART_TRANSITION_OUTCOME = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
			"startTaskOutcome");
	QName PROP_TASK_MODIFIED = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "modified");
	/** property constant for closedFrom. */
	QName PROP_WF_ARCHIVE_REASON = QName.createQName(CMF_WORKFLOW_MODEL_1_0_URI, "archiveReason");
}
