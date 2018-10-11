package com.sirma.itt.seip.configuration;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.sync.Synchronizer;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default reloadable configuration property implementation.
 *
 * @param <T>
 *            the property type
 */
public class Property<T> implements ConfigurationProperty<T>, Destroyable {
	private static final long serialVersionUID = -5701880420055145025L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private T value;
	private final String name;
	private final Synchronizer sync;
	private final List<ConfigurationChangeListener<T>> callbacks = new CopyOnWriteArrayList<>();
	private final List<Consumer<T>> finalizers = new CopyOnWriteArrayList<>();
	private final Supplier<T> valueSupplier;
	private volatile boolean valueLoaded = false;
	private final ConfigurationInstance definition;

	/**
	 * Instantiates a new property.
	 *
	 * @param instance
	 *            the configuration definition instance. Should be the definition of the configuration that defines the
	 *            current configuration
	 * @param sync
	 *            the sync
	 * @param valueSupplier
	 *            the value supplier
	 */
	public Property(ConfigurationInstance instance, Synchronizer sync, Supplier<T> valueSupplier) {
		if (instance == null || sync == null || valueSupplier == null) {
			throw new IllegalArgumentException("Name and synchronizer and supplier are required");
		}
		this.definition = instance;
		this.name = instance.getName();
		this.sync = sync;
		this.valueSupplier = valueSupplier;
	}

	@Override
	public T get() {
		return getValue();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSet() {
		return getValue() != null;
	}

	@Override
	public Synchronizer getSynchronizer() {
		return sync;
	}

	@Override
	public ConfigurationInstance getDefinition() {
		return definition;
	}

	@Override
	public void valueUpdated() {
		// do reloading only if the value was loaded
		if (valueLoaded) {
			sync.beginWrite();
			try {
				loadConfigurationValue(true);
			} finally {
				sync.endWrite();
			}
		}
	}

	/**
	 * Gets the cached value or retrieves new one from the supplier depending on the value of {@link #valueLoaded}
	 *
	 * @return the value
	 */
	protected T getValue() {
		if (valueLoaded) {
			return value;
		}
		sync.beginWrite();
		try {
			// verify that the value was not created while we were waiting to enter
			if (valueLoaded) {
				return value;
			}

			loadConfigurationValue(false);
			LOGGER.trace("Loaded configuration value {}={}", name, value);
			return value;
		} finally {
			sync.endWrite();
		}
	}

	private void loadConfigurationValue(boolean notifyForChangeIfNeeded) {
		T newValue;
		try {
			newValue = valueSupplier.get();
		} catch (ConverterException e) {
			Throwable cause = e.getCause();
			LOGGER.warn("Could not load value for configuration {}, due to {}", getName(),
					e.getMessage() + (cause != null ? " -> " + cause.getMessage() : ""));
			LOGGER.trace("Could not load value for configuration {}, due to:", getName(), e);
			// on next use we will try to load it again
			setNotLoaded();
			return;
		}

		// get the old value to check it with the new one and to allow disposition of if if needed.
		T oldValue = value;

		// set the value as loaded before continue it need to be set before the notifications need to be send
		// it's here because no matter what happens bellow the value is loaded or will be loaded
		setLoaded();

		boolean isSame = EqualsHelper.nullSafeEquals(oldValue, newValue);
		if (isSame) {
			LOGGER.trace("On reload of {} found that old value and new value are equal to {}", name, value);
			// dispose the newly created value if we are not going to use it
			notifyForValueDisposal(newValue);
		} else {
			LOGGER.trace("On reload of {} detected change in value:\n\tNew value={}\n\tOld value={}", name, newValue,
					oldValue);
			notifyForValueDisposal(oldValue);
			// assign the new value to be the current one
			value = newValue;
			if (notifyForChangeIfNeeded) {
				notifyForChange();
			}
		}
	}

	private void setNotLoaded() {
		valueLoaded = false;
	}

	private void setLoaded() {
		valueLoaded = true;
	}

	protected void notifyForChange() {
		try {
			callbacks.forEach((callback) -> callback.onConfigurationChange(this));
		} catch (Exception e) {
			LOGGER.warn("Failed to execute callback on property change", e);
		}
	}

	protected void notifyForValueDisposal(T oldValue) {
		if (oldValue == null) {
			return;
		}
		try {
			finalizers.forEach(destroyer -> destroyer.accept(oldValue));
		} catch (Exception e) {
			LOGGER.warn("Failed to execute callback on property value disposal", e);
		}
	}

	@Override
	public void addConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		Objects.requireNonNull(callback, "Cannot register null callback");
		((CopyOnWriteArrayList<ConfigurationChangeListener<T>>) callbacks).addIfAbsent(callback);
	}

	// suppress warning for issue about performance of remove operation of CopyOnWriteArrayList
	// this is safe here as it's rarely used
	@SuppressWarnings("squid:S2250")
	@Override
	public void removeConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		Objects.requireNonNull(callback, "Cannot unregister null callback");
		callbacks.remove(callback);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		return builder.append("Configuration[").append(getName()).append("=").append(get()).append("]").toString();
	}

	@Override
	public void addValueDestroyListener(Consumer<T> destroyListener) {
		((CopyOnWriteArrayList<Consumer<T>>) finalizers).addIfAbsent(destroyListener);
	}

	@Override
	public boolean isInitialized() {
		return valueLoaded && value != null;
	}

	@Override
	public void destroy() {
		notifyForValueDisposal(value);
		Destroyable.destroy(value);
		valueLoaded = false;
		value = null;
	}

}
