package com.sirma.itt.seip.instance.version;

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
	MAJOR;

}
