package com.sirma.itt.seip.security.mocks;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sirma.itt.seip.configuration.ConfigurationChangeListener;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.Synchronizer;

/**
 * Simple configuration property mock.
 *
 * @author BBonev
 * @param <T>            the generic type
 */
public class ConfigurationPropertyMock<T> implements ConfigurationProperty<T> {

	private Supplier<T> supplier;

	/**
	 * Instantiates a new configuration property mock.
	 */
	public ConfigurationPropertyMock() {

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
	 * @param supplier
	 *            the supplier
	 */
	public ConfigurationPropertyMock(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		// implement me!
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		return supplier.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSet() {
		return supplier.get() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Synchronizer getSynchronizer() {
		// implement me!
		return null;
	}

	@Override
	public ConfigurationInstance getDefinition() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void valueUpdated() {
		// nothing to do property value is always fetched
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		// implement me!

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		// implement me!

	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(T value) {
		supplier = () -> value;
	}

	/**
	 * Sets the supplier.
	 *
	 * @param value the new supplier
	 */
	public void setSupplier(Supplier<T> value) {
		supplier = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addValueDestroyListener(Consumer<T> oldValueConsumer) {
		// implement me!

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInitialized() {
		return true;
	}

}
