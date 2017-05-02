package com.sirma.itt.seip.testutil.mocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.configuration.ConfigurationChangeListener;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.Synchronizer;

/**
 * Simple configuration property mock
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
public class ConfigurationPropertyMock<T> implements ConfigurationProperty<T> {

	private static final long serialVersionUID = 1L;

	private Supplier<T> supplier;

	/** The is set. */
	private boolean isSet;

	private ConfigurationChangeListener<T> configurationChangeListener;

	private Consumer<T> destroyer;

	private String name;

	private ConfigurationInstance definition;

	public ConfigurationPropertyMock() {
		this(() -> null);
	}

	/**
	 * Instantiates a new configuration property mock.
	 *
	 * @param value
	 *            the value
	 */
	public ConfigurationPropertyMock(T value) {
		this(() -> value);
	}

	/**
	 * Instantiates a new configuration property mock.
	 *
	 * @param value
	 *            the value
	 * @param isSet
	 *            the is set
	 */
	public ConfigurationPropertyMock(T value, boolean isSet) {
		this(() -> value);
		this.isSet = isSet;
	}

	/**
	 * Instantiates a new configuration property mock.
	 *
	 * @param supplier
	 *            the supplier
	 */
	public ConfigurationPropertyMock(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Instantiates a new configuration property mock.
	 *
	 * @param valueBuilder
	 *            the value builder
	 * @param target
	 *            the target
	 * @param args
	 *            the args
	 */
	public ConfigurationPropertyMock(Method valueBuilder, Object target, Object... args) {
		setSupplier(valueBuilder, target, args);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public T get() {
		return supplier.get();
	}

	@Override
	public boolean isSet() {
		return supplier.get() != null || isSet;
	}

	@Override
	public Synchronizer getSynchronizer() {
		// implement me!
		return null;
	}

	@Override
	public ConfigurationInstance getDefinition() {
		return definition;
	}

	/**
	 * Sets the definition.
	 *
	 * @param definition
	 *            the definition to set
	 * @return the current instance to allow method chaining
	 */
	public ConfigurationPropertyMock<T> setDefinition(ConfigurationInstance definition) {
		this.definition = definition;
		return this;
	}

	@Override
	public void valueUpdated() {
		if (configurationChangeListener != null) {
			configurationChangeListener.onConfigurationChange(this);
		}
	}

	@Override
	public void addConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		this.configurationChangeListener = callback;
	}

	@Override
	public void removeConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		configurationChangeListener = null;
	}

	public void setValue(T value) {
		supplier = () -> value;
		valueUpdated();
	}

	public void setSupplier(Supplier<T> value) {
		supplier = value;
	}

	/**
	 * Sets the supplier that will call the given converter method to produce the value
	 *
	 * @param valueBuilder
	 *            the value builder
	 * @param target
	 *            the target
	 * @param args
	 *            the args
	 */
	@SuppressWarnings("unchecked")
	public void setSupplier(Method valueBuilder, Object target, Object... args) {
		supplier = new CachingSupplier<>(() -> {
			try {
				valueBuilder.setAccessible(true);
				return (T) valueBuilder.invoke(target, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		});
	}

	@Override
	public void addValueDestroyListener(Consumer<T> oldValueConsumer) {
		this.destroyer = oldValueConsumer;
	}

	/**
	 * Invokes the registered consumer for value destruction
	 */
	public void destroy() {
		if (destroyer != null) {
			destroyer.accept(get());
		}
	}

	@Override
	public boolean isInitialized() {
		return isSet;
	}

}
