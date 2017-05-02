package com.sirma.itt.cmf.alfresco4;

/**
 * The Interface ServiceURIRegistry holds uri for alfresco services.
 */
public interface ServiceURIRegistry {

	/** The service root. */
	String SERVICE_ROOT = "api/";

	// ########## DMS NODE OPERATIONS ####################
	String DMS_FILE_DELETE = "/slingshot/doclib/action/file/node/{0}";
	String DMS_FOLDER_DELETE = "/slingshot/doclib/action/folder/node/{0}";

	String CMF_TO_DMS_PROXY_SERVICE = "/dms/proxy";

	// ########### DEFINITIONS ####################
	/** The cmf permission definitions search. */
	String CMF_PERMISSION_DEFINITIONS_SEARCH = "/cmf/search/definitions/permission";
	/** The cmf case definitions search. */
	String CMF_CASE_DEFINITIONS_SEARCH = "/cmf/search/definitions/case";
	/** The cmf doc definitions search. */
	String CMF_DOC_DEFINITIONS_SEARCH = "/cmf/search/definitions/document";
	/** definition of workflows. */
	String CMF_WORKFLOW_DEFINITIONS_SEARCH = "/cmf/search/definitions/workflow";
	/** definition of tasks. */
	String CMF_TASK_DEFINITIONS_SEARCH = "/cmf/search/definitions/task";
	/** definition of templates. */
	String CMF_TEMPLATE_DEFINITIONS_SEARCH = "/cmf/search/templates/doctemplates";
	/** The mail templates. */
	String CMF_DEFINITIONS_NOTIFICATIONS_SEARCH = "/cmf/search/templates/notifications";
	/** generic definitions. */
	String CMF_GENERIC_DEFINITIONS_SEARCH = "/cmf/search/definitions/generic";
	// ########### GENERAL ####################
	/** The node details. */
	String NODE_DETAILS = "/details/node/";
	/** The node update. */
	String NODE_UPDATE = "/cmf/node/update";
	/** Create a new folder. */
	String FOLDER_CREATE = "/cmf/node/folder/create";

	// ########### CASE ####################
	/** The cmf close case service. */
	String CMF_CLOSE_CASE_SERVICE = "/cmf/instance/close";
	/** The cmf create case service. */
	String CMF_CREATE_CASE_SERVICE = "/cmf/instance/create";
	/** The cmf delete case service. */
	String CMF_DELETE_CASE_SERVICE = "/cmf/instance/delete";
	/** The cmf update case service. */
	String CMF_UPDATE_CASE_SERVICE = "/cmf/instance/update";
	// ########### SEARCH ####################
	/** search service uri. */
	String CMF_SEARCH_SERVICE = "/cmf/search";

	/** The cmf search case doc service. */
	String CMF_SEARCH_CASE_DOC_SERVICE = "/cmf/search/case/documents";

	/** The cmf search case global unique containers. */
	String CMF_SEARCH_CASE_CONTAINERS = "/cmf/search/containers/cmf";

	/** The cmf login. */
	String CMF_LOGIN = "/cmf/login";

	// ########### WORKFLOW ####################
	/** move workflow to next state. */
	String CMF_WORKFLOW_TRANSITION = "/workflow/instance/transition";
	String CMF_WORKFLOW_START = "/workflow/instance/start";
	String CMF_WORKFLOW_CANCEL = "/workflow/instance/cancel";
	String CMF_WORKFLOW_TASKUPDATE = "/workflow/instance/taskupdate";
	/** search task service uri. */
	String CMF_TASK_FOR_WF_SEARCH_SERVICE = "/cmf/workflow/workflow-instances/{workflow_instance_id}/task-instances";
	/** search task service uri. */
	String CMF_TASK_SEARCH_SERVICE = "/cmf/workflow/task-instances";
	/** diagram of the process. */
	String WORKFLOW_DIAGRAM = "api/workflow-instances/{workflow_instance_id}/diagram";
	// ########### TASK ####################
	String CMF_TASK_START = "/task/instance/start";
	String CMF_TASK_CANCEL = "/task/instance/cancel";
	String CMF_TASK_COMPLETE = "/task/instance/complete";

