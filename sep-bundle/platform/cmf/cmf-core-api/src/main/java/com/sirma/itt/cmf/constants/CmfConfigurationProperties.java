package com.sirma.itt.cmf.constants;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Defines all CMF specific configuration name properties.
 *
 * @author BBonev
 */
@Documentation("Cmf configuration properties")
public interface CmfConfigurationProperties extends Configuration {

	/** The DMS host. */
	@Documentation("DMS server host address")
	String DMS_HOST = "dms.host";

	/** The DMS port. */
	@Documentation("DMS server port")
	String DMS_PORT = "dms.port";

	/** The DMS port. */
	@Documentation("DMS server protocol. <b>Default value is: http</b>")
	String DMS_PROTOCOL = "dms.protocol";

	/** path to config file. */
	@Documentation("Path to the DMS converter properties")
	String CONFIG_CONVERTER_LOCATION = "convertor.dms.location";

	@Documentation("List of DMS fields to be copied to each document from holding case.")
	String INHERITED_DOCUMENT_PERMISSIONS = "permissions.document.inherited";

	@Documentation("Whether dms should be updated when permissions are changed in cmf. Default is <b>false</b>")
	String PERMISSION_MODEL_DMS_ENABLED = "permissions.model.dms.enabled";
	/** The codelist wsdl location. */
	@Documentation("Location of the codelist service")
	String CODELIST_WSDL_LOCATION = "codelist.wsdlLocation";

	/** The codelist file location. */
	@Documentation("Location of the codelist mock location file")
	String CODELIST_FILE_LOCATION = "codelist.fileLocation";

	/** The key document update description. */
	@Documentation("If present will be used as an auto generated comment to populate the version description when updating only document properties.")
	String KEY_DOCUMENT_UPDATE_DESCRIPTION = "document.update.auto.description";

	@Documentation("Who is the responsible user for requested user help?")
	String NOTIFICATIONS_ADMIN_USERNAME = "notifications.admin.username";

	// ***************************************************
	// web configuration properties
	// ***************************************************

	// Dynamic codelist load
	/** number for codelist of document title. */
	@Documentation("Codelist number of document title field. <b>Default value is: 210</b>")
	String CODELIST_DOCUMENT_TITLE = "codelist.documentTitle";

	@Documentation("Codelist number of case definition type. <b>Default value is: 200</b>")
	String CODELIST_CASE_DEFINITIONS = "codelist.caseDefinitions";

	@Documentation("Codelist number of task definition type. <b>Default value is: 227</b>")
	String CODELIST_TASK_DEFINITION = "codelist.taskDefinition";

	@Documentation("Codelist value for start task outcome. <b>Default value is: RT0099</b>")
	String CODELIST_START_TASK_OUTCOME = "codelist.value.startTaskOutcome";

	@Documentation("Codelist number of task status field. <b>Default value is: 102</b>")
	String CODELIST_TASK_STATUS = "codelist.taskStatus";

	@Documentation("Codelist number of service priority field. <b>Default value is: 208</b>")
	String CODELIST_SERVICE_PRIORITY = "codelist.servicePriority";

	@Documentation("Codelist number for common document type.<b>Default value is: OT210027</b>")
	// REVIEW rethink the property name
	String CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE = "codelist.documentDefaultAttachmentType";

	@Documentation("Codelist value for workflow service priority with level: low")
	String WORKFLOW_PRIORITY_LOW = "workflow.priority.low";
	@Documentation("Codelist value for workflow service priority with level: normal")
	String WORKFLOW_PRIORITY_NORMAL = "workflow.priority.normal";
	@Documentation("Codelist value for workflow service priority with level: high")
	String WORKFLOW_PRIORITY_HIGH = "workflow.priority.high";

	/*
	 * ====================== STATE CONFIGURATIONS ======================
	 */
	/*
	 * TASK STATES
	 */
	@Documentation("Codelist number for task state field")
	String TASK_STATE_CODELIST = "task.state.codelist";
	/** The task state initial. */
	@Documentation("The task state initial.")
	String TASK_STATE_INITIAL = "task.state.initial";
	/** The task state in progress. */
	@Documentation("Codelist value for task state: in progress")
	String TASK_STATE_IN_PROGRESS = "task.state.in_progress";
	/** The task state approved. */
	@Documentation("Codelist value for task state: opened")
	String TASK_STATE_APPROVED = "task.state.approved";
	/** The task state on hold. */
	@Documentation("Codelist value for task state: on hold")
	String TASK_STATE_ON_HOLD = "task.state.on_hold";
	/** The task state completed. */
	@Documentation("Codelist value for task state: completed")
	String TASK_STATE_COMPLETED = "task.state.completed";
	/** The task state canceled. */
	@Documentation("Codelist value for task state: canceled")
	String TASK_STATE_CANCELED = "task.state.stoppped";
	/** The task state submitted. */
	@Documentation("The task state submitted.")
	String TASK_STATE_SUBMITTED = "task.state.submitted";
	/** The task state deleted. */
	@Documentation("The task state deleted.")
	String TASK_STATE_DELETED = "task.state.deleted";

