package com.sirma.itt.seip.serialization.kryo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.serialization.SerializationEngine;

/**
 * Base class for implementing kryo serialization engines. Defines methods for serialization and deserialization using
 * passed Kryo engine.
 *
 * @author BBonev
 */
public abstract class BaseKryoSerializationEngine implements SerializationEngine {
	public static final int DEFAULT_BUFFER_SIZE = 10240;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Perform serialization of the given source object and using the passed {@link Kryo} instance.
	 *
	 * @param kryo
	 *            the {@link Kryo} instance to use for serialization
	 * @param byteBuffer
	 *            the byte array buffer size to use when serializing. If passed 0 or negative number the
	 *            {@link #DEFAULT_BUFFER_SIZE}={@value #DEFAULT_BUFFER_SIZE} will be used
	 * @param src
	 *            the object to serialize
	 * @return the serialized byte[] data.
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	protected byte[] doSerialization(Kryo kryo, int byteBuffer, Object src) {
		int bufferSize = byteBuffer;
		if (bufferSize <= 0) {
			bufferSize = DEFAULT_BUFFER_SIZE;
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(bufferSize);
		try (Output output = new Output(bytes)) {
			kryo.writeClassAndObject(output, src);
			// flushes the last data from the buffer to the output if not called not all data will
			// be send to the output. Instead of flush could be called directly close
			output.flush();
			LOGGER.trace("Serialized object: {} to {} bytes", src.getClass(), bytes.size());
			// the ByteArrayOutputStream is closed when the output is closed
			return bytes.toByteArray();
		} finally {
			// explicitly reset the engine to clear internal caches
			kryo.reset();
		}
	}

	/**
	 * Perform deserialization of the given serializable instance using the given {@link Kryo} instance. The allowed
	 * input for now is a byte[] or {@link InputStream}
	 *
	 * @param kryo
	 *            the {@link Kryo} instance to use for deserialization
	 * @param src
	 *            the object to deserialize
	 * @return the deserialized object instance
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	protected Object doDeserialize(Kryo kryo, Serializable src) {
		Input input = null;
		if (src instanceof byte[]) {
			byte[] bytes = (byte[]) src;
			LOGGER.trace("Deserializing from a byte[] with lenght {}", bytes.length);

			if (bytes.length == 0) {
				return null;
			}
			// read the input bytes using stream because specific implementation of Kryo that modifies the input byte
			// array when reading strings/ascii
			// https://github.com/EsotericSoftware/kryo/issues/128
			input = new Input(Arrays.copyOf(bytes, bytes.length));
		} else if (src instanceof InputStream) {
			input = new Input((InputStream) src);
		}
		if (input == null) {
			throw new EmfRuntimeException("Cannot convert serializable that is not byte[] or InputStream but is: "
					+ (src == null ? "null" : src.getClass()));
		}
		// try reading the input
		try (Input toRead = input) {
			return kryo.readClassAndObject(toRead);
		} finally {
			kryo.reset();
			input = null;
		}
	}
}
