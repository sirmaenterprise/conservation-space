package com.sirma.itt.seip;

import java.io.Serializable;

/**
 * Interface that identifies a unique semantic identifier that to be common for all semantic implementations. Based on
 * the Open RDF URI definition.
 *
 * @author BBonev
 */
public interface Uri extends Serializable {

	/**
	 * Gets the namespace.
	 *
	 * @return the namespace
	 */
	String getNamespace();

	/**
	 * Gets the local name.
	 *
	 * @return the local name
	 */
	String getLocalName();

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	String toString();

	/**
	 * Equals.
	 *
	 * @param paramObject
	 *            the param object
	 * @return true, if successful
	 */
	@Override
	boolean equals(Object paramObject);

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	int hashCode();
}
