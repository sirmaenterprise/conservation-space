package com.sirma.itt.seip.serialization.kryo;

import java.io.Serializable;

import com.esotericsoftware.kryo.Kryo;

/**
 * Serialization engine implementation for Kryo library
 *
 * @author BBonev
 */
public class KryoSerializationEngine extends BaseKryoSerializationEngine {
	private final Kryo kryo;

	/**
	 * Instantiates a new kryo serialization engine.
	 *
	 * @param kryo
	 *            the kryo
	 */
	public KryoSerializationEngine(Kryo kryo) {
		this.kryo = kryo;
	}

	@Override
	public Serializable serialize(Object src) {
		return doSerialization(kryo, -1, src);
	}

	@Override
	public Object deserialize(Serializable src) {
		return doDeserialize(kryo, src);
	}
}