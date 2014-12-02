package com.sirma.itt.cmf.constants.allowed_action;

/**
 * The Interface ActionTypeConstants.
 * 
 * @author svelikov
 */
// TODO: Move in emf-api
public interface ActionTypeConstants {

	// ######## CREATE #########
	// TODO: this next 3 actions should be removed from here FIXME
	String CREATE_OBJECTS_SECTION = "createObjectsSection";
	String CREATE_OBJECT = "createObject";
	String ATTACH_OBJECT = "attachObject";
	String ADD_THUMBNAIL = "addThumbnail";
	String ADD_PRIMARY_IMAGE = "addPrimaryImage";
	String UPLOAD_IN_OBJECT = "uploadInObject";

	String CREATE_TASK = "createTask";
	String START_WORKFLOW = "createWorkflow";
	String CREATE_CASE = "createCase";
	String CREATE_DOCUMENTS_SECTION = "createDocumentsSection";
	String SAVE_AS_TEMPLATE = "saveObjectAsTemplate";

	// ######## GENERAL #########
	String EDIT_DETAILS = "editDetails";
	String CLONE = "clone";
	String CREATE_TOPIC = "createTopic";
	String TOPIC_REPLY = "createComment";
	String LINK = "link";
	String CREATE_LINK = "createLink";
	String MOVE_SAME_CASE = "moveSameCase";
	String MOVE_OTHER_CASE = "moveOtherCase";
	String NO_PERMISSIONS = "noPermissions";

	// ######## CHANGE STATE #########
	String APPROVE = "approve";
	String START = "start";
	String STOP = "stop";
	String RESTART = "restart";
	String SUSPEND = "suspend";
	String COMPLETE = "complete";
	String DELETE = "delete";

	// ######## DOCUMENT #########
	String DOWNLOAD = "download";
	String PREVIEW = "preview";
	String PRINT = "print";
	String UPLOAD = "upload";
	String ATTACH_DOCUMENT = "attachDocument";
	String DETACH_DOCUMENT = "detachDocument";
	String CREATE_IDOC = "createIdoc";
	String UPLOAD_NEW_VERSION = "uploadNewVersion";
	String EDIT_STRUCTURED_DOCUMENT = "editStructured";
	String EDIT_OFFLINE = "editOffline";
	String HISTORY_PREVIEW = "historyPreview";
	String HISTORY_DOWNLOAD = "historyDownload";
	String REVERT = "revert";
	String SIGN = "sign";
	String RELEASE = "release";
	String EDIT_ONLINE = "editOnline";
	String EDIT_INLINE = "editInline";
	String LOCK = "lock";
	String UNLOCK = "unlock";
	String STOP_EDIT = "stopEdit";
	String COPY_CONTENT = "copyContent";
	String EXPORT = "export";

	String CREATE_SUB_DOCUMENT = "createSubDocument";
	String SAVE_AS_PUBLIC_TEMPLATE = "saveAsTemplate";

	// ######## ACTIVITI #########
	String REASSIGN_TASK = "reassign";
	String CLAIM = "claim";
	/** The workflow had passed a transition. */
	String WORKFLOW_TRANSITION = "WORKFLOW_TRANSITION";
	String LOG_WORK = "logWork";
	String UPDATE_LOGGED_WORK = "updateWorkTime";
	String DELETE_LOGGED_WORK = "deleteWorkTime";

}
