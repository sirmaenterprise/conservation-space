package com.sirmaenterprise.sep.jms.convert;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link MessageWriter} store for registered writers. <br>
 * Any writers registered via the method
 * {@link #register(Class, Class)} will be instantiated via the registered {@link MessageWriterBuilder}s. If non
 * builder is registered the default builder will be used. It tries to call the default no arg constructor of the
 * given writer class. If it fails to do so the writer will be considered invalid and message send will fail. The
 * instantiated writers will be cashed and reused. If this behaviour is problematic thet should not be registred but
 * used oer message send.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 23/05/2017
 */
@ApplicationScoped
public class MessageWriters {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final MessageWriterBuilder DEFAULT_BUILDER = new ReflectionMessageWriterBuilder();

	private List<MessageWriterBuilder> builders = new CopyOnWriteArrayList<>();

	private Map<Class, MessageWriter> writerInstances = new ConcurrentHashMap<>();
	private Map<Class, Class<? extends MessageWriter>> writerClasses = new ConcurrentHashMap<>();

	/**
	 * Register a MessageWriter class that should be used to convert the given data type on message send when no
	 * explicit writer is passed
	 *
	 * @param dataType the data type that need to be converted by the given writer
	 * @param writerClass the writer class to register. It should be instantiable via one of the registered
	 * {@link MessageWriterBuilder}s
	 */
	public void register(Class<?> dataType, Class<? extends MessageWriter> writerClass) {
		if (writerClass == null) {
			return;
		}
		addNonNullValue(writerClasses, dataType, writerClass);
	}

	/**
	 * Register generic message writer instance to be used for converting the given data type to {@link Message}
	 * .<p>Note that writer registered via this method will precede a writer registered via
	 * {@link #register(Class, Class)} for the same data type</p>
	 *
	 * @param dataType the data type that need to be converted by the given writer
	 * @param writer the writer instance to use
	 * @param <T> the data type, used to link the generics of the data and the writer
	 */
	public <T, M extends Message> void register(Class<T> dataType, MessageWriter<T, M> writer) {
		addNonNullValue(writerInstances, dataType, writer);
	}

	/**
	 * Resolve {@link MessageWriter} for the given type.
	 *
	 * @param dataType the data type that needs to be converted
	 * @param <T> the writer type
	 * @param <M> the message type
	 * @return the found writer or null if no one is found or could not instantiate it if present
	 */
	@SuppressWarnings("unchecked")
	public <T, M extends Message> Optional<MessageWriter<T, M>> getWriterFor(Class<T> dataType) {
		return Optional.ofNullable(writerInstances.computeIfAbsent(dataType,
				type -> buildWriterFor(type, writerClasses::get)));
	}

	private <W extends MessageWriter<?, ?>> W buildWriterFor(Class type, Function<Class, Class<? extends
			W>> writerProvider) {
		Class<? extends W> writerClass = writerProvider.apply(type);
		if (writerClass != null) {
			return buildWriter(writerClass).orElse(null);
		}
		return null;
	}

	/**
	 * Build a writer instance using the registered {@link MessageWriterBuilder}s
	 *
	 * @param writerClass the writer class to build
	 * @return the build writer if possible or empty optional if not
	 */
	public <W extends MessageWriter> Optional<W> buildWriter(Class<W> writerClass) {
		return Stream.concat(builders.stream(), Stream.of(DEFAULT_BUILDER))
				.map(callBuilder(writerClass))
				.filter(Objects::nonNull)
				.map(writerClass::cast)
				.findFirst();
	}

	private static Function<MessageWriterBuilder, MessageWriter> callBuilder(Class<? extends MessageWriter>
			writerClass) {
		return builder -> {
			try {
				return builder.build(writerClass);
			} catch (RuntimeException e) {
				LOGGER.trace("Failed building writer {} using {}", writerClass, builder, e);
			}
			return null;
		};
	}

	/**
	 * Register a {@link MessageWriterBuilder} instance to the list of builders that are used for
	 * {@link MessageWriter} instantiation
	 *
	 * @param builder the build to register
	 */
	public void addWriterBuilder(MessageWriterBuilder builder) {
		addNonNullValue(builders, builder);
	}

	/**
	 * {@link MessageWriterBuilder} that uses the default class constructor to instantiate the writer class
	 *
	 * @author BBonev
	 */
	private static class ReflectionMessageWriterBuilder implements MessageWriterBuilder {
		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

		@Override
		public MessageWriter build(Class<? extends MessageWriter> writerClass) {
			try {
				Constructor<? extends MessageWriter> constructor = writerClass.getDeclaredConstructor(
						EMPTY_CLASS_ARRAY);
				constructor.setAccessible(true);
				return constructor.newInstance((Object[]) null);
			} catch (RuntimeException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
				LOGGER.trace("Cannot instantiate writer", e);
				return null;
			} catch (InvocationTargetException e) { // NOSONAR
				throw new IllegalStateException(e.getTargetException());
			}
		}
	}
}
