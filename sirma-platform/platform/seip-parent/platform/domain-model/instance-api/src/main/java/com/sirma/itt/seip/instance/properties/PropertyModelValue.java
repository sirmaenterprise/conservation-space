package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;

/**
 * The implementation should provide means to accessing the actual value stored in the instance of this object.
 *
 * @author BBonev
 */
public interface PropertyModelValue extends Serializable {

	/**
	 * Fetches the value as a desired type. Collections (i.e. multi-valued properties) will be converted as a whole to
	 * ensure that all the values returned within the collection match the given type.
	 *
	 * @param typeName
	 *            the type required for the return value
	 * @return Returns the value of this property as the desired type, or a <code>Collection</code> of values of the
	 *         required type
	 * @see com.sirma.itt.seip.domain.definition.DataTypeDefinition#ANY The static qualified names for the types
	 */
	Serializable getValue(String typeName);
}
