package com.sirma.itt.emf.extension;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.instance.properties.PersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * List of properties that need to be persisted without definition in an {@link Instance} property map
 *
 * @author Valeri Tishev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 12)
public class PersistentProperties implements PersistentPropertiesExtension {

	/**
	 * Enumeration of all persistent property keys
	 *
	 * @author Valeri Tishev
	 */
	public enum PersistentPropertyKeys {

		URI("uri");

		private final String key;

		/**
		 * Instantiates a new persistent property keys.
		 *
		 * @param key
		 *            the key
		 */
		private PersistentPropertyKeys(String key) {
			this.key = key;
		}

		/**
		 * Gets the key.
		 *
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * Gets the all persistent property keys.
		 *
		 * @return the all persistent property keys
		 */
		public static Set<String> getAllPersistentPropertyKeys() {
			Set<String> keys = new HashSet<>();
			for (PersistentPropertyKeys value : PersistentPropertyKeys.values()) {
				keys.add(value.getKey());
			}
			return keys;
		}
	}

	private static final Set<String> ALLOWED_NO_DEFINITION_FIELDS = PersistentPropertyKeys
			.getAllPersistentPropertyKeys();

	@Override
	public Set<String> getPersistentProperties() {
		return ALLOWED_NO_DEFINITION_FIELDS;
	}

}
