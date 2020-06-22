package com.sirma.itt.seip.serialization;

import java.io.Serializable;

/**
 * Generic serialization engine. Provides methods for serialization between object and serializable and reverse.
 *
 * @author BBonev
 */
public interface SerializationEngine {

	/**
	 * Serialize the given object based on the concrete serialization engine. The object could be deserialized via
	 * {@link #deserialize(Serializable)} method.
	 *
	 * @param src
	 *            the source object that needs to be serialized.
	 * @return the serializable object
	 */
	Serializable serialize(Object src);

	/**
	 * Deserialize an object serialized via {@link #serialize(Object)} method.
	 *
	 * @param src
	 *            the source object to deserialize.
	 * @return the object
	 */
	Object deserialize(Serializable src);
}