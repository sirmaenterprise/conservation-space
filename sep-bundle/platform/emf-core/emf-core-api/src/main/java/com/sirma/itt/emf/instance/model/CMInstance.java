package com.sirma.itt.emf.instance.model;

/**
 * The Interface CMInstance marks an instance that has a content management id representation. The
 * method {@link #getContentManagementId()} should return the id in content management id
 * 
 * @author BBonev
 */
public interface CMInstance {

	/**
	 * Gets the content management id.
	 * 
	 * @return the content management id
	 */
	String getContentManagementId();

	/**
	 * Sets the content management id.
	 * 
	 * @param contentManagementId
	 *            the new content management id
	 */
	void setContentManagementId(String contentManagementId);
}
