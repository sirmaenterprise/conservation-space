package com.sirma.itt.seip.domain.security;

/**
 * The Interface ActionTypeConstants.
 *
 * @author svelikov
 */
public interface ActionTypeConstants {

	// ######## CREATE #########
	String SYNCHRONIZE = "synchronize";
	String CREATE = "create";
	@Deprecated
	String ATTACH_OBJECT = "attachObject";
	@Deprecated
	String DETACH_OBJECT = "detachObject";
	String ADD_THUMBNAIL = "addThumbnail";
	String ADD_PRIMARY_IMAGE = "addPrimaryImage";
	@Deprecated
	String UPLOAD_IN_OBJECT = "uploadInObject";
	/** Import external instance. */
	String IMPORT = "import";
	/** Update of imported instance. */
	String UPDATE_INTEGRATION = "updateInt";

	String SAVE_AS_TEMPLATE = "saveAsTemplate";
	String ADD_ICONS = "addIcons";
	// Used to create instance (intelligent document) in UI2
	@Deprecated
	String CREATE_IDOCUMENT = "createIDocument";
	String CREATE_IN_CONTEXT = "createInContext";

	// ######## GENERAL #########
	String EDIT_DETAILS = "editDetails";
	String CLONE = "clone";
	String CREATE_TOPIC = "createTopic";
	String TOPIC_REPLY = "createComment";
	String LINK = "link";
	String CREATE_LINK = "createLink";
	@Deprecated
	String MOVE_SAME_CASE = "moveSameCase";
	String MOVE = "move";
	String NO_PERMISSIONS = "noPermissions";
	/** manage permissions for instance. */
	String MANAGE_PERMISSIONS = "managePermissions";
	String RESTORE_PERMISSIONS = "restorePermissions";
	String VIEW_DETAILS = "viewDetails";
	@Deprecated
	String EDIT_SCHEDULE_DETAILS = "editScheduleDetails";
	String CONFIRM_READ = "confirmRead";
	String ATTACH = "attach";
	String ATTACH_TO = "attachTo";
	String DETACH = "detach";

	// ######## CHANGE STATE #########
	String APPROVE = "approve";
	String START = "start";
	String STOP = "stop";
	String RESTART = "restart";
	String SUSPEND = "suspend";
	String COMPLETE = "complete";
	String DELETE = "delete";
	String DEACTIVATE = "deactivate";
	String ACTIVATE = "activate";
	String ACTIVATE_TEMPLATE = "activateTemplate";
	String PUBLISH = "publish";
	String PUBLISH_AS_PDF = "publishAsPdf";
	String REJECT = "reject";
	String OBSOLETE = "obsolete";
	String APPROVE_AND_PUBLISH = "approveAndPublish";
	String REJECT_AND_PUBLISH = "rejectAndPublish";
	// ######## DOCUMENT #########
	String DOWNLOAD = "download";
	String PREVIEW = "preview";
	String PRINT = "print";
	String UPLOAD = "upload";
	@Deprecated
	String ATTACH_DOCUMENT = "attachDocument";
	@Deprecated
	String DETACH_DOCUMENT = "detachDocument";
	@Deprecated
	String CREATE_IDOC = "createIdoc";
	String UPLOAD_NEW_VERSION = "uploadNewVersion";
	@Deprecated
	String EDIT_STRUCTURED_DOCUMENT = "editStructured";
	@Deprecated
	String EDIT_OFFLINE = "editOffline";
	String HISTORY_PREVIEW = "historyPreview";
	String HISTORY_DOWNLOAD = "historyDownload";
	String REVERT = "revert";
	String SIGN = "sign";
	String RELEASE = "release";
	@Deprecated
	String EDIT_ONLINE = "editOnline";
	@Deprecated
	String EDIT_INLINE = "editInline";
	String LOCK = "lock";
	String UNLOCK = "unlock";
	@Deprecated
	String STOP_EDIT = "stopEdit";
	@Deprecated
	String COPY_CONTENT = "copyContent";
	String EXPORT = "export";
	String DOCUMENT_GENERATED_AUTOMATICALLY = "generateDocument";
	String PRINT_TAB = "printTab";
	String EXPORT_PDF = "exportPDF";
	String EXPORT_TAB_PDF = "exportTabPDF";
	String EXPORT_WORD = "exportWord";
	String EXPORT_TAB_WORD = "exportTabWord";

	// ######## ACTIVITI #########
	String REASSIGN_TASK = "reassign";
	String CLAIM = "claim";
	/** The workflow had passed a transition. */
	String WORKFLOW_TRANSITION = "WORKFLOW_TRANSITION";
	String LOG_WORK = "logWork";
	String UPDATE_LOGGED_WORK = "updateWorkTime";
	String DELETE_LOGGED_WORK = "deleteWorkTime";
	String RECALL = "recall";

	String ADD_LIBRARY = "addLibrary";
	String REMOVE_LIBRARY = "removeLibrary";
	String REPLY = "reply";

	// ######## COMMENT'S ACTIONS #########
	String REPLY_COMMENT = "replyComment";
	String SUSPEND_COMMENT = "suspendComment";
	String RESTART_COMMENT = "restartComment";
	String EDIT_COMMENT = "editComment";
}
