package com.sirma.itt.cmf.alfresco4;

import java.nio.charset.StandardCharsets;

/**
 * Constants for rest communication.
 *
 * @author bbanchev
 */
public interface AlfrescoCommunicationConstants {

	String UTF_8 = StandardCharsets.UTF_8.name();

	String TYPE_CM_CONTENT = "cm:content";

	String KEY_CREATOR = "creator";

	String KEY_LABEL = "label";

	String KEY_CREATED_DATE_ISO = "createdDateISO";

	String KEY_USER_NAME = "userName";

	String KEY_ATTACHMENT_ID = "attachmentId";

	String KEY_SITE_ID = "site";

	String KEY_SITEID = "siteid";

	String KEY_CONTEXT = "context";

	String KEY_NODEREF = "nodeRef";

	String KEY_NODEID = "node";

	String KEY_OVERALLSUCCESS = "overallSuccess";

	String KEY_CHILD_ASSOC_NAME = "childAssocName";

	String KEY_PROPERTIES = "properties";

	String KEY_DATA_ITEMS = "items";

	String KEY_ROOT_DATA = "data";

	String KEY_SORT = "sort";

	String KEY_QUERY = "query";

	String KEY_ASPECT = "ASPECT";

	String KEY_VERSION = "version";

	String KEY_PAGING = "paging";

	String KEY_PAGING_SKIP = "skip";

	String KEY_PAGING_SIZE = "pageSize";

	String KEY_PAGING_TOTAL = "total";

	String KEY_PAGING_MAX = "maxSize";

	String KEY_LOCK_OWNER = "lockOwner";

	String KEY_TYPE = "type";

	String KEY_DESCRIPTION = "description";

	String KEY_UPDATE_NODE_REF = "updatenoderef";

	String KEY_OVERWRITE = "overwrite";

	String KEY_MAJOR_VERSION = "majorversion";

	String KEY_ASPECTS = "aspects";

	String KEY_CONTENT_TYPE = "contenttype";

	String KEY_UPLOAD_DIRECTORY = "uploaddirectory";

	String KEY_CONTAINER_ID = "containerid";

	String KEY_DESTINATION = "destination";

	String KEY_FILE_DATA = "filedata";

	/** The thumbnail (asynch, synch, none). */
	String KEY_THUMBNAIL = "thumbnail";

	/** The direct,custom, etc. mode. */
	String KEY_UPLOAD_MODE = "uploadMode";

	String DIR_DOCUMENT_LIBRARY = "documentLibrary";

	String KEY_REFERENCE_ID = "referenceId";

	String KEY_NAME = "name";

	String KEY_ID = "id";

	String KEY_DMS_DESCRIPTION = "cm:description";

	String KEY_DMS_TITLE = "cm:title";

	String KEY_DMS_NAME = "cm:name";

	String KEY_DMS_CONTENT = "cm:content";
}
