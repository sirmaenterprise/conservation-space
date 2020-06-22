package com.sirma.itt.seip.serialization.kryo;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.serialization.SerializationEngine;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Utility for producing serialization engines based on {@link Kryo}. There are two supported uses of the library:
 * <ul>
 * <li>for long term serialization - Uses controlled class and field serialization via the {@link TaggedFieldSerializer}
 * . To register classes that are supported the extension {@link KryoInitializer} could be used.
 * <li>for short term serialization and object cloning/coping - Uses the default {@link FieldSerializer} with disabled
 * transient fields serialization to clone or deep copy instances.
 * </ul>
 * Because {@link Kryo} is not thread safe there are different {@link KryoPool} instances for both cases:
 * {@link #getSerializationPool()} and {@link #getClonePool()}
 *
 * @author BBonev
 */
@NoOperation
public class KryoHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int KRYO_ENGINE_CLASS_OFFSET = 100;

	private static final KryoHelper INSTANCE = new KryoHelper();
	private final List<Pair<Class<?>, Integer>> classRegister = new CopyOnWriteArrayList<>();
	private final CountDownLatch waitForClassRegisterToFinish = new CountDownLatch(1);

	/** {@link KryoFactory} that produces instances used for object cloning/copying */
	private final KryoFactory cloningKryoFactory = () -> {
		// use default engine configuration only for object cloning.
		Kryo kryo = new Kryo();
		// this is one custom serializer to disabled copying of transient fields
		kryo.setDefaultSerializer((engine, type) -> {
			FieldSerializer<Object> serializer = new FieldSerializer<>(engine, type);
			serializer.setCopyTransient(false);
			return serializer;
		});
		return kryo;
	};

	/** Kryo pool that stores engines used for object cloning/copying */
	private final KryoPool cloneEnginePool = new KryoPool.Builder(cloningKryoFactory).build();

	/**
	 * {@link KryoFactory} that produces instances that are configured for managed serializations via
	 * {@link KryoInitializer} extension. Instances serialized via instance of this factory should be deserializable in
	 * later builds
	 */
	private final KryoFactory serialyzingKryoFactory = () -> {
		Kryo kryo = new Kryo();
		kryo.setDefaultSerializer(TaggedFieldSerializer.class);
		// this should be turned off some time
		kryo.setRegistrationRequired(true);

		registerClasses(kryo);
		return kryo;
	};

	/** {@link KryoPool} for serialization {@link Kryo} instances */
	private final KryoPool serialyzingKryoPool = new KryoPool.Builder(serialyzingKryoFactory).build();

	/** Singleton pooled kryo engine instance */
	private final SerializationEngine pooledKryoEngine = new KryoPooledSerializationEngine(serialyzingKryoPool);

	@Inject
	private KryoInitializer kryoInitializer;

	/**
	 * Register {@link TaggedFieldSerializer} enabled classes to the given {@link Kryo} instance. The classes are
	 * collected via {@link KryoInitializer} extension.
	 *
	 * @param kryo
	 *            the kryo
	 */
	private void registerClasses(Kryo kryo) {
		try {
			LOGGER.debug("Awaiting for Kryo mapping to become available");
			if (!waitForClassRegisterToFinish.await(30, TimeUnit.MINUTES)) {
				throw new IllegalStateException("Kryo mapping was not initialized in 30 minutes. Something id wrong!");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EmfRuntimeException(e);
		}

		LOGGER.trace("Initializing Kryo instance. Registering {} classes", classRegister.size());

		if (classRegister.isEmpty()) {
			throw new EmfRuntimeException("No classes to register in the Kryo engine!");
		}

		for (Pair<Class<?>, Integer> objects : classRegister) {
			// use offset because Kryo has some default classes like String and primitives registered in the first
			// several positions
			int registerId = objects.getSecond() + KRYO_ENGINE_CLASS_OFFSET;

			if (objects instanceof Triplet) {
				Triplet<Class<?>, Integer, Serializer<?>> triplet = (Triplet<Class<?>, Integer, Serializer<?>>) objects;
				kryo.register(triplet.getFirst(), triplet.getThird(), registerId);
			} else {
				kryo.register(objects.getFirst(), registerId);
			}
		}
	}

	/**
	 * Initialize all {@link KryoInitializer} extensions and stores all defined mappings.
	 */
	@PostConstruct
	public void initializeKryoRegister() {
		Set<Pair<Class<?>, Integer>> temp = new TreeSet<>(Comparator.comparing(Pair::getSecond));
		// we will sort elements while collecting then we will add them the class register
		for (Pair<Class<?>, Integer> pair : kryoInitializer.getClassesToRegister()) {
			if (temp.contains(pair)) {
				throw new IllegalStateException("The class " + pair.getFirst() + " is already registered!");
			}
			temp.add(pair);
		}
		addToRegister(temp);

		// initialize the static instance that will be produced by CDI
		INSTANCE.addToRegister(temp);
	}

	private void addToRegister(Collection<Pair<Class<?>, Integer>> toRegister) {
		classRegister.clear();
		classRegister.addAll(toRegister);
		waitForClassRegisterToFinish.countDown();
	}

	@SuppressWarnings("unused")
	@Startup(phase = StartupPhase.DEPLOYMENT, transactionMode = TransactionMode.NOT_SUPPORTED)
	static void triggerInit(KryoHelper helper) {
		// trigger instance CDI instantiation on startup so if no other injects the instance it still be initialzied.
		helper.toString();
	}

	/**
	 * Gets the single instance of KryoHelper.
	 *
	 * @return single instance of KryoHelper
	 */
	@Produces
	@SuppressWarnings("static-method")
	public KryoHelper getInstance() {
		return getStaticInstance();
	}

	/**
	 * Gets the static instance. This method is intended where no CDI injection is possible. for all other cases use it
	 * as CDI injection.
	 *
	 * @return the static instance
	 */
	public static KryoHelper getStaticInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the serialization {@link KryoPool}. The used {@link Kryo} instances are configured via
	 * {@link KryoInitializer} extension plugin. The produced serialization should be stable between API releases and is
	 * intended for long term data storage.
	 *
	 * @return the serialization kryo pool
	 */
	public KryoPool getSerializationPool() {
		return serialyzingKryoPool;
	}

	/**
	 * Gets a {@link KryoPool} for cloning purposes. The pool uses {@link Kryo} instance that works as Java
	 * serialization (ignores transient fields).
	 *
	 * @return the clone pool
	 */
	public KryoPool getClonePool() {
		return cloneEnginePool;
	}

	/**
	 * Creates new {@link SerializationEngine} and holds a single {@link Kryo} instance and is not usable in multiple
	 * threads.
	 * <p>
	 * <b>NOTE: Do not share the instance between multiple threads</b>
	 * <p>
	 * For multi thread use {@link #getPooled()}
	 *
	 * @return the serialization engine
	 */
	public SerializationEngine createEngine() {
		return new KryoSerializationEngine(serialyzingKryoFactory.create());
	}

	/**
	 * Gets a pooled singleton serialization engine instance. The instance uses the {@link #getSerializationPool()} to
	 * do the needed serialization/deserialization and is thread safe.
	 *
	 * @return a singleton thread save serialization engine
	 */
	public SerializationEngine getPooled() {
		return pooledKryoEngine;
	}

	/**
	 * Gets the class registration id if registered or <code>null</code> if not.
	 *
	 * @param clazz
	 *            the class that need to fetch the registration
	 * @return the class registration id
	 */
	public Integer getClassRegistration(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		return getSerializationPool().run(kryo -> {
			Registration registration = kryo.getClassResolver().getRegistration(clazz);
			return registration == null ? null : registration.getId();
		});
	}

	/**
	 * Checks if is class registered.
	 *
	 * @param clazz
	 *            the clazz
	 * @return true, if is class registered
	 */
	public boolean isClassRegistered(Class<?> clazz) {
		return getClassRegistration(clazz) != null;
	}

	/**
	 * Gets the registered class to the given register id or <code>null</code> if nothing is registered.
	 *
	 * @param registerId
	 *            the class register id
	 * @return the registered class
	 */
	public Class<?> getRegisteredClass(Integer registerId) {
		if (registerId == null) {
			return null;
		}
		return getSerializationPool().run(kryo -> {
			Registration registration = kryo.getClassResolver().getRegistration(registerId);
			return registration == null ? null : registration.getType();
		});
	}
}
