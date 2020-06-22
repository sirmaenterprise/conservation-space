package com.sirma.itt.seip.serialization;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.serialization.kryo.KryoHelper;

/**
 * Utility class that provides methods for custom serialization and deserialization of objects.
 *
 * @author BBonev
 */
@Singleton
public class SerializationHelper {

	private KryoHelper kryoHelper;

	@Inject
	public SerializationHelper(KryoHelper kryoHelper) {
		this.kryoHelper = kryoHelper;
	}

	/**
	 * Gets the default serialization engine. The current default is the one returned by the
	 * {@link KryoHelper#getPooled()}.
	 *
	 * @return the default kryo serialization engine
	 */
	public SerializationEngine getDefaultSerializationEngine() {
		return kryoHelper.getPooled();
	}

	/**
	 * Serialize the given object using the default serialization engine. If the engine is not provided then the default
	 * serialization engine will be used.
	 *
	 * @param src
	 *            the object to serialize
	 * @return the serialized object
	 * @see #getDefaultSerializationEngine()
	 */
	public Serializable serialize(Object src) {
		return serialize(src, null);
	}

	/**
	 * Serialize the given object and the specified engine. If the engine is not provided then the default serialization
	 * engine will be used.
	 *
	 * @param src
	 *            the object to serialize
	 * @param engine
	 *            the engine to use (optional)
	 * @return the serialized object
	 */
	public Serializable serialize(Object src, SerializationEngine engine) {
		SerializationEngine serializationEngine = engine;
		if (engine == null) {
			serializationEngine = getDefaultSerializationEngine();
		}
		return serializationEngine.serialize(src);
	}

	/**
	 * Deserialize the given object using the default serialization engine.
	 * <p>
	 * <b>Note: </b> The object should be serialized via the same engine implementation!
	 *
	 * @param src
	 *            the source object to deserialize
	 * @return the deserialized object
	 */
	public Object deserialize(Serializable src) {
		return deserialize(src, null);
	}

	/**
	 * Deserialize the given object using the specified engine. If the engine is not provided then the default
	 * serialization engine will be used.
	 * <p>
	 * <b>Note: </b> The object should be serialized via the same engine implementation!
	 *
	 * @param src
	 *            the source object to deserialize
	 * @param engine
	 *            the engine to use (optional)
	 * @return the deserialized object
	 */
	public Object deserialize(Serializable src, SerializationEngine engine) {
		SerializationEngine serializationEngine = engine;
		if (engine == null) {
			serializationEngine = getDefaultSerializationEngine();
		}
		return serializationEngine.deserialize(src);
	}

	/**
	 * Perform deep copy of the specified object.
	 *
	 * @param <T>
	 *            the object type to copy
	 * @param source
	 *            object to copy
	 * @return a deep object copy
	 */
	public <T> T copy(T source) {
		return kryoHelper.getClonePool().run(kryo -> kryo.copy(source));
	}
}
