package com.sirma.itt.emf.web.util;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;

/**
 * Utility functions.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class DateUtil implements TypeConverterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);

	private static final int HOURS_IN_A_DAY = 8;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "datepicker.first.week.day", defaultValue = "1", subSystem = "ui", label = "Whether the datepicker control should start the week from the Sunday=0 or Monday=1.")
	private ConfigurationProperty<String> firstWeekDay;

	/** Date format pattern that is used in jQuery datepicker. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.format", defaultValue = "dd.mm.yy", subSystem = "ui", label = "Date format pattern that is used in jQuery datepicker. Formats the date as 21.11.2012")
	private ConfigurationProperty<String> dateFormatPattern;

	/**
	 * This config is the last one used and valid for UI2. After migration is complete then remove the other
	 * configurations for dates like date.format, datepicker.first.week.day and others...
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.date.format", defaultValue = "DD.MM.YY", subSystem = "ui", label = "Date format pattern that is used in ui.")
	private ConfigurationProperty<String> dateFormat;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.time.format", defaultValue = "HH.mm", subSystem = "ui", label = "Time format pattern that is used in ui.")
	private ConfigurationProperty<String> timeFormat;

	/** Date format pattern that is used in extJS datepicker. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "date.extjs.format", defaultValue = "d.m.Y", subSystem = "ui", label = "Date format pattern that is used in extJS datepicker. Formats the date as 21.11.2012")
	private ConfigurationProperty<String> dateExtJSFormatPattern;

	@Inject
	private DateConverter dateConverter;

	/**
	 * Configure year range for date/time picker. First value specify the number of year before the current and the
	 * second, after the current.
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "datepicker.years.range", defaultValue = "-120:+3", subSystem = "ui", label = "Register year range based on current.")
	private ConfigurationProperty<String> yearRange;

	/**
	 * Provides an array as string that holds the month names to be used from jquery detepicker plugin.
	 *
	 * @return the monthNames
	 */
	public String getMonthNames() {
		return labelProvider.getValue(EmfLabelConstants.MONTH_NAMES);
	}

	/**
	 * Gets the day names.
	 *
	 * @return the day names
	 */
	public String getDayNames() {
		return labelProvider.getValue(EmfLabelConstants.DAY_NAMES);
	}

	/**
	 * Getter for dayNamesMin.
	 *
	 * @return the dayNamesMin
	 */
	public String getDayNamesMin() {
		return labelProvider.getValue(EmfLabelConstants.DAY_NAMES_MIN);
	}

	/**
	 * Return date and time based on a date template.
	 *
	 * @param date
	 *            Date object
	 * @return String representation
	 */
	public String getFormattedDateTime(Date date) {
		if (date == null) {
			return null;
		}

		return dateConverter.formatDateTime(date);
	}

	/**
	 * Parse a string into a {@link Date} object. (datetime format)
	 *
	 * @param source
	 *            String to parse.
	 * @return Resulting date date object or null if an error occurs.
	 */
	public Date parse(String source) {
		try {
			return dateConverter.parseDateTime(source);
		} catch (EmfRuntimeException e) {
			LOGGER.error("Unable to parse date string {}", source, e);
		}
		return null;
	}

	/**
	 * Parse a string into a {@link Date} object. (date format)
	 *
	 * @param source
	 *            String to parse.
	 * @return Resulting date date object or null if an error occurs.
	 */
	public Date parseDate(String source) {
		try {
			return dateConverter.parseDate(source);
		} catch (EmfRuntimeException | TypeConversionException e) {
			LOGGER.error("Unable to parse date string {}", source, e);
		}
		return null;
	}

	/**
	 * Return date based on a date template.
	 *
	 * @param date
	 *            Date object
	 * @return String representation
	 */
	public String getFormattedDate(Date date) {
		if (date == null) {
			return null;
		}

		return dateConverter.formatDate(date);
	}

	/**
	 * Return date and time based on a date template.
	 *
	 * @param date
	 *            Date object
	 * @return String representation
	 */
	public String getISOFormattedDateTime(Date date) {
		if (date == null) {
			return null;
		}
		return typeConverter.convert(String.class, date);
	}

	/**
	 * Return date and time object based on a date template.
	 *
	 * @param formattedDate
	 *            String object
	 * @return String representation
	 */
	public Date getISODateTime(String formattedDate) {
		if (formattedDate == null) {
			return null;
		}
		return typeConverter.convert(Date.class, formattedDate);
	}

	/**
	 * Getter method for monthNamesShort.
	 *
	 * @return the monthNamesShort
	 */
	public String getMonthNamesShort() {
		return labelProvider.getValue(EmfLabelConstants.MONTH_NAMES_SHORT);
	}

	/**
	 * Getter method for timeText.
	 *
	 * @return the timeText
	 */
	public String getTimeText() {
		return labelProvider.getValue(EmfLabelConstants.CMF_DATETIMEPICKER_TIMETEXT);
	}

	/**
	 * Getter method for hourText.
	 *
	 * @return the hourText
	 */
	public String getHourText() {
		return labelProvider.getValue(EmfLabelConstants.CMF_DATETIMEPICKER_HOURTEXT);
	}

	/**
	 * Getter method for minuteText.
	 *
	 * @return the minuteText
	 */
	public String getMinuteText() {
		return labelProvider.getValue(EmfLabelConstants.CMF_DATETIMEPICKER_MINUTETEXT);
	}

	/**
	 * Getter method for currentText.
	 *
	 * @return the currentText
	 */
	public String getCurrentText() {
		return labelProvider.getValue(EmfLabelConstants.CMF_DATETIMEPICKER_CURRENTTEXT);
	}

	/**
	 * Getter method for closeText.
	 *
	 * @return the closeText
	 */
	public String getCloseText() {
		return labelProvider.getValue(EmfLabelConstants.CMF_DATETIMEPICKER_CLOSETEXT);
	}

	/**
	 * Getter method for firstWeekDay.
	 *
	 * @return the firstWeekDay
	 */
	public String getFirstWeekDay() {
		return firstWeekDay.get();
	}

	/**
	 * Getter method for dateFormatPattern.
	 *
	 * @return the dateFormatPattern
	 */
	public String getDateFormatPattern() {
		return dateFormatPattern.get();
	}

	/**
	 * Getter method for dateExtJSFormatPattern.
	 *
	 * @return the dateExtJSFormatPattern
	 */
	public String getDateExtJSFormatPattern() {
		return dateExtJSFormatPattern.get();
	}

	/**
	 * Getter for date-time format pattern.
	 *
	 * @return date-time format pattern
	 */
	public String getConverterDatetimeFormatPattern() {
		return dateConverter.getConverterDatetimeFormatPattern().get();
	}

	/**
	 * Getter for converted date format patter.
	 *
	 * @return converted date format pattern
	 */
	public String getConverterDateFormatPattern() {
		return dateConverter.getConverterDateFormatPattern().get();
	}

	/**
	 * Retrieves year range, used for date/time pickers.
	 *
	 * @return year range format
	 */
	public String getYearRange() {
		return yearRange.get();
	}

	/**
	 * Retrieves the date format pattern used in the system.
	 *
	 * @return string representation of date pattern
	 */
	public ConfigurationProperty<String> getDateFormat() {
		return dateFormat;
	}

	/**
	 * Retrieves the time format pattern used in the system.
	 *
	 * @return string representation of time pattern
	 */
	public ConfigurationProperty<String> getTimeFormat() {
		return timeFormat;
	}

	/**
	 * Convert minutes to time string in d h m format (example: 2d 3h 35m) assuming a day has 8 hours.
	 *
	 * @param minutesNum
	 *            the minutes
	 * @return time string
	 */
	public String convertMinutesToTimeString(Number minutesNum) {
		String result = "";
		if (minutesNum != null) {
			Long minutes = minutesNum.longValue();
			long hours = minutes / 60;
			long remainingMinutes = minutes % 60;

			long days = hours / HOURS_IN_A_DAY;
			long remainingHours = hours % HOURS_IN_A_DAY;

			result = (days > 0 ? " " + days + "d" : "") + (remainingHours > 0 ? " " + remainingHours + "h" : "")
					+ (remainingMinutes > 0 ? " " + remainingMinutes + "m" : "");

		}
		return result.trim();
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Date.class, FormattedDate.class, source -> new FormattedDate(getFormattedDate(source)));
		converter.addConverter(Date.class, FormattedDateTime.class,
				source -> new FormattedDateTime(getFormattedDateTime(source)));
	}

}