package com.sirma.itt.seip.domain.util;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Converter provider for application date formats. The provided date formatters are based on the current user language
 * preference.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DateConverterImpl implements DateConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Date format pattern that is used in DateConverter. Formats the date as 21.11.2012 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.converter.format", defaultValue = "dd.MM.yyyy", label = "Date format pattern that is used in DateConverter. Formats the date as 21.11.2012")
	private ConfigurationProperty<String> converterDateFormatPattern;

	/**
	 * Date time format pattern that is used in DateConverter. Formats the date as 21.11.2012, 12:30
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "datetime.converter.format", defaultValue = "dd.MM.yyyy, HH:mm", label = "Date time format pattern that is used in DateConverter. Formats the date as 21.11.2012, 12:30")
	private ConfigurationProperty<String> converterDatetimeFormatPattern;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.format.full", defaultValue = "dd MM yyyy HH:mm:ss", label = "The full date format. The format used for short date formatting like year, month and day of the month and hour to seconds.")
	private ConfigurationProperty<String> fullSystemDateTimeFormat;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.format.short", defaultValue = "dd MM yyyy", label = "The short date format. The format used for short date formatting like year, month and day of the month")
	private ConfigurationProperty<String> shortSystemDateTimeFormat;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.format.default", defaultValue = "dd MM yyyy HH:mm", label = "The default date format to be used in the application. The format is used in expressions")
	private ConfigurationProperty<String> defaultSystemDateTimeFormat;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private SystemConfiguration systemConfigs;

	@Inject
	private ContextualMap<DateFormatType, Map<String, DateFormat>> dateFormats;
	
	@Inject
	private TypeConverter converter;

	@PostConstruct
	void initialize() {
		// if pattern changes clear mappings for the tenant
		converterDateFormatPattern.addConfigurationChangeListener(c -> dateFormats.clear());
		converterDatetimeFormatPattern.addConfigurationChangeListener(c -> dateFormats.clear());
	}

	/**
	 * Gets the date format based on the current user language preference.
	 *
	 * @return the date format
	 */
	@Override
	public DateFormat getDateFormat() {
		return getDateFormat(DateFormatType.DATE, converterDateFormatPattern.get(), userPreferences::getLanguage);
	}

	/**
	 * Gets the date time format based on the current user language preference.
	 *
	 * @return the date time format
	 */
	@Override
	public DateFormat getDateTimeFormat() {
		return getDateFormat(DateFormatType.DATETIME, converterDatetimeFormatPattern.get(),
				userPreferences::getLanguage);
	}

	/**
	 * Gets the system full format. The format used for short date formatting like year, month and day of the month and
	 * hour to seconds based on system language configuration.
	 *
	 * @return the system full format
	 */
	@Override
	public DateFormat getSystemFullFormat() {
		return getDateFormat(DateFormatType.SYSTEM_FULL, fullSystemDateTimeFormat.get(),
				systemConfigs::getSystemLanguage);
	}

	/**
	 * Gets the system short format. The format used for short date formatting like year, month and day of the month
	 * based on system language configuration.
	 *
	 * @return the system full format
	 */
	@Override
	public DateFormat getSystemShortFormat() {
		return getDateFormat(DateFormatType.SYSTEM_SHORT, shortSystemDateTimeFormat.get(),
				systemConfigs::getSystemLanguage);
	}

	/**
	 * Gets the system default format. The default date format to be used in the application. The format is used in
	 * expressions based on system language configuration.
	 *
	 * @return the system full format
	 */
	@Override
	public DateFormat getSystemDefaultFormat() {
		return getDateFormat(DateFormatType.SYSTEM_DEFAULT, defaultSystemDateTimeFormat.get(),
				systemConfigs::getSystemLanguage);
	}

	/**
	 * Parses the date using {@link #getDateFormat()}
	 *
	 * @param toParse
	 *            the to parse
	 * @return the date
	 */
	@Override
	public Date parseDate(String toParse) {
		return parseInternal(getDateFormat(), toParse, converterDateFormatPattern.get());
	}

	/**
	 * Parses the date time using the {@link #getDateTimeFormat()}
	 *
	 * @param toParse
	 *            the to parse
	 * @return the date
	 */
	@Override
	public Date parseDateTime(String toParse) {
		return parseInternal(getDateTimeFormat(), toParse, converterDatetimeFormatPattern.get());
	}

	/**
	 * Format the given date using the date time formatter
	 *
	 * @param toFormat
	 *            the to format
	 * @return the string
	 */
	@Override
	public String formatDateTime(Date toFormat) {
		return getDateTimeFormat().format(toFormat);
	}

	/**
	 * Format the given date using the date formatter
	 *
	 * @param toFormat
	 *            the to format
	 * @return the string
	 */
	@Override
	public String formatDate(Date toFormat) {
		return getDateFormat().format(toFormat);
	}

	/**
	 * Gets the converter pattern for date format .
	 *
	 * @return the converter date format pattern
	 */
	@Override
	public ConfigurationProperty<String> getConverterDateFormatPattern() {
		return converterDateFormatPattern;
	}

	/**
	 * Gets the converter pattern for datetime format.
	 *
	 * @return the converter datetime format pattern
	 */
	@Override
	public ConfigurationProperty<String> getConverterDatetimeFormatPattern() {
		return converterDatetimeFormatPattern;
	}

	private Date parseInternal(DateFormat dateFormat, String toParse, String pattern) {
		if (toParse == null) {
			return null;
		}
		try {
			return dateFormat.parse(toParse);
		} catch (ParseException e) {
			LOGGER.warn("Failed to parse {} to match format {} - will try ISO 8601", toParse, pattern);
		}
		return converter.convert(Date.class, toParse);
	}

	/**
	 * Gets the date format for locale returned by the given supplier. The methods returns a copy of the date formatter
	 * because the implementation of {@link SimpleDateFormat} is not thread safe!
	 *
	 * @param type
	 *            the type
	 * @param pattern
	 *            the pattern
	 * @param languageSupplier
	 *            the language supplier
	 * @return the date format
	 */
	private DateFormat getDateFormat(DateFormatType type, String pattern, Supplier<String> languageSupplier) {
		return (DateFormat) getLanguageMapping(type)
				.computeIfAbsent(languageSupplier.get(), lang -> new SimpleDateFormat(pattern, getLocaleForLang(lang)))
					.clone();
		// return a copy of the build date format because the simple date format is not thread safe!
	}

	/**
	 * Gets locale by language
	 * 
	 * @param lang
	 *            the language represent as abbreviation
	 * @return locale
	 */
	public static Locale getLocaleForLang(String lang) {
		return new Locale.Builder().setLanguage(lang).build();
	}

	private Map<String, DateFormat> getLanguageMapping(DateFormatType type) {
		return dateFormats.computeIfAbsent(type, t -> new HashMap<>());
	}

	/**
	 * Date formatting types used as keys to identifies formats returned by different methods
	 *
	 * @author BBonev
	 */
	private enum DateFormatType {
		DATE, DATETIME, SYSTEM_FULL, SYSTEM_SHORT, SYSTEM_DEFAULT;
	}
}
