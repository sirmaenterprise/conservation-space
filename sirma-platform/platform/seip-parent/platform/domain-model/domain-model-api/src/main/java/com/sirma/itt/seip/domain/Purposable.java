package com.sirma.itt.seip.domain;

/**
 * Interface that defines a means to filer objects/definitions by purpose.
 *
 * @author BBonev
 */
public interface Purposable {

	/**
	 * Gets the purpose.
	 *
	 * @return the purpose
	 */
	String getPurpose();

	/**
	 * Sets the purpose.
	 *
	 * @param purpose
	 *            the new purpose
	 */
	void setPurpose(String purpose);

	/**
	 * Gets the purpose from the given source object if the object implements the {@link Purposable} interface.
	 * Otherwise the given default value will be returned.
	 *
	 * @param source
	 *            the source object to extract the purpose from
	 * @param defaultValue
	 *            value to return if the given source object is not type of {@link Purposable}.
	 * @return the purpose of the source object or the default value
	 */
	static String getPurpose(Object source, String defaultValue) {
		if (source instanceof Purposable) {
			return ((Purposable) source).getPurpose();
		}
		return defaultValue;
	}
}
