package com.sirma.itt.seip.util;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Collection of methods for logging.
 *
 * @author BBonev
 */
public class LoggingUtil {

	/**
	 * Comment for STRING.
	 */
	private static final String DEVIDER = "===============================================================================";
	private static final String INFO_START = "\n\n" + DEVIDER + "\n\n";
	private static final String INFO_END = "\n\n" + DEVIDER + "\n";

	/**
	 * Instantiates a new logging util.
	 */
	private LoggingUtil() {
		// nothing to do
	}

	/**
	 * Shorten the given string if the string is more than the given length.
	 * <p>
	 * Example:<br>
	 * <code>
	 * System.out.println(LoggingUtil.shorten("The quick brown fox jumps over the lazy dog", 10));
	 * </code><br>
	 * Prints:<br>
	 * <code>The quick... 33 more</code>
	 *
	 * @param string
	 *            the string to shorten
	 * @param upTo
	 *            the up to
	 * @return the string
	 */
	public static String shorten(String string, int upTo) {
		if (string == null) {
			return null;
		}
		if (string.length() > upTo) {
			StringBuilder builder = new StringBuilder(upTo + 10);
			return builder
					.append(string.substring(0, upTo))
						.append("... ")
						.append(string.length() - upTo)
						.append(" more")
						.toString();
		}
		return string;
	}

	/**
	 * Shorten the given value if the value is more than the given length.
	 * <p>
	 * Example:<br>
	 * <code>
	 * System.out.println(LoggingUtil.shorten("The quick brown fox jumps over the lazy dog", 10));
	 * </code><br>
	 * Prints:<br>
	 * <code>The quick... 33 more</code>
	 *
	 * @param value the value to shorten
	 * @param nullDefault the value to use if the given argument is null, cannot be null
	 * @param upTo the up to
	 * @return the value
	 */
	public static String shorten(Object value, String nullDefault, int upTo) {
		return shorten(Objects.toString(value, Objects.requireNonNull(nullDefault)), upTo);
	}

	/**
	 * Shorten the given value if the value is more than the given length.
	 * <p>
	 * Example:<br>
	 * <code>
	 * System.out.println(LoggingUtil.shorten("The quick brown fox jumps over the lazy dog", 10));
	 * </code><br>
	 * Prints:<br>
	 * <code>The quick... 33 more</code>
	 *<br> If the value is {@code null} then <b>null</b> String will be returned
	 *
	 * @param value the value to shorten
	 * @param upTo the up to
	 * @return the value
	 */
	public static String shortenToNull(Object value, int upTo) {
		return shorten(value, "null", upTo);
	}

	/**
	 * Returns the given map contents as string with it's values shortened to up to 512. This method tries to simulate
	 * the {@link Map#toString()} method as format.
	 *
	 * @param map the map to print
	 * @param <K> the map key type
	 * @param <V> the map value type
	 * @return the String representation of the given map
	 * @see #toString(Map, int)
	 * @see #shorten(String, int)
	 */
	public static <K, V> String toString(Map<K, V> map) {
		return toString(map, 512);
	}

	/**
	 * Returns the given map contents as string with it's value shortened to up to given argument
	 * @param map the map to print
	 * @param upTo the maximum number of characters to print for the map key or value
	 * @param <K> the map key type
	 * @param <V> the map value type
	 * @return the String representation of the given map
	 * @see #toString(Map, int)
	 * @see #shorten(String, int)
	 */
	public static <K, V> String toString(Map<K, V> map, int upTo) {
		if (map == null) {
			return null;
		}
		return map.entrySet()
				.stream()
				.map(entry -> shorten(entry.getKey(), "null", upTo) + "=" + shorten(entry.getValue(), "null", upTo))
				.collect(Collectors.joining(", ", "{", "}"));
	}

	/**
	 * Builds the info message.
	 *
	 * @param message
	 *            the message
	 * @return the string
	 */
	public static String buildInfoMessage(String message) {
		int mid = message.length() >> 1;
		StringBuilder builder = new StringBuilder(256);
		int halfRowLength = DEVIDER.length() >> 1;
		if (mid < halfRowLength) {
			while (builder.length() < halfRowLength - mid) {
				builder.append(' ');
			}
		}
		builder.append(message);
		builder.insert(0, INFO_START);
		builder.append(INFO_END);
		return builder.toString();
	}
}
