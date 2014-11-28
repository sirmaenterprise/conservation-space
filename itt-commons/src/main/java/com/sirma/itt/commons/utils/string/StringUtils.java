/**
 * Copyright (c) 2009 15.12.2009 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provide basic operations over {@link String}s.
 * 
 * @author Hristo Iliev
 */
public final class StringUtils {

	/**
	 * Constant for new line.
	 */
	public final static String NEW_LINE = System.getProperty("line.separator");

	/** Pattern which match the white space or end of {@link String}. */
	private static final Pattern WHITE_SPACE_OR_END = Pattern.compile("\\s+|$");

	/**
	 * Hide utility default constructor.
	 */
	private StringUtils() {
		// Hide utility default constructor
	}

	/**
	 * Capitalize the first character from provided {@link String}, and
	 * optionally lower other characters from the word. All words are compound
	 * and every other word is also first-letter-capitalized. The second
	 * argument of the function enable lowering of other characters in the word.
	 * <p>
	 * For example: <br>
	 * {@code toUpperCamelCase("today is holiday", false)} will return
	 * {@code TodayIsHoliday}. <br>
	 * {@code toUpperCamelCase("toDaY iS hOlIdAy", false)} will return
	 * {@code ToDaYISHOlIdAy}. <br>
	 * {@code toUpperCamelCase("toDaY iS hOlIdAy", true)} will return
	 * {@code TodayIsHoliday}.
	 * 
	 * @param str
	 *            {@link String}, string which will be capitalized
	 * @param lowerOther
	 *            boolean, will other characters in the word will be lowered or
	 *            not
	 * @return {@link String}, capitalized string
	 */
	public static String toUpperCamelCase(final String str,
			final boolean lowerOther) {
		StringBuilder result = new StringBuilder();
		String toChange = str.trim();
		Matcher matcher = WHITE_SPACE_OR_END.matcher(toChange);
		int from = 0;
		int to;
		while (matcher.find()) {
			to = matcher.start();
			result.append(Character.toUpperCase(toChange.charAt(from)));
			if (from != to + 1) {
				String subPart = toChange.substring(from + 1, to);
				if (lowerOther) {
					result.append(subPart.toLowerCase());
				} else {
					result.append(subPart);
				}
			}
			from = matcher.end();
		}
		to = toChange.length();

		return result.toString();
	}

	/**
	 * Uncapitalize the first character from provided {@link String}, and
	 * optionally lower other characters from the word. All words are compound
	 * and every other word is first-letter-capitalized. The second argument of
	 * the function enable lowering of other characters in the word.
	 * <p>
	 * For example: <br>
	 * {@code toLowerCamelCase("today is holiday", false)} will return
	 * {@code todayIsHoliday}. <br>
	 * {@code toLowerCamelCase("toDaY iS hOlIdAy", false)} will return
	 * {@code toDaYISHOlIdAy}. <br>
	 * {@code toLowerCamelCase("toDaY iS hOlIdAy", true)} will return
	 * {@code todayIsHoliday}.
	 * 
	 * @param str
	 *            {@link String}, string which will be capitalized
	 * @param lowerOther
	 *            boolean, will other characters in the word will be lowered or
	 *            not
	 * @return {@link String}, capitalized string
	 */
	public static String toLowerCamelCase(final String str,
			final boolean lowerOther) {
		// TODO use private methods with StringBuilder
		return lowerFirstChar(toUpperCamelCase(str, lowerOther));
	}

	/**
	 * Upper first character of the provided {@link String}. All other
	 * characters remain unchanged. Note that this method can return the same
	 * instance if the first character is already in upper case.
	 * 
	 * @param str
	 *            {@link String}, string which will be manipulated
	 * @return {@link String}, the result string
	 */
	public static String upperFirstChar(final String str) {
		if ((str.length() == 0) || Character.isUpperCase(str.charAt(0))) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * Lower first character of the provided {@link String}. All other
	 * characters remain unchanged. Note that this method can return the same
	 * instance if the first character is already in lower case.
	 * 
	 * @param str
	 *            {@link String}, string which will be manipulated
	 * @return {@link String}, the result string
	 */
	public static String lowerFirstChar(final String str) {
		if ((str.length() == 0) || Character.isLowerCase(str.charAt(0))) {
			return str;
		}
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * Check is the provided string is null or empty.
	 * 
	 * @param str
	 *            {@link String}, string to check;
	 * @return boolean, is the provided string is null or empty
	 */
	public static boolean isNullOrEmpty(final String str) {
		return (str == null) || (str.length() == 0);
	}

	/**
	 * Check is the provided string is NOT null or empty.
	 * 
	 * @param str
	 *            {@link String}, string to check;
	 * @return boolean, is the provided string is NOT null or empty
	 */
	public static boolean isNotNullOrEmpty(final String str) {
		return !isNullOrEmpty(str);
	}

	/**
	 * Checks if the provided string is not null.
	 * 
	 * @param str
	 *            {@link String}, string to check;
	 * @return true if the provided string is not null, false otherwise.
	 */
	public static boolean isNotNull(final String str) {
		return !(str == null);
	}

	// TODO remove this - make TestNG
	public static void main(final String[] args) {
		System.out.println(toUpperCamelCase("today is holiday", false));
		System.out.println(toUpperCamelCase("toDaY iS hOlIdAy", false));
		System.out.println(toUpperCamelCase("toDaY iS hOlIdAy", true));
		System.out.println(toUpperCamelCase("Hello my name is Jack.", false));
		System.out.println(toUpperCamelCase("howdy", false));
		System.out.println(toUpperCamelCase("HOWDY", false));
		System.out.println(toUpperCamelCase("HO W DY", false));
		System.out.println(toUpperCamelCase("Hello my name is Jack.", true));
		System.out.println(toUpperCamelCase("howdy", true));
		System.out.println(toUpperCamelCase("HOWDY", true));
		System.out.println(toUpperCamelCase("HO W DY", true));
		System.out.println(toUpperCamelCase("h o w d y", true));
	}
}
