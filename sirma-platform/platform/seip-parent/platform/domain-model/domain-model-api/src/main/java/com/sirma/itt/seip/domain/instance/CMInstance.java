package com.sirma.itt.seip.domain.instance;

/**
 * The Interface CMInstance marks an instance that has a content management id representation. The method
 * {@link #getContentManagementId()} should return the id in content management id
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

	/**
	 * Sets the content management id to the given object if the object implements the {@link CMInstance} interface.
	 *
	 * @param target
	 *            the target
	 * @param newId
	 *            the new id to set
	 */
	static void setContentManagementId(Object target, String newId) {
		if (target instanceof CMInstance) {
			((CMInstance) target).setContentManagementId(newId);
		}
	}

	/**
	 * Gets the content management id to the given object if the object implements the {@link CMInstance} interface.
	 *
	 * @param target
	 *            the target
	 * @param defaultValue
	 *            to return if the the given target is not instance of {@link CMInstance}.
	 * @return the content management id or the default value if the object is not of type {@link CMInstance}
	 */
	static String getContentManagementId(Object target, String defaultValue) {
		if (target instanceof CMInstance) {
			return ((CMInstance) target).getContentManagementId();
		}
		return defaultValue;
	}
}
