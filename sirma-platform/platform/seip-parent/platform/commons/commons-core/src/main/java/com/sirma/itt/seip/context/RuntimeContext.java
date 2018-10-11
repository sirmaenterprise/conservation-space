package com.sirma.itt.seip.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Thread local runtime configuration. The configuration implementation supports stacking of the set configuration
 * instead of replacing them. This means multiple sets of the same configuration will result in multiple entries in the
 * configuration.<br>
 * Example:
 * 
 * <pre>
 * <code>
 * 		String key = "someKey";
 * 		String value1 = "data";
 * 		Integer value2 = 22;
 *
 * 		RuntimeConfiguration.setConfiguration(key, value1);
 * 		RuntimeConfiguration.setConfiguration(key, value2);
 * 		...
 * 		Integer fetchedValue2 = (Integer) RuntimeConfiguration.getConfiguration(key);
 * 		RuntimeConfiguration.clearConfiguration(key); // will remove the String value1
 * </code>
 * </pre>
 *
 * @author BBonev
 */
public final class RuntimeContext {

	/** The thread local configuration. */
	private static final ThreadLocal<Map<String, Deque<Serializable>>> CONFIGURATION = new ThreadLocal<>();
	private static final ThreadLocal<Map<String, Deque<Serializable>>> INHERITABLE_CONFIGURATION = new InheritableThreadLocal<>();
	private static final Set<String> TRANSFERABLE_KEYS = new HashSet<>();

	/**
	 * Instantiates a new runtime configuration.
	 */
	private RuntimeContext() {
		// hides default constructor
	}

	/**
	 * Sets the configuration using the given key and value. If value is <code>null</code> the property will be removed
	 * from the configuration if was set at all.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void setConfiguration(String key, Serializable value) {
		setConfigInternal(key, value, CONFIGURATION);
	}

	static void setConfigInternal(String key, Serializable value, ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> data = getOrCreateConfigurationData(store);
		Deque<Serializable> deque = data.computeIfAbsent(key, k -> new LinkedList<>());
		deque.addFirst(value);
	}

	/**
	 * Checks if is configuration with the given key is set.
	 *
	 * @param key
	 *            the configuration key to check for
	 * @return true, if configuration is set
	 */
	public static boolean isConfigurationSet(String key) {
		return isConfigurationSetInternal(key, CONFIGURATION);
	}

	static boolean isConfigurationSetInternal(String key, ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> data = store.get();
		Deque<Serializable> deque = null;
		return data != null && (deque = data.get(key)) != null && !(deque.isEmpty() || deque.peek() == null);
	}

	/**
	 * Clear configuration property. If that configuration was the last property then the configuration object is
	 * cleared from the local cache.
	 *
	 * @param key
	 *            the configuration key to remove
	 */
	public static void clearConfiguration(String key) {
		clearConfigurationInternal(key, CONFIGURATION);
	}

	static void clearConfigurationInternal(String key, ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> data = store.get();
		if (data != null) {
			Deque<Serializable> deque = data.get(key);
			if (deque != null) {
				// clear backup configuration
				deque.pollFirst();
				if (deque.isEmpty()) {
					data.remove(key);
				}
			}
			// clear configuration if needed
			if (data.isEmpty()) {
				store.remove();
			}
		}
	}

	/**
	 * Gets the configuration data for the given key.
	 *
	 * @param key
	 *            the configuration key to look for
	 * @return the configuration data
	 */
	public static Serializable getConfiguration(String key) {
		return getConfigurationInternal(key, CONFIGURATION);
	}

	static Serializable getConfigurationInternal(String key, ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> map = store.get();
		Deque<Serializable> deque;
		if (map != null) {
			return (deque = map.get(key)) != null ? deque.peekFirst() : null;
		}
		return null;
	}

