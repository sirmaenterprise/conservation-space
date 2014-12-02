package com.sirma.itt.pm.alfresco4;

/**
 * The Interface ServiceURIRegistry holds uri for alfresco services regarding PM
 * functionality.
 */
public interface ServiceURIRegistry extends com.sirma.itt.cmf.alfresco4.ServiceURIRegistry {

	/** The pm definition retrieve. */
	String PM_PROJECT_DEFINITIONS_SEARCH = "/pm/search/definitions/project";
	/** Create instance service uri. */
	String PM_PROJECT_INSTANCE_CREATE_SERVICE = "/pm/projectinstance/create";
	/** update instance service uri. */
	String PM_PROJECT_INSTANCE_UPDATE_SERVICE = "/pm/projectinstance/update";
	/** delete instance service uri. */
	String PM_PROJECT_INSTANCE_DELETE_SERVICE = "/pm/projectinstance/delete";
}
