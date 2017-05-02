package com.sirma.itt.emf.semantic.security;

/**
 * Request to change the parent inheritance to the given new parent
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/04/2017
 */
public class ParentChangeRequest extends PermissionChangeRequest {
	/**
	 * Instantiates a new permission change request.
	 */
	ParentChangeRequest() {
		// needed by Kryo to instantiate the instance
	}

	/**
	 * Initialize the parent change request
	 *
	 * @param targetInstance the affected instance
	 * @param newParent the new parent to set, if null the parent will be removed
	 */
	ParentChangeRequest(String targetInstance, String newParent) {
		super(targetInstance, newParent);
	}
}
