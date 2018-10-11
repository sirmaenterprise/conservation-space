package com.sirma.itt.seip.instance.version;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Defines modes for version processing. Used to control, how instances version should be incremented.
 *
 * @author A. Kunchev
 */
public enum VersionMode {

	/**
	 * Used to show that the version should stay unchanged.
	 */
	NONE,

	/**
	 * Used to show that only the minor part of the version should be incremented.
	 */
	MINOR,

	/**
	 * Used to show that the major part of the version should be incremented.
	 */
	MAJOR,

	/**
	 * Used to show that the version number should not be changed and the latest version data should be updated.
	 */
	UPDATE;

	/**
	 * Retrieves mode from string key.
	 *
	 * @param mode the key of the mode that should be returned
	 * @return matched {@link VersionMode}
	 * @throws NoSuchElementException when the input mode does not match any of the defined modes
	 */
	public static VersionMode getMode(String mode) {
		return findMatch(mode)
				.orElseThrow(() -> new NoSuchElementException("There is no match with the available modes."));
	}

	private static Optional<VersionMode> findMatch(String mode) {
		return Arrays.stream(values()).filter(defined -> defined.toString().equalsIgnoreCase(mode)).findFirst();
	}

	/**
	 * Retrieves mode from string key.
	 *
	 * @param mode the key of the mode that should be returned
	 * @param defaultValue will be returned if the input mode does not match any of the defined modes
	 * @return matched {@link VersionMode} or the default value
	 */
	public static VersionMode getMode(String mode, VersionMode defaultValue) {
		return findMatch(mode).orElse(defaultValue);
	}
}