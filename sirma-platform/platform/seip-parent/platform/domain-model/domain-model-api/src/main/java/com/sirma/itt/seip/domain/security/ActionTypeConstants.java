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
	/**
	 * @deprecated Should not be used as it's not relevant
	 */
	@Deprecated
	String ATTACH_OBJECT = "attachObject";
	/**
	 * @deprecated Should not be used as it's not relevant
	 */
	@Deprecated
	String DETACH_OBJECT = "detachObject";
	String ADD_THUMBNAIL = "addThumbnail";
	String ADD_PRIMARY_IMAGE = "addPrimaryImage";
	/** Import external instance. */
	String IMPORT = "import";

	String ADD_ICONS = "addIcons";
	// Used to create instance (intelligent document) in UI2
	String CREATE_IN_CONTEXT = "createInContext";

	// ######## GENERAL #########
	String EDIT_DETAILS = "editDetails";
	String CLONE = "clone";
	String CREATE_TOPIC = "createTopic";
	String TOPIC_REPLY = "createComment";
	String LINK = "link";
	String CREATE_LINK = "createLink";
	String MOVE = "move";
	String NO_PERMISSIONS = "noPermissions";
	String NO_ACTIONS_ALLOWED = "noAllowed";
	/** manage permissions for instance. */
	String MANAGE_PERMISSIONS = "managePermissions";
	String RESTORE_PERMISSIONS = "restorePermissions";
	String VIEW_DETAILS = "viewDetails";
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
	String PUBLISH = "publish";
	String PUBLISH_AS_PDF = "publishAsPdf";
	String UPLOAD_REVISION = "uploadRevision";
	String REJECT = "reject";
	String OBSOLETE = "obsolete";
	String APPROVE_AND_PUBLISH = "approveAndPublish";
	String REJECT_AND_PUBLISH = "rejectAndPublish";
	// ######## DOCUMENT #########
	String DOWNLOAD = "download";
	String PREVIEW = "preview";
	String PRINT = "print";
	String UPLOAD = "upload";
	/**
	 * @deprecated Should not be used as it's not relevant
	 */
	@Deprecated
	String ATTACH_DOCUMENT = "attachDocument";
	/**
	 * @deprecated Should not be used as it's not relevant
	 */
	@Deprecated
	String DETACH_DOCUMENT = "detachDocument";
	String UPLOAD_NEW_VERSION = "uploadNewVersion";
	String UPDATE_INSTANCE_TEMPLATE = "updateInstanceTemplate";
	String UPDATE_SINGLE_INSTANCE_TEMPLATE = "updateTemplate";
	String EDIT_OFFLINE = "editOffline";
	String HISTORY_PREVIEW = "historyPreview";
	String HISTORY_DOWNLOAD = "historyDownload";
	String REVERT = "revert";
	String SIGN = "sign";
	String RELEASE = "release";
	String LOCK = "lock";
	String UNLOCK = "unlock";
	String EXPORT = "export";
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

	// ######## SYSTEM ACTIONS #########
	String READ = "$read$";
}
