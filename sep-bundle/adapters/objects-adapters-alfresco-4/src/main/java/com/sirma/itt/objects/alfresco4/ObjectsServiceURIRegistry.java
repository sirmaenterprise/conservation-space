package com.sirma.itt.objects.alfresco4;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;

/**
 * The Interface ServiceURIRegistry holds uri for alfresco services.
 * 
 * @author BBonev
 */
public interface ObjectsServiceURIRegistry extends ServiceURIRegistry {

	/** The object definitions search. */
	String OBJECT_DEFINITIONS_SEARCH = "/dom/search/definitions/objects";

	/** The create object service. */
	String CREATE_OBJECT_SERVICE = "/dom/objectinstance/create";
	/** The update object service. */
	String UPDATE_OBJECT_SERVICE = "/dom/objectinstance/update";
	/** The delete object service. */
	String DELETE_OBJECT_SERVICE = "/dom/objectinstance/delete";

}
