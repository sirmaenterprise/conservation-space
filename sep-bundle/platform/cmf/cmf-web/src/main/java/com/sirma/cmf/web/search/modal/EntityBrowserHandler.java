package com.sirma.cmf.web.search.modal;

/**
 * The Interface EntityBrowserHandler.This is a marker interface that should be implemented from all
 * action classes that are involved in handling of EntityBrowser operations.
 */
public interface EntityBrowserHandler {

	/**
	 * Can handle.
	 * 
	 * @param action
	 *            the action
	 * @return true, if successful
	 */
	boolean canHandle(com.sirma.itt.emf.security.model.Action action);

}
