package com.sirma.itt.cmf.alfresco4;

/**
 * Constants for rest communication.
 *
 * @author bbanchev
 */
public interface AlfrescoCommunicationConstants {
	String WF_NAMESPACE = "wf_";
	String TASK_NAMESPACE = "task_";
	/** Comment for UTF_8. */
	String UTF_8 = "UTF-8";
	/** Comment for CMF. */
	String CMF_MODEL_PREFIX = "cmf:";
	/** workflow model prefix. */
	String CMF_WF_MODEL_PREFIX = "cmfwf:";
	/** workflow model prefix. */
	String BPM_WF_MODEL_PREFIX = "bpm:";
	/** CMF Model URI. */
	String CMF_MODEL_1_0_URI = "http://www.sirmaitt.com/model/cmf/1.0";
	/** general type. */
	String TYPE_CM_CONTENT = "cm:content";
	/** skipped for update properties suffix. */
	String SKIPPED_PROPERTY = "|SKIP";

	String KEY_CREATOR = "creator";

	String KEY_LABEL = "label";

	String KEY_CREATED_DATE_ISO = "createdDateISO";

	String KEY_USER_NAME = "userName";
	/** . */
	String KEY_START_PATH = "startPath";
	/** . */
	String KEY_SITES_IDS = "sites";
	/** the key definition, describing the noderef id for definition . */
	String KEY_DEFINITION_ID = "definitionId";

	/** . */
	String KEY_FORCED_OPERATION = "force";
	/** attachment id. */
	String KEY_ATTACHMENT_ID = "attachmentId";
	/** . */
	String KEY_SITE_ID = "site";
	/** . */
	String KEY_SITEID = "siteid";
	/** . */
	String KEY_CONTEXT = "context";
	/** . */
	String KEY_NODEREF = "nodeRef";
	/** . */
	String KEY_NODEID = "node";
	
	/** . */
	String KEY_OVERALLSUCCESS = "overallSuccess";
	
	/** case dms id. */
	// TODO remove case_id
	@Deprecated
	String KEY_CASE_ID = KEY_NODEID;
	/** . */
	String KEY_CHILD_ASSOC_NAME = "childAssocName";
	/** . */
	String KEY_PROPERTIES = "properties";
	/** . */
	String KEY_SECTIONS = "sections";
	/** . */
	String KEY_DATA_ITEMS = "items";
	/** . */
	String KEY_PARENT = "parent";
	/** . */
	String KEY_ROOT_DATA = "data";
	/** . */
	String KEY_SORT = "sort";
	/** . */
	String KEY_QUERY = "query";
	/** . */
	String KEY_ASPECT = "ASPECT";
	/** . */
	String KEY_VERSION = "version";
	/** . */
	String KEY_DOCUMENT_VERSION = "documentVersion";
	// paging
	/** . */
	String KEY_PAGING = "paging";
	/** . */
	String KEY_PAGING_SKIP = "skip";
	/** . */
	String KEY_PAGING_SIZE = "pageSize";
	/** . */
	String KEY_PAGING_TOTAL = "total";

	String KEY_PAGING_MAX = "maxSize";
	/** ticket key. */
	String KEY_TICKET = "ticket";
	/** lock owner. */
	String KEY_LOCK_OWNER = "lockOwner";

	String KEY_VERSIONS = "versions";
	/** The emf type. */
	String KEY_TYPE = "type";
	// ----------------- for upload
	/** The Constant KEY_DESCRIPTION. */
	String KEY_DESCRIPTION = "description";
	/** The Constant KEY_UPDATE_NODE_REF. */
	String KEY_UPDATE_NODE_REF = "updatenoderef";
	/** The Constant KEY_OVERWRITE. */
	String KEY_OVERWRITE = "overwrite";
	/** The Constant KEY_MAJOR_VERSION. */
	String KEY_MAJOR_VERSION = "majorversion";
	/** The Constant KEY_ASPECTS. */
	String KEY_ASPECTS = "aspects";
	/** The Constant KEY_CONTENT_TYPE. */
	String KEY_CONTENT_TYPE = "contenttype";
	/** The Constant KEY_FILENAME. */
	String KEY_FILENAME = "filename";
	/** The Constant KEY_UPLOAD_DIRECTORY. */
	String KEY_UPLOAD_DIRECTORY = "uploaddirectory";
	/** The Constant KEY_CONTAINER_ID. */
	String KEY_CONTAINER_ID = "containerid";
	/** the parent node. */
	String KEY_DESTINATION = "destination";
	/** The Constant KEY_FILE_DATA. */
	String KEY_FILE_DATA = "filedata";
	/** The thumbnail (asynch, synch, none). */
	String KEY_THUMBNAIL = "thumbnail";
	// some default values
	/** The Constant DIR_DOCUMENT_LIBRARY. */
	String DIR_DOCUMENT_LIBRARY = "documentLibrary";
	/** The Constant ASPECT_CM_VERSIONABLE. */
	String ASPECT_CM_VERSIONABLE = "cm:versionable";
	// -----------------

	// -------task & workflow-----//
	/** the task id. */
	String KEY_TASK_ID = "taskId";
	/** the transition id. */
	String KEY_TRANSITION_ID = "transitionId";
	/** the workflow id. */
	String KEY_WORKFLOW_ID = "workflowId";
	String KEY_PARENT_TASK_ID = "taskParentId";
	/** reference id. */
	String KEY_REFERENCE_ID = "referenceId";
	String KEY_NAME = "name";
	String KEY_ID = "id";
	String KEY_TITLE = "title";

}
