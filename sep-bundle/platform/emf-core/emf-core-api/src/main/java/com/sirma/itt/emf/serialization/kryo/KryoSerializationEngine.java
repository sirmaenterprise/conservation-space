package com.sirma.itt.emf.serialization.kryo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.serialization.SerializationEngine;
import com.sirma.itt.emf.serialization.SerializationUtil;

/**
 * Serialization engine implementation for Kryo library
 * 
 * @author BBonev
 */
public class KryoSerializationEngine implements SerializationEngine {

	private static final int DEFAULT_BUFFER_SIZE = 10240;

	private static final Logger LOGGER = LoggerFactory.getLogger(KryoSerializationEngine.class);

	private static final boolean trace = LOGGER.isTraceEnabled();

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
	@SuppressWarnings("resource")
	public Serializable serialize(Object src) {
		if (trace) {
			LOGGER.trace("Deserializing from engine with hash=" + hashCode());
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(determineSize(src));
		Output output = new Output(bytes);
		try {
			kryo.writeClassAndObject(output, src);
			// flushes the last data from the buffer to the output if not called not all data will
			// be send to the output. Instead of flush could be called directly close
			output.flush();
			if (trace) {
				LOGGER.trace("Serialized object: " + src.getClass() + " to " + bytes.size()
						+ " bytes");
			}
			// the ByteArrayOutputStream is closed when the output is closed
			return bytes.toByteArray();
		} finally {
			// explicitly reset the engine to clear internal caches
			kryo.reset();
			output.close();
		}
	}

	/**
	 * Determine size based on the source object. For cases and workflows the size should be larger
	 * to minimize memory moving. The default size is 10KB.
	 * 
	 * @param src
	 *            the source object
	 * @return the calculated size.
	 */
	private int determineSize(Object src) {
		// // for cases and workflows we create bigger buffer for more to be more efficient
		// if ((src instanceof CaseDefinition) || (src instanceof WorkflowDefinition)) {
		// return DEFAULT_BUFFER_SIZE * 10;
		// }// TODO optimize somehow
		return DEFAULT_BUFFER_SIZE;
	}

	@Override
	public Object deserialize(Serializable src) {
		if (trace) {
			LOGGER.trace("Deserializing from engine with hash=" + hashCode());
		}
		Input input = null;
		if (src instanceof byte[]) {
			byte[] bytes = (byte[]) src;
			if (trace) {
				LOGGER.trace("Deserializing from a byte[] with lenght " + bytes.length);
			}
			if (bytes.length == 0) {
				return null;
			}
			// inputStream = new ByteArrayInputStream((byte[]) src);
			input = new Input(bytes);
		} else if (src instanceof InputStream) {
			input = new Input((InputStream) src);
		}
		if (input == null) {
			throw new EmfRuntimeException(
					"Cannot convert serializable that is not byte[] or InputStream but is: "
							+ ((src == null) ? "null" : src.getClass()));
		}
		// try reading the input
		try {
			Object object = kryo.readClassAndObject(input);
			return object;
		} finally {
			kryo.reset();
			input.close();
			input = null;
		}
	}

	/**
	 * Getter method for kryo.
	 * 
	 * @return the kryo
	 */
	public Kryo getKryoInstance() {
		return kryo;
	}

	/**
	 * Gets thread local fully initialized instance of KryoSerializationEngine.
	 * 
	 * @return instance of KryoSerializationEngine
	 */
	public static KryoSerializationEngine getInstance() {
		SerializationEngine engine = SerializationUtil.getCachedSerializationEngine();
		if (engine instanceof KryoSerializationEngine) {
			return (KryoSerializationEngine) engine;
		}
		throw new EmfConfigurationException("The default engine is not a KryoSerializationEngine!");
	}

	/**
	 * Gets the class registration id if registered or <code>null</code> if not.
	 * 
	 * @param clazz
	 *            the class that need to fetch the registration
	 * @return the class registration id
	 */
	public static Integer getClassRegistration(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		Kryo instance = getInstance().getKryoInstance();
		Registration registration = instance.getClassResolver().getRegistration(clazz);
		return registration == null ? null : registration.getId();
	}

	/**
	 * Gets the registered class to the given register id or <code>null</code> if nothing is
	 * registered.
	 * 
	 * @param registerId
	 *            the class register id
	 * @return the registered class
	 */
	public static Class<?> getRegisteredClass(Integer registerId) {
		if (registerId == null) {
			return null;
		}
		Kryo instance = getInstance().getKryoInstance();
		Registration registration = instance.getClassResolver().getRegistration(registerId);
		return registration == null ? null : registration.getType();
	}

}