	/**
	 * Enables the given configuration key by inserting <code>true</code> for the given key.
	 *
	 * @param key
	 *            the key to set
	 */
	public static void enable(String key) {
		setConfiguration(key, Boolean.TRUE);
	}

	/**
	 * Disables the given configuration key by removing the value for the given key.
	 *
	 * @param key
	 *            the key to clear
	 */
	public static void disable(String key) {
		clearConfiguration(key);
	}

	/**
	 * Checks if the given key/s are set. This is shorter version of the method {@link #isConfigurationSet(String)}. The
	 * method could check multiple keys for activation with OR operator.
	 *
	 * @param key
	 *            the key to check
	 * @param other
	 *            the other keys to check
	 * @return true, if is set
	 */
	public static boolean isSet(String key, String... other) {
		boolean result = isConfigurationSet(key);
		// if the first key is set there is no need to continue checking even we have more keys to
		// check
		if (!result && other != null && other.length > 0) {
			for (String anOther : other) {
				result |= isConfigurationSet(anOther);
			}
		}
		return result;
	}

	/**
	 * Resets all the current configurations. Cannot be undone.
	 */
	public static void resetConfiguration() {
		Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData(CONFIGURATION);
		configurationData.clear();
		configurationData = getOrCreateConfigurationData(INHERITABLE_CONFIGURATION);
		configurationData.clear();
	}

	/**
	 * Returns a read only snapshot of the current configuration, this should be used only for coping configuration to
	 * other thread when using async calls. The method effectively creates a snapshot of the current configuration for
	 * later use. <br>
	 * <b>NOTE:</b> the objects are not cloned so complex objects will still be changeable.
	 *
	 * @return the current configuration
	 */
	public static CurrentRuntimeConfiguration getCurrentConfiguration() {
		return CurrentRuntimeConfiguration.getSnapshot();
	}

	/**
	 * <b>IMPORTANT:</b> Make sure to understand what and how this method works before using it.
	 * <p>
	 * The method uses the given {@link CurrentRuntimeConfiguration} object that is produced by a
	 * {@link #getCurrentConfiguration()} method and <b>OVERRDIDES</b> the current configuration if the current
	 * {@link Thread}. Any configuration set before that will be lost if not backup before that. This method is intended
	 * for initializing new async threads from a calling thread if needed to transfer the configuration to other thread.
	 *
	 * @param config
	 *            the new current configuration
	 */
	public static void setCurrentConfiguration(CurrentRuntimeConfiguration config) {
		if (config == null) {
			return;
		}
		Map<String, Deque<Serializable>> currentConfiguration = config.getCurrentConfiguration();
		if (!currentConfiguration.isEmpty()) {
			Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData(CONFIGURATION);
			configurationData.putAll(currentConfiguration);
		}
		currentConfiguration = config.getCurrentInheritableConfiguration();
		if (!currentConfiguration.isEmpty()) {
			Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData(
					INHERITABLE_CONFIGURATION);
			configurationData.putAll(currentConfiguration);
		}
	}

	/**
	 * Replace the current configuration context with the specified copy and return the old configurations. If the
	 * passed configuration is <code>null</code> then the method will clear the current context and return context state
	 * before the method call.
	 *
	 * @param runtimeConfiguration
	 *            the current runtime configuration to use
	 * @return the current runtime configuration as a backup. It could be used to restore the current thread state
	 */
	public static CurrentRuntimeConfiguration replaceConfiguration(CurrentRuntimeConfiguration runtimeConfiguration) {
		CurrentRuntimeConfiguration oldConfiguration = getCurrentConfiguration();
		// clear all previous configuration from other configurations copy
		// clear only transferable keys and keep the non transferable
		Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData(CONFIGURATION);
		configurationData.keySet().removeAll(TRANSFERABLE_KEYS);
		configurationData = getOrCreateConfigurationData(INHERITABLE_CONFIGURATION);
		configurationData.keySet().removeAll(TRANSFERABLE_KEYS);
		// set all provided configurations from the calling thread
		if (runtimeConfiguration != null) {
			setCurrentConfiguration(runtimeConfiguration);
		}
		// if the new engine is the same as the old one (if equal this means the method is not
		// called on other thread but on the original so no need to clear the engine instance)
		return oldConfiguration;
	}

