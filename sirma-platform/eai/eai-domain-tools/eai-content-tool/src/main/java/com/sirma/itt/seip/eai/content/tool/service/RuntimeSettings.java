package com.sirma.itt.seip.eai.content.tool.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;

/**
 * Runtime settings wrapper that holds the current settings and stores/retrieves already stored settings from previous
 * sessions.
 * 
 * @author bbanchev
 */
public class RuntimeSettings {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Singleton instance to settings. */
	public static final RuntimeSettings INSTANCE = new RuntimeSettings();
	/** Configuration for cached selected files by the user. */
	public static final String LAST_FILE_SELECTED = "last.file.selection";

	private LinkedHashMap<String, Serializable> localData;

	/**
	 * Instantiates a new runtime settings wrapper
	 */
	private RuntimeSettings() {
		loadModel();
	}

	/**
	 * Put a cached value in the storage.
	 *
	 * @param <T>
	 *            the generic type for the new value
	 * @param key
	 *            the storage key
	 * @param value
	 *            the new value
	 * @return the existing value if any
	 */
	public <T extends Serializable> Serializable put(final String key, final T value) {
		Objects.requireNonNull(key, "Could not set value to non valid key!");
		return localData.put(key, value);
	}

	/**
	 * Gets a cached value from the storage.
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @return the existing value if any. Might be null
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(final String key) {
		return (T) localData.get(key);
	}

	/**
	 * Gets a cached value from the storage and optionally returns a default value
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param defValue
	 *            the default value to use if missing
	 * @return the existing value if any or the default value
	 */
	public <T extends Serializable> T get(final String key, final T defValue) {
		final T loaded = get(key);
		if (loaded == null && defValue != null) {
			put(key, defValue);
			return defValue;
		}
		return loaded;
	}

	/**
	 * Store the current model to the {@link #getStorageLocation()}. Model could be obtained later using
	 * {@link #loadModel()}
	 */
	public void storeModel() {
		try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(getStorageLocation()))) {
			writer.writeObject(localData);
		} catch (final Exception e) {
			LOGGER.error("Failure during storing settings!", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadModel() {
		final File storageModel = getStorageLocation();
		if (storageModel.canRead()) {
			try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(storageModel))) {
				localData = (LinkedHashMap<String, Serializable>) reader.readObject();
			} catch (final Exception e) {
				LOGGER.error("Failure during loading settings!", e);
			}
		}
		if (localData == null) {
			// set a new default local storage
			localData = new LinkedHashMap<>();
		}
	}

	private static File getStorageLocation() {
		File settingsDirectory = LocalFileService.createDirectory(LocalFileService.INSTANCE.getInstanceDirectory(),
				".settings");
		return LocalFileService.createFile(settingsDirectory, "runtime", ".config");
	}

}
