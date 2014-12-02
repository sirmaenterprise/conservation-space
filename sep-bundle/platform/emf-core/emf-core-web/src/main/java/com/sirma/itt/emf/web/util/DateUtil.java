package com.sirma.itt.emf.web.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.date.FormattedDate;
import com.sirma.itt.emf.date.FormattedDateTime;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * Utility functions.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class DateUtil implements TypeConverterProvider {

	private static final int HOURS_IN_A_DAY = 8;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The first week day. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DATEPICKER_FIRST_WEEK_DAY, defaultValue = "1")
	private String firstWeekDay;

	/** Date format pattern that is used in jQuery datepicker. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DATE_FORMAT, defaultValue = "dd.mm.yy")
	private String dateFormatPattern;

	/** Date format pattern that is used in extJS datepicker. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DATE_EXTJS_FORMAT, defaultValue = "d.m.Y")
	private String dateExtJSFormatPattern;

	/** The converter date format pattern. */
	@Inject
	@Config(name = EmfConfigurationProperties.CONVERTER_DATE_FORMAT, defaultValue = "dd.MM.yyyy")
	private String converterDateFormatPattern;

	/** The converter datetime format pattern. */
	@Inject
	@Config(name = EmfConfigurationProperties.CONVERTER_DATETIME_FORMAT, defaultValue = "dd.MM.yyyy, HH:mm")
	private String converterDatetimeFormatPattern;

	/**
	 * Configure year range for date/time picker. First value specify the number of year
	 * before the current and the second, after the current.
	 */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DATEPICKER_YEAR_RANGE, defaultValue = "-120:+3")
	private String yearRange;

	/** The date time format. */
	private DateFormat dateTimeFormat;

	/** The date format. */
	private DateFormat dateFormat;

	/**
	 * Initializes this utility class.
	 */
	@PostConstruct
	public void init() {
		dateTimeFormat = new SimpleDateFormat(converterDatetimeFormatPattern);
		dateFormat = new SimpleDateFormat(converterDateFormatPattern);
	}

	/**
	 * Provides an array as string that holds the month names to be used from
	 * jquery detepicker plugin.
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

		return dateTimeFormat.format(date);
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

		return dateFormat.format(date);
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
		return firstWeekDay;
	}

	/**
	 * Getter method for dateFormatPattern.
	 * 
	 * @return the dateFormatPattern
	 */
	public String getDateFormatPattern() {
		return dateFormatPattern;
	}

	/**
	 * Getter method for dateExtJSFormatPattern.
	 * 
	 * @return the dateExtJSFormatPattern
	 */
	public String getDateExtJSFormatPattern() {
		return dateExtJSFormatPattern;
	}

	/**
	 * Getter for date-time format pattern.
	 * 
	 * @return date-time format pattern
	 */
	public String getConverterDatetimeFormatPattern() {
		return converterDatetimeFormatPattern;
	}

	/**
	 * Setter for date-time format pattern.
	 * 
	 * @param converterDatetimeFormatPattern
	 *            date-time format pattern
	 */
	public void setConverterDatetimeFormatPattern(String converterDatetimeFormatPattern) {
		this.converterDatetimeFormatPattern = converterDatetimeFormatPattern;
	}

	/**
	 * Getter for converted date format patter.
	 * 
	 * @return converted date format pattern
	 */
	public String getConverterDateFormatPattern() {
		return converterDateFormatPattern;
	}

	/**
	 * Setter for converted date format pattern.
	 * 
	 * @param converterDateFormatPattern
	 *            converted date format pattern
	 */
	public void setConverterDateFormatPattern(String converterDateFormatPattern) {
		this.converterDateFormatPattern = converterDateFormatPattern;
	}

	/**
	 * Getter for year range, used from date/time pickers.
	 * 
	 * @return year range format
	 */
	public String getYearRange() {
		return yearRange;
	}

	/**
	 * Setter for year range, used from date/time pickers.
	 * 
	 * @param yearRange
	 *            year range format
	 */
	public void setYearRange(String yearRange) {
		this.yearRange = yearRange;
	}

	/**
	 * Convert minutes to time string in d h m format (example: 2d 3h 35m) assuming a day has 8
	 * hours.
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

			result = (days > 0 ? " " + days + "d" : "")
					+ (remainingHours > 0 ? " " + remainingHours + "h" : "")
					+ (remainingMinutes > 0 ? " " + remainingMinutes + "m" : "");

		}
		return result.trim();
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Date.class, FormattedDate.class,
				new Converter<Date, FormattedDate>() {

					@Override
					public FormattedDate convert(Date source) {
						return new FormattedDate(getFormattedDate(source));
					}
				});
		converter.addConverter(Date.class, FormattedDateTime.class,
				new Converter<Date, FormattedDateTime>() {

					@Override
					public FormattedDateTime convert(Date source) {
						return new FormattedDateTime(getFormattedDateTime(source));
					}
				});
	}
}