	/**
	 * Gets the or create configuration data.
	 */
	private static Map<String, Deque<Serializable>> getOrCreateConfigurationData(
			ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> map = store.get();
		if (map == null) {
			store.set(new HashMap<>(8));
		}
		return store.get();
	}

	private static Map<String, Deque<Serializable>> getTransferableConfigurationData(
			ThreadLocal<Map<String, Deque<Serializable>>> store) {
		Map<String, Deque<Serializable>> map = store.get();
		if (map == null || map.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Deque<Serializable>> copy = new HashMap<>(map);
		copy.keySet().retainAll(TRANSFERABLE_KEYS);
		return copy;
	}

	/**
	 * Creates configuration instance. The configuration data set using this {@link Config} is only for the thread that
	 * set it.
	 *
	 * @param key
	 *            the key
	 * @param allowTransfer
	 *            via {@link #getCurrentConfiguration()} and
	 *            {@link #setCurrentConfiguration(CurrentRuntimeConfiguration)} methods.
	 * @return the config
	 * @see ThreadLocal
	 */
	public static Config createConfig(String key, boolean allowTransfer) {
		if (allowTransfer) {
			TRANSFERABLE_KEYS.add(key);
		}
		return new ConfigImpl(key, CONFIGURATION);
	}

	/**
	 * Creates the inheritable {@link Config}. Any configuration set using this {@link Config} will be inherited in
	 * threads created from the current thread.
	 *
	 * @param key
	 *            the key
	 * @return the config
	 * @see InheritableThreadLocal
	 */
	public static Config createInheritableConfig(String key) {
		TRANSFERABLE_KEYS.add(key);
		return new ConfigImpl(key, INHERITABLE_CONFIGURATION);
	}

	/**
	 * Creates the option. The option activated using this {@link Option} is only for the thread that use it.
	 *
	 * @param key
	 *            the key
	 * @return the option
	 * @see ThreadLocal
	 */
	public static Option createOption(String key) {
		TRANSFERABLE_KEYS.add(key);
		return new OptionImpl(key, CONFIGURATION);
	}

	/**
	 * Creates the inheritable option. Any option activated {@link Option} will be transfered in the newly created
	 * threads from the current thread.
	 *
	 * @param key
	 *            the key
	 * @return the option
	 * @see InheritableThreadLocal
	 */
	public static Option createInheritableOption(String key) {
		TRANSFERABLE_KEYS.add(key);
		return new OptionImpl(key, INHERITABLE_CONFIGURATION);
	}

	/**
	 * Creates the group.
	 *
	 * @param key
	 *            the key
	 * @param options
	 *            the options
	 * @return the option
	 */
	public static Option createGroup(String key, Option... options) {
		return new GroupOption(key, CONFIGURATION, options);
	}

	/**
	 * DAO class for the current runtime configuration to be transferable between threads without exposing it.
	 *
	 * @author BBonev
	 */
	public static final class CurrentRuntimeConfiguration implements Serializable {

		/** The current configuration. */
		private final Map<String, Deque<Serializable>> currentConfiguration;
		private final Map<String, Deque<Serializable>> currentInheritableConfiguration;

		/**
		 * Instantiates a new current runtime configuration.
		 */
		private CurrentRuntimeConfiguration() {
			// we can clone the map and its content also
			currentConfiguration = getTransferableConfigurationData(CONFIGURATION);
			currentInheritableConfiguration = getTransferableConfigurationData(INHERITABLE_CONFIGURATION);
		}

		/**
		 * Gets a snapshot of the current configuration.
		 *
		 * @return the snapshot
		 */
		private static CurrentRuntimeConfiguration getSnapshot() {
			return new CurrentRuntimeConfiguration();
		}

		/**
		 * Getter method for currentConfiguration.
		 *
		 * @return the currentConfiguration
		 */
		private Map<String, Deque<Serializable>> getCurrentConfiguration() {
			return currentConfiguration;
		}

		/**
		 * Gets the current inheritable configuration.
		 *
		 * @return the current inheritable configuration
		 */
		private Map<String, Deque<Serializable>> getCurrentInheritableConfiguration() {
			return currentInheritableConfiguration;
		}
	}

	/**
	 * Configuration option. Allows setting a value in the thread local context.
	 *
	 * @author BBonev
	 */
	private static class ConfigImpl implements Config {

		/** The key of the value that is managed by the current instance. */
		private final String key;
		private final ThreadLocal<Map<String, Deque<Serializable>>> store;

		/**
		 * Instantiates a new config.
		 *
		 * @param key
		 *            the key
		 * @param store
		 *            the store
		 */
		ConfigImpl(String key, ThreadLocal<Map<String, Deque<Serializable>>> store) {
			this.key = key;
			this.store = store;
		}

		/**
		 * Sets the given value to the thread local using the current key.
		 *
		 * @param value
		 *            the value to set
		 */
		@Override
		public void set(Serializable value) {
			setConfigInternal(key, value, store);
		}

		/**
		 * Retrieves a value from thread local context using the current key.
		 *
		 * @return the value or <code>null</code> if not set.
		 */
		@Override
		public Serializable get() {
			return getConfigurationInternal(key, store);
		}

		/**
		 * Clear the set value for the current key.
		 */
		@Override
		public void clear() {
			clearConfigurationInternal(key, store);
		}

		/**
		 * Checks if there is a value associated with the current key.
		 *
		 * @return <code>true</code>, if there is a non <code>null</code> value set.
		 */
		@Override
		public boolean isSet() {
			return isConfigurationSetInternal(key, store);
		}
	}

	/**
	 * Boolean option. An option could be active and inactive but not both at the same time.
	 *
	 * @author BBonev
	 */
	private static class OptionImpl implements Option {

		/** The key to be used. */
		private final String key;
		private final ThreadLocal<Map<String, Deque<Serializable>>> store;

		/**
		 * Instantiates a new option.
		 *
		 * @param key
		 *            the key
		 * @param store
		 *            the store
		 */
		OptionImpl(String key, ThreadLocal<Map<String, Deque<Serializable>>> store) {
			this.key = key;
			this.store = store;
		}

		/**
		 * Enables the option.
		 */
		@Override
		public void enable() {
			setConfigInternal(key, Boolean.TRUE, store);
		}

		/**
		 * Disables the option.
		 */
		@Override
		public void disable() {
			clearConfigurationInternal(key, store);
		}

		/**
		 * Checks if is enabled.
		 *
		 * @return <code>true</code>, if is enabled.
		 */
		@Override
		public boolean isEnabled() {
			return isConfigurationSetInternal(key, store);
		}
	}

	private static class GroupOption extends OptionImpl {

		/** The additional options. */
		private final Option[] options;

		/**
		 * Instantiates a new group option.
		 *
		 * @param key
		 *            the main option key
		 * @param store
		 *            the store
		 * @param options
		 *            the additional options
		 */
		GroupOption(String key, ThreadLocal<Map<String, Deque<Serializable>>> store, Option... options) {
			super(key, store);
			if (options == null) {
				this.options = new Option[0];
			} else {
				this.options = options;
			}
		}

		@Override
		public void enable() {
			super.enable();
			for (Option option : options) {
				option.enable();
			}
		}

		@Override
		public void disable() {
			super.disable();
			for (Option option : options) {
				option.disable();
			}
		}

		@Override
		public boolean isEnabled() {
			// implement me!
			return false;
		}
	}
}
