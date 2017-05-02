package com.sirma.itt.seip.serialization.kryo;

import java.io.Serializable;

import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * Serialization engine implementation for Kryo library that uses a {@link KryoPool} instead of single
 * {@link com.esotericsoftware.kryo.Kryo} instance.
 *
 * @author BBonev
 */
public class KryoPooledSerializationEngine extends BaseKryoSerializationEngine {
	private final KryoPool pool;

	/**
	 * Instantiates a new kryo pooled serialization engine.
	 *
	 * @param pool
	 *            the pool instance to use
	 */
	public KryoPooledSerializationEngine(KryoPool pool) {
		this.pool = pool;
	}

	@Override
	public Serializable serialize(Object src) {
		return pool.run(kryo -> doSerialization(kryo, -1, src));
	}

	@Override
	public Object deserialize(Serializable src) {
		return pool.run(kryo -> doDeserialize(kryo, src));
	}
}