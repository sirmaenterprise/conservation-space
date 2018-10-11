package com.sirma.itt.seip.alfresco4;

// TODO: Auto-generated Javadoc
/**
 * The Interface ServiceURIRegistry holds uri for alfresco services.
 */
public interface ServiceURIRegistry {

	/** The cmf case definitions search. */
	String CMF_CASE_DEFINITIONS_SEARCH = "/cmf/search/definitions/case";
	/** The cmf doc definitions search. */
	String CMF_DOC_DEFINITIONS_SEARCH = "/cmf/search/definitions/document";
	/** Base uploading uri. */
	String UPLOAD_SERVICE_URI = "/alfresco/service/api/cmf/upload";
	/** Attachment of case file uri. */
	String CMF_ATTACH_TO_CASE_SERVICE = "/cmf/instance/attach";

	/** The cmf dettach from case service. */
	String CMF_DETTACH_FROM_CASE_SERVICE = "/cmf/instance/dettach";

	/** The cmf close case service. */
	String CMF_CLOSE_CASE_SERVICE = "/cmf/instance/close";
	/** search service uri. */
	String CMF_SEARCH_SERVICE = "/cmf/search";

	/** The cmf search case doc service. */
	String CMF_SEARCH_CASE_DOC_SERVICE = "/cmf/search/case/documents";

	/** The cmf login. */
	String CMF_LOGIN = "/cmf/login";

	/** The node details. */
	String NODE_DETAILS = "/details/node/";
	/** The node update. */
	String NODE_UPDATE = "/cmf/node/update";
	/** The transform to pdf service. */
	String CMF_NODE_TRANSFORM = "/cmf/node/transform/";
	/** The node details. */
	String DOCUMENT_DETAILS = "/details/document/";
	/** lock node. */
	String LOCK_NODE = "/cmf/document/lock";
	/** unlock node. */
	String UNLOCK_NODE = "/cmf/document/unlock";
	/** checkout document. */
	String CHECKOUT_DOC = "/cmf/document/checkout";
	/** checkin document. */
	String CHECKIN_DOC = "/cmf/document/checkin";
	/** definition of workflows. */
	String CMF_WORKFLOW_DEFINITIONS_SEARCH = "/cmf/search/definitions/workflow";
	/** definition of tasks. */
	String CMF_TASK_DEFINITIONS_SEARCH = "/cmf/search/definitions/task";
	/** version info. */
	String VERSION_NODE_REF = "api/version?nodeRef=";
}
