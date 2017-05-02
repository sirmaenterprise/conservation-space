package com.sirma.itt.seip.util;

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
