package com.sirma.itt.seip.configuration;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.sync.NoOpSynchronizer;
import com.sirma.itt.seip.configuration.sync.Synchronizer;

/**
 * Represent an injectable wrapper object of configuration property or group identified by a name. The injected
 * configuration could be tenant aware if such implementation is present. All configurations injected via this interface
 * may support dynamic configurations and they can change during runtime. User could register to configuration change
 * via provided method {@link #addConfigurationChangeListener(ConfigurationChangeListener)}.
 *
 * @param <T>
 *            the injected configuration type
 * @author BBonev
 */
public interface ConfigurationProperty<T> extends Named, Serializable {

	/**
	 * Returns the configuration value. The returned value will be the same as the
	 * {@link ConfigurationPropertyDefinition#type()} or {@link ConfigurationGroupDefinition#type()} . <br>
	 * the returned value will be <code>null</code> if the method {@link #isSet()} returns <code>false</code> . This
	 * could happen if the configuration does not have valid value and there is no default value.
	 *
	 * @return the configuration valid or <code>null</code> if the method {@link #isSet()} returns <code>false</code>.
	 */
	T get();

	/**
	 * Gets the configuration value of fail if the configuration is not present.
	 *
	 * @return non <code>null</code> configuration value.
	 * @see #get()
	 * @see #requireConfigured()
	 */
	default T getOrFail() {
		return requireConfigured().get();
	}

	/**
	 * Checks if is the configuration is set or not. The configuration is set when there is a valid configured value or
	 * valid default value. A configuration is considered valid when the configuration converter called returns non
	 * <code>null</code> value or does not throw an exception when called.
	 * <p>
	 * Note that this method will trigger value loading if not loaded.
	 *
	 * @return <code>true</code> if the configuration has a valid non <code>null</code> value and <code>false</code>
	 *         otherwise.
	 */
	default boolean isSet() {
		return get() != null;
	}

	/**
	 * Checks if the current configuration is already loaded and initialized. This method should return
	 * <code>true</code> if the value has been retrieved and converter to configured type. The method should not trigger
	 * value loading if not already loaded.
	 *
	 * @return true, if is initialized and value is loaded
	 */
	boolean isInitialized();

	/**
	 * Performs a check if the configuration is present and if not throws a {@link ConfigurationException} with a
	 * message: "Configuration [config_name] is required!.
	 *
	 * @return the current configuration property
	 */
	default ConfigurationProperty<T> requireConfigured() {
		if (isNotSet()) {
			throw new ConfigurationException("Configuration " + getName() + " is required!");
		}
		return this;
	}

	/**
	 * Performs a check if the configuration is present and if not throws a {@link ConfigurationException} with the
	 * specified message.
	 *
	 * @param message
	 *            a custom message to include in the thrown exception if the value is not set.
	 * @return the current configuration property
	 */
	default ConfigurationProperty<T> requireConfigured(String message) {
		if (isNotSet()) {
			throw new ConfigurationException(message);
		}
		return this;
	}

	/**
	 * Checks if the given configuration property is not set
	 *
	 * @return <code>true</code> if the value returned from the {@link #get()} method is <code>null</code>.
	 */
	default boolean isNotSet() {
		return get() == null;
	}

	/**
	 * Gets the synchronizer object used for locking and synchronizing the configuration access. This should not be used
	 * by the caller directly. This is intended for API implementation. It's not required the implementation to actually
	 * lock/unlock if not needed.
	 *
	 * @return the non <code>null</code> synchronizer object
	 */
	default Synchronizer getSynchronizer() {
		return NoOpSynchronizer.INSTANCE;
	}

	/**
	 * Gets the configuration definition that defined the current configuration instance.
	 *
	 * @return the configuration definition
	 */
	ConfigurationInstance getDefinition();

	/**
	 * Notifies the configuration property that his values was changed. If configuration is not system and there is a
	 * tenant module present then this method will be called in specific tenant environment and will be valid only for
	 * that tenant. <br>
	 * While this method is called the implementation should notify any callback/observers that are registered via the
	 * method {@link #addConfigurationChangeListener(ConfigurationChangeListener)}
	 */
	void valueUpdated();

	/**
	 * Adds the change callback to be executed when configuration value changes. The provided instances will be invoked
	 * after the new configuration is active. A configuration change is considered: no value -&gt; some value, a value
	 * -&gt; other value, a value -&gt; no value. <br>
	 * If the callback is registered for a non system property, when invoked it will be in the environment of the
	 * particular tenant if tenants are enabled.
	 *
	 * @param callback
	 *            the callback to add
	 */
	default void addConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes the change callback added via the {@link #addConfigurationChangeListener(ConfigurationChangeListener)}
	 * method.
	 *
	 * @param callback
	 *            the callback to remove
	 */
	default void removeConfigurationChangeListener(ConfigurationChangeListener<T> callback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds value destroy listener. Listeners registered via this method will be invoked with the old value of the
	 * configuration property when configuration has a change and the old value will no longer be in use. The consumer
	 * will be invoked with a non <code>null</code> value to clear any resources or call any predestroy method
	 * associated with the value. For example to call close method to a resource or connection object.
	 *
	 * @param oldValueConsumer
	 *            the old value consumer, never <code>null</code>.
	 */
	default void addValueDestroyListener(Consumer<T> oldValueConsumer) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Compute a value if configuration is not set otherwise returns the configuration value.
	 *
	 * @param supplier
	 *            the supplier to provide the result if not set
	 * @return the configuration value or supplied one
	 */
	default T computeIfNotSet(Supplier<T> supplier) {
		return isSet() ? get() : supplier.get();
	}
}
