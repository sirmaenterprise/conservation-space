package com.sirma.itt.seip.domain.util;

import java.text.DateFormat;
import java.util.Date;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Converter provider for application date formats. The provided date formatters are based on the current user language
 * preference.
 * 
 * @author BBonev
 */
public interface DateConverter {

	/**
	 * Gets the date format based on the current user language preference.
	 *
	 * @return the date format
	 */
	DateFormat getDateFormat();

	/**
	 * Gets the date time format based on the current user language preference.
	 *
	 * @return the date time format
	 */
	DateFormat getDateTimeFormat();

	/**
	 * Gets the system full format. The format used for short date formatting like year, month and day of the month and
	 * hour to seconds based on system language configuration.
	 *
	 * @return the system full format
	 */
	DateFormat getSystemFullFormat();

	/**
	 * Gets the system short format. The format used for short date formatting like year, month and day of the month
	 * based on system language configuration.
	 *
	 * @return the system full format
	 */
	DateFormat getSystemShortFormat();

	/**
	 * Gets the system default format. The default date format to be used in the application. The format is used in
	 * expressions based on system language configuration.
	 *
	 * @return the system full format
	 */
	DateFormat getSystemDefaultFormat();

	/**
	 * Parses the date using {@link #getDateFormat()}
	 *
	 * @param toParse
	 *            the to parse
	 * @return the date
	 */
	Date parseDate(String toParse);

	/**
	 * Parses the date time using the {@link #getDateTimeFormat()}
	 *
	 * @param toParse
	 *            the to parse
	 * @return the date
	 */
	Date parseDateTime(String toParse);

	/**
	 * Format the given date using the date time formatter
	 *
	 * @param toFormat
	 *            the to format
	 * @return the string
	 */
	String formatDateTime(Date toFormat);

	/**
	 * Format the given date using the date formatter
	 *
	 * @param toFormat
	 *            the to format
	 * @return the string
	 */
	String formatDate(Date toFormat);

	/**
	 * Gets the converter pattern for date format .
	 *
	 * @return the converter date format pattern
	 */
	ConfigurationProperty<String> getConverterDateFormatPattern();

	/**
	 * Gets the converter pattern for datetime format.
	 *
	 * @return the converter datetime format pattern
	 */
	ConfigurationProperty<String> getConverterDatetimeFormatPattern();

}