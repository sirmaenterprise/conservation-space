package com.sirma.itt.emf.serialization;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.event.ApplicationInitializationEvent;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.serialization.kryo.KryoInitializer;
import com.sirma.itt.emf.serialization.kryo.KryoSerializationEngine;

/**
 * Utility class that provides methods for custom serialization and deserialization of objects. <br>
 * REVIEW: separate the serialization utility and Kryo engine initialization in separate classes.
 * 
 * @author BBonev
 */
public class SerializationUtil {

	private static final int KRYO_ENGINE_CLASS_OFFSET = 100;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SerializationUtil.class);

	/** The Constant trace. */
	private static final boolean trace = LOGGER.isTraceEnabled();

	@Inject
	@ExtensionPoint(KryoInitializer.TARGET_NAME)
	private Iterable<KryoInitializer> kryoInitializers;

	/** The Constant CLASS_REGISTER. */
	private static final List<Pair<Class<?>, Integer>> CLASS_REGISTER = new LinkedList<>();

	/** The Constant SERIALIZATION_ENGINE. */
	private static final String SERIALIZATION_ENGINE = RuntimeConfigurationProperties.SERIALIZATION_ENGINE;

	/** The Constant CLONE_ENGINE_HOLDER. */
	private static final ThreadLocal<Kryo> CLONE_ENGINE_HOLDER = new ThreadLocal<Kryo>() {
		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}
	};

	private static Lock createLock = new ReentrantLock();

	/**
	 * Initialize the admin user information.
	 */
	@PostConstruct
	public void init() {
		Set<Pair<Class<?>, Integer>> temp = new TreeSet<>(
				new Comparator<Pair<Class<?>, Integer>>() {

					@Override
					public int compare(Pair<Class<?>, Integer> o1, Pair<Class<?>, Integer> o2) {
						return o1.getSecond().compareTo(o2.getSecond());
					}
				});
		// we will sort elements while collecting then we will add them the class register
		for (KryoInitializer initializer : kryoInitializers) {
			List<Pair<Class<?>, Integer>> list = initializer.getClassesToRegister();
			for (Pair<Class<?>, Integer> pair : list) {
				if (temp.contains(pair)) {
					throw new EmfConfigurationException("The class " + pair.getFirst()
							+ " is already registered!");
				}
				temp.add(pair);
			}
		}
		CLASS_REGISTER.addAll(temp);
	}

	/**
	 * On application start.
	 * 
	 * @param event
	 *            the event
	 */
	public void onApplicationStart(@Observes ApplicationInitializationEvent event) {
		// trigger instance initialization on application start
	}

	/**
	 * Creates the default serialization engine.
	 * 
	 * @return the serialization engine
	 */
	@SuppressWarnings("unchecked")
	public static SerializationEngine createDefaultSerializationEngine() {
		Kryo kryo = new Kryo();
		kryo.setDefaultSerializer(TaggedFieldSerializer.class);
		// this should be turned off some time
		kryo.setRegistrationRequired(true);

		if (trace) {
			LOGGER.trace("Initializing Kryo instance. Registering " + CLASS_REGISTER.size()
					+ " classes");
		}
		if (CLASS_REGISTER.isEmpty()) {
			throw new EmfRuntimeException("No classes to register in the Kryo engine!");
		}

		for (Pair<Class<?>, Integer> objects : CLASS_REGISTER) {
			if (objects instanceof Triplet) {
				Triplet<Class<?>, Integer, Serializer<?>> triplet = (Triplet<Class<?>, Integer, Serializer<?>>) objects;
				kryo.register(triplet.getFirst(), triplet.getThird(), triplet.getSecond()
						+ KRYO_ENGINE_CLASS_OFFSET);
			} else {
				kryo.register(objects.getFirst(), objects.getSecond() + KRYO_ENGINE_CLASS_OFFSET);
			}
		}
		return new KryoSerializationEngine(kryo);
	}

	/**
	 * Gets the cached serialization engine. The engine is cached in the thread local variables. If
	 * not present new default serialization engine is created.
	 * 
	 * @return the cached serialization engine
	 */
	public static SerializationEngine getCachedSerializationEngine() {
		if (RuntimeConfiguration.isConfigurationSet(SERIALIZATION_ENGINE)) {
			return ((EngineHolder) RuntimeConfiguration.getConfiguration(SERIALIZATION_ENGINE)).engine;
		}
		try {
			// lock the new engine creation to limit possible duplicates in same cases
			createLock.lock();
			SerializationEngine engine = createDefaultSerializationEngine();
			EngineHolder engineHolder = new EngineHolder(engine);
			RuntimeConfiguration.setConfiguration(SERIALIZATION_ENGINE, engineHolder);
			if (trace) {
				LOGGER.trace("Created new SE=" + engine + " for thread current thread "
						+ Thread.currentThread().getName());
			}
			return engine;
		} finally {
			createLock.unlock();
		}
	}

	/**
	 * Serialize the given object and the specified engine. If the engine is not provided then the
	 * default serialization engine will be used.
	 * 
	 * @param src
	 *            the object to serialize
	 * @param engine
	 *            the engine to use (optional)
	 * @return the serialized object
	 */
	public static Serializable serialize(Object src, SerializationEngine engine) {
		SerializationEngine serializationEngine = engine;
		if (engine == null) {
			serializationEngine = getCachedSerializationEngine();
		}
		return serializationEngine.serialize(src);
	}

	/**
	 * Deserialize the given object using the specified engine. If the engine is not provided then
	 * the default serialization engine will be used.
	 * <p>
	 * <b>Note: </b> The object should be serialized via the same engine implementation!
	 * 
	 * @param src
	 *            the source object to deserialize
	 * @param engine
	 *            the engine to use (optional)
	 * @return the deserialized object
	 */
	public static Object deserialize(Serializable src, SerializationEngine engine) {
		SerializationEngine serializationEngine = engine;
		if (engine == null) {
			serializationEngine = getCachedSerializationEngine();
		}
		try {
			return serializationEngine.deserialize(src);
		} catch (Exception e) {
			// if there is an error deserializing with the engine will try to create new instance
			// and try again
			SerializationEngine newEngineInstance;
			createLock.lock();
			try {
				newEngineInstance = createDefaultSerializationEngine();
			} finally {
				createLock.unlock();
			}
			// check if the engines are of the same type
			if (serializationEngine.getClass().equals(newEngineInstance.getClass())) {
				if (trace) {
					LOGGER.trace("Initial deserialization failed. Will try again with new instance!");
				}
				return newEngineInstance.deserialize(src);
			}
			throw e;
		}
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
	public static <T> T copy(T source) {
		Kryo kryo = getCloneEngine();
		return kryo.copy(source);
	}

	/**
	 * Gets the engine.
	 * 
	 * @return the engine
	 */
	private static Kryo getCloneEngine() {
		return CLONE_ENGINE_HOLDER.get();
	}

	/**
	 * Serializable class to wrap the non-serializable Kryo instance.
	 * 
	 * @author BBonev
	 */
	private static class EngineHolder implements Serializable {
		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -8766181815762495316L;
		/** The engine. */
		SerializationEngine engine;

		/**
		 * Instantiates a new engine holder.
		 * 
		 * @param engine
		 *            the engine
		 */
		public EngineHolder(SerializationEngine engine) {
			this.engine = engine;
		}

		@Override
		public String toString() {
			return "#SerializationEngine@" + engine.hashCode();
		}
	}
}
