package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Date util used as a helper for date property evaluators.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 03/10/2017
 */
class UserDateConverter {
	@Inject
	private DateConverter dateConverter;
	@Inject
	private UserPreferences userPreferences;
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Gets the date as a String in the system's short time format with the user's timezone offset.
	 *
	 * @param value a date value.
	 * @return the formatted Date.
	 */
	String evaluateDateWithZoneOffset(Serializable value) {
		if (value == null) {
			throw new IllegalArgumentException("The value cannot be null");
		}
		DateFormat systemShortFormat = dateConverter.getSystemShortFormat();
		TimeZone userTimezone = userPreferences.getUserTimezone();
		systemShortFormat.setTimeZone(userTimezone);
		Date convertedDate = typeConverter.convert(Date.class, value);
		return systemShortFormat.format(convertedDate);
	}
}