	// ########### DOCUMENT ####################
	String CMF_DOCUMENT_HISTORIC_VERSION = "/cmf/document/version";
	String CMF_DOCUMENT_REVERT_VERSION = "/cmf/document/revert";
	String CMF_DOCUMENT_COPY = "/cmf/document/copy";
	String CMF_DOCUMENT_MOVE = "/cmf/document/move";
	/** checkin document. */
	String CMF_DOCUMENT_CHECKIN = "/cmf/document/checkin";
	/** The node details. */
	String CMF_DOCUMENT_DETAILS = "/details/document/";
	/** checkout document. */
	String CMF_DOCUMENT_CHECKOUT = "/cmf/document/checkout";
	/** checkout document. */
	String CMF_DOCUMENT_CHECKOUT_CANCEL = "/cmf/document/cancelcheckout";
	/** lock node. */
	String CMF_DOCUMENT_LOCK_NODE = "/cmf/document/lock";
	/** unlock node. */
	String CMF_DOCUMENT_UNLOCK_NODE = "/cmf/document/unlock";
	/** version info. */
	String CMF_DOCUMENT_VERSION_BY_NODE = "api/version?nodeRef=";
	/** Base uploading uri. */
	String UPLOAD_SERVICE_URI = "/alfresco/service/api/cmf/upload";
	/** Attachment of case file uri. */
	String CMF_ATTACH_TO_INSTANCE_SERVICE = "/cmf/instance/attach";
	/** The cmf dettach from case service. */
	String CMF_DETTACH_FROM_INSTANCE_SERVICE = "/cmf/instance/dettach";
	/** content access uri. */
	String CMF_DOCUMENT_ACCESS_URI = "/document/access/api/node/content/{0}/{1}?a={2}";
	// ######### DYNAMIC MODEL ##############
	/** update the model with the provided xml. */
	String CMF_DYNAMIC_MODEL_UPDATE = "dictionarymodel/update";
	/** list the active cmf models. */
	String CMF_DYNAMIC_MODEL_LIST = "dictionarymodel/list";

	// ########### PERMISSIONS ####################
	/** The site membership. */
	String SITE_MEMBERSHIP = "/api/sites/{0}/memberships/{1}";
	/** The people service uri. */
	String PEOPLE_SERVICE_URI = "/api/people";
	/** The group service uri. */
	String GROUPS_SERVICE_URI = "/api/groups";
	/** The group service uri. */
	String GROUPS_MEMBERS_SERVICE_URI = "/api/groups/{0}/children";
	/** The group for user service uri. */
	String GROUPS_FOR_USER = "/cmf/authority/{0}/groups";

	/** The user synchronize method. */
	String AUTH_USER_SYNCHRONIZATION = "/cmf/authority/{0}/find";
	/** permission fix for documents. */
	String CMF_PERMISSIONS_FIX = "/case/security/update/documents";
	/** Update instance members. */
	String CMF_INSTANCE_MEMBERS = "/cmf/security/members";

	// ###############RENDITION ##############
	/** Added parameter ?c=force is used by alfresco to create thumbnail if it isn't created */
	String CONTENT_THUMBNAIL_ACCESS_URI = "/api/node/workspace/SpacesStore/{0}/content/thumbnails/doclib?c=force";
	String CONTENT_ACCESS_URI = "/api/node/{0}/content";
	/** The transform to pdf service. */
	String CONTENT_TRANSFORM_SERVICE = "/cmf/node/transform/{0}?mimetype={1}";

	// ############## DOWNLOADS ##############
	String DOWNLOADS_CREATE_ZIP_URI = "/api/internal/downloads";
	String DOWNLOADS_ZIP_URI = DOWNLOADS_CREATE_ZIP_URI + "/workspace/SpacesStore/{0}";
	String DOWNLOADS_ZIP_STATUS = DOWNLOADS_ZIP_URI + "/status";
	String DOWNLOADS_ARCHIVE_URI = "/emf/document/access/api/node/content/workspace/SpacesStore/{0}/{1}?a=true";

}
