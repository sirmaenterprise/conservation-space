package com.sirma.itt.emf.configuration;

import java.io.Serializable;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Thread local runtime configuration. The configuration implementation supports stacking of the set
 * configuration instead of replacing them. This means multiple sets of the same configuration will
 * result in multiple entries in the configuration.<br>
 * Example: <code><pre>
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
public final class RuntimeConfiguration {

	/** The thread local configuration. */
	private static final ThreadLocal<Map<String, Deque<Serializable>>> CONFIGURATION = new ThreadLocal<>();

	/**
	 * Instantiates a new runtime configuration.
	 */
	private RuntimeConfiguration() {
		// hides default constructor
	}

	/**
	 * Sets the configuration using the given key and value. If value is <code>null</code> the
	 * property will be removed from the configuration if was set at all.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void setConfiguration(String key, Serializable value) {
		Map<String, Deque<Serializable>> data = getOrCreateConfigurationData();
		Deque<Serializable> deque = data.get(key);
		if (deque == null) {
			deque = new LinkedList<>();
			data.put(key, deque);
		}
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
		Map<String, Deque<Serializable>> data = CONFIGURATION.get();
		Deque<Serializable> deque = null;
		return (data != null) && ((deque = data.get(key)) != null)
				&& !(deque.isEmpty() || (deque.peek() == null));
	}

	/**
	 * Clear configuration property. If that configuration was the last property then the
	 * configuration object is cleared from the local cache.
	 * 
	 * @param key
	 *            the configuration key to remove
	 */
	public static void clearConfiguration(String key) {
		Map<String, Deque<Serializable>> data = CONFIGURATION.get();
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
				CONFIGURATION.remove();
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
		Map<String, Deque<Serializable>> map = CONFIGURATION.get();
		Deque<Serializable> deque = null;
		return map != null ? ((deque = map.get(key)) != null ? deque.peekFirst() : null) : null;
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
	 * Checks if the given key/s are set. This is shorter version of the method
	 * {@link #isConfigurationSet(String)}. The method could check multiple keys for activation with
	 * OR operator.
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
		if (!result && (other != null) && (other.length > 0)) {
			for (String string : other) {
				result |= isConfigurationSet(string);
			}
		}
		return result;
	}

	/**
	 * Resets all the current configurations. Cannot be undone.
	 */
	public static void resetConfiguration() {
		Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData();
		configurationData.clear();
	}

	/**
	 * Returns a read only snapshot of the current configuration, this should be used only for
	 * coping configuration to other thread when using async calls. The method effectively creates a
	 * snapshot of the current configuration for later use. <br>
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
	 * {@link #getCurrentConfiguration()} method and <b>OVERRDIDES</b> the current configuration if
	 * the current {@link Thread}. Any configuration set before that will be lost if not backup
	 * before that. This method is intended for initializing new async threads from a calling thread
	 * if needed to transfer the configuration to other thread.
	 * 
	 * @param config
	 *            the new current configuration
	 */
	public static void setCurrentConfiguration(CurrentRuntimeConfiguration config) {
		Map<String, Deque<Serializable>> currentConfiguration = config.getCurrentConfiguration();
		if (!currentConfiguration.isEmpty()) {
			Map<String, Deque<Serializable>> configurationData = getOrCreateConfigurationData();
			configurationData.putAll(currentConfiguration);
		}
	}

	/**
	 * Gets the or create configuration data.
	 * 
	 * @return the or create configuration data
	 */
	private static Map<String, Deque<Serializable>> getOrCreateConfigurationData() {
		Map<String, Deque<Serializable>> map = CONFIGURATION.get();
		if (map == null) {
			CONFIGURATION.set(new HashMap<String, Deque<Serializable>>());
		}
		return CONFIGURATION.get();
	}

	/**
	 * DAO class for the current runtime configuration to be transferable between threads without
	 * exposing it.
	 * 
	 * @author BBonev
	 */
	public static final class CurrentRuntimeConfiguration {

		/** The current configuration. */
		private final Map<String, Deque<Serializable>> currentConfiguration;

		/**
		 * Instantiates a new current runtime configuration.
		 */
		private CurrentRuntimeConfiguration() {
			// we can clone the map and its content also
			currentConfiguration = Collections.unmodifiableMap(getOrCreateConfigurationData());
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
	}
}
