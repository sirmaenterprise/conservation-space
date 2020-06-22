package com.sirma.itt.seip.domain.util;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isBlank;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.Pair;

/**
 * Used for some operations on instance versions. Contains methods for splitting specific version to major and minor
 * part, incrementing or setting version on specific instance.
 *
 * @author A. Kunchev
 */
public class VersionUtil {

	private static final String VERSION_SPLITTER_REGEX = "\\.";

	private static final String VERSION_SPLITTER = ".";

	private VersionUtil() {
		// utility
	}

	/**
	 * Splits the passed version into two, major and minor. For the splitter is used dot ( . ). The splitted version
	 * values are stored in {@link Pair}, where the first member is the major part of the version and the second member
	 * is the minor part.
	 *
	 * @param version
	 *            the version that should be split. Required and the separator between major and minor should be dot
	 * @return {@link Pair} where the first member is the major part and the second member is the minor part of the
	 *         version
	 */
	public static IntegerPair split(String version) {
		if (isBlank(version) || !version.contains(VERSION_SPLITTER)) {
			throw new IllegalArgumentException("The passed argument is blank or it doesn't contain correct separator.");
		}

		String[] splitVersion = version.split(VERSION_SPLITTER_REGEX);
		return new IntegerPair(parseInt(splitVersion[0]), parseInt(splitVersion[1]));
	}

	/**
	 * Combines the passed values and returns them as string. The values are combined with splitter dot ( . ) between
	 * them.
	 *
	 * @param major
	 *            the major number(version)
	 * @param minor
	 *            the minor number(version)
	 * @return combined numbers as string with separator dot between
	 */
	public static String combine(int major, int minor) {
		if (major < 0 || minor < 0) {
			throw new NumberFormatException("Passed version parts should be positive numbers.");
		}

		return major + VERSION_SPLITTER + minor;
	}

}