	/*
	 * CASE STATES
	 */
	/** The case state codelist. */
	@Documentation("The case state codelist. Default value: 106")
	String CASE_STATE_CODELIST = "case.state.codelist";
	/** The case initial state. */
	@Documentation("The case initial state.")
	String CASE_STATE_INITIAL = "case.state.initial";
	/** The case state in progress. */
	@Documentation("The case state in progress.")
	String CASE_STATE_IN_PROGRESS = "case.state.in_progress";
	/** The case state completed. */
	@Documentation("The case state completed.")
	String CASE_STATE_COMPLETED = "case.state.completed";
	/** The case state deleted. */
	@Documentation("The case state deleted.")
	String CASE_STATE_DELETED = "case.state.deleted";
	/** The case state submitted. */
	@Documentation("The case state submitted.")
	String CASE_STATE_SUBMITTED = "case.state.submitted";
	/** The case state approved. */
	@Documentation("The case state approved.")
	String CASE_STATE_APPROVED = "case.state.approved";
	/** The case state on hold. */
	@Documentation("The case state on hold.")
	String CASE_STATE_ON_HOLD = "case.state.on_hold";
	/** The case state stopped. */
	@Documentation("The case state stopped.")
	String CASE_STATE_STOPPED = "case.state.stopped";
	/** The case state archived. */
	@Documentation("The case state archived.")
	String CASE_STATE_ARCHIVED = "case.state.archived";

	/*
	 * DOCUMENT STATES
	 */
	/** The document state codelist. */
	@Documentation("The document state codelist. Default value: 106")
	String DOCUMENT_STATE_CODELIST = "document.state.codelist";
	/** The document initial state. */
	@Documentation("The document initial state.")
	String DOCUMENT_STATE_INITIAL = "document.state.initial";
	/** The document state in progress. */
	@Documentation("The document state in progress.")
	String DOCUMENT_STATE_IN_PROGRESS = "document.state.in_progress";
	/** The document state completed. */
	@Documentation("The document state completed.")
	String DOCUMENT_STATE_COMPLETED = "document.state.completed";
	/** The document state deleted. */
	@Documentation("The document state deleted.")
	String DOCUMENT_STATE_DELETED = "document.state.deleted";
	/** The document state submitted. */
	@Documentation("The document state submitted.")
	String DOCUMENT_STATE_SUBMITTED = "document.state.submitted";
	/** The document state approved. */
	@Documentation("The document state approved.")
	String DOCUMENT_STATE_APPROVED = "document.state.approved";
	/** The document state on hold. */
	@Documentation("The document state on hold.")
	String DOCUMENT_STATE_ON_HOLD = "document.state.on_hold";
	/** The document state stopped. */
	@Documentation("The document state stopped.")
	String DOCUMENT_STATE_STOPPED = "document.state.stopped";
	/** The document state archived. */
	@Documentation("The document state archived.")
	String DOCUMENT_STATE_ARCHIVED = "document.state.archived";

	/*
	 * WORKFLOW STATES
	 */
	@Documentation("Codelist number for workflow status field")
	String WORKFLOW_STATE_CODELIST = "workflow.state.codelist";
	/** The workflow state canceled. */
	@Documentation("Codelist value for workflow status: canceled")
	String WORKFLOW_STATE_CANCELED = "workflow.state.stopped";
	/** The workflow state in progress. */
	@Documentation("Codelist value for workflow status: in progress")
	String WORKFLOW_STATE_IN_PROGRESS = "workflow.state.in_progress";
	/** The workflow state completed. */
	@Documentation("Codelist value for workflow status: completed")
	String WORKFLOW_STATE_COMPLETED = "workflow.state.completed";
	/** The workflow state submitted. */
	@Documentation("The workflow state submitted.")
	String WORKFLOW_STATE_SUBMITTED = "workflow.state.submitted";
	/** The workflow state deleted. */
	@Documentation("The workflow state deleted.")
	String WORKFLOW_STATE_DELETED = "workflow.state.deleted";
	/** The workflow state approved. */
	@Documentation("The workflow state approved.")
	String WORKFLOW_STATE_APPROVED = "workflow.state.approved";
	/** The workflow state initial. */
	@Documentation("The workflow state initial.")
	String WORKFLOW_STATE_INITIAL = "workflow.state.initial";
	/** The workflow state on hold. */
	@Documentation("The workflow state on hold.")
	String WORKFLOW_STATE_ON_HOLD = "workflow.state.on_hold";

	/*
	 * END OF STATE CONFIGURATIONS
	 */

	@Documentation("Cron like expression for interval of users cache update. Format is: 'M d h m'. Default value is: * * * */15")
	String CACHE_USER_UPADTE_SCHEDULE = "cache.user.update.schedule";
	@Documentation("Cron like expression for interval of groups cache update. Format is: 'M d h m'. Default value is: * * * */15")
	String CACHE_GROUP_UPADTE_SCHEDULE = "cache.group.update.schedule";
	@Documentation("Maximum number of files to upload with one request")
	String LIMIT_MAX_FILES_NUMBER = "cmf.limit.upload.max.file.number";

	/*
	 * DEFAULT DEFINITIONS
	 */
	@Documentation("The default definition to be used for users handling")
	String DEFAULT_USER_DEFINITION = "user.definition.default";
	@Documentation("The default definition to be used for groups handling")
	String DEFAULT_GROUP_DEFINITION = "group.definition.default";
	@Documentation("The default definition to be used for topics handling")
	String DEFAULT_TOPIC_DEFINITION = "topic.definition.default";
	@Documentation("The default definition to be used for comment handling")
	String DEFAULT_COMMENT_DEFINITION = "comment.definition.default";
	@Documentation("The default definition to be used for relations handling")
	String DEFAULT_LINK_DEFINITION = "link.definition.default";
	@Documentation("The default definition to be used for image annotation handling")
	String DEFAULT_IMAGE_ANNOTATION_DEFINITION = "imageAnnotation.definition.default";

	/**
	 * Property to enable/disable parallel template saving. Disabled by default due to some unknown
	 * race condition in DMS when document upload is made.<b>Default value is: false</b>
	 */
	@Documentation("Property to enable/disable parallel template saving. Disabled by default due to some unknown race condition in DMS when document upload is made.<b>Default value is: false</b>")
	String TEMPLATES_PARALLEL_SAVE = "templates.useParallelSave";

}
