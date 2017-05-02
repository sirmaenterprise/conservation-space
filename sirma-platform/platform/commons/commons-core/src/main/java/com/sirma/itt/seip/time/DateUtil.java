package com.sirma.itt.seip.time;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Utility class for generic functionalities involving time API integration between {@code java.util.Date} and
 * {@code java.time.*}.
 *
 * @author BBonev
 */
public class DateUtil {

	private DateUtil() {
		// utility class
	}

	/**
	 * Converts the given local server time to the given {@link TimeZone}. The method makes sure the time is adjusted to
	 * match the given time zone. <br>
	 * For example, if the argument represents {@code 2007-12-03T10:30+02:00} and the given time zone offset is
	 * {@code +03:00}, then this method will return {@code 2007-12-03T11:30+03:00}.
	 *
	 * @param localDateTime
	 *            the local time that need to be converted to the given time zone
	 * @param clientTimeZone
	 *            the target client zone
	 * @return ISO8601 formatted date with included time zone offset
	 */
	public static OffsetDateTime toOffsetDateTime(Date localDateTime, TimeZone clientTimeZone) {
		Objects.requireNonNull(localDateTime, "Local time is required");
		Objects.requireNonNull(clientTimeZone, "Time zone is required");

		return ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(localDateTime.getTime()), ZoneId.systemDefault())
					.withZoneSameInstant(clientTimeZone.toZoneId())
					.toOffsetDateTime();
	}

	/**
	 * Convert the given {@link OffsetDateTime} instance to {@link Calendar} instance with the same time and time zone
	 *
	 * @param offsetDateTime
	 *            the date time to convert
	 * @return the converted calendar instance
	 */
	public static Calendar toCalendar(OffsetDateTime offsetDateTime) {
		Objects.requireNonNull(offsetDateTime, "Offset date time is required");

		TimeZone timeZone = TimeZone.getTimeZone(offsetDateTime.toZonedDateTime().getZone());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(offsetDateTime.toInstant().toEpochMilli());
		calendar.setTimeZone(timeZone);
		return calendar;
	}

	/**
	 * Convert the given {@link OffsetDateTime} instance to {@link Calendar} instance with the same time and time zone
	 *
	 * @param offsetDateTime
	 *            the date time to convert
	 * @return the converted calendar instance
	 */
	public static Date toDate(OffsetDateTime offsetDateTime) {
		return toCalendar(offsetDateTime).getTime();
	}

	/**
	 * Convert the given {@link ZonedDateTime} instance to {@link Calendar} instance with the same time and time zone
	 *
	 * @param zonedDateTime
	 *            the date time to convert
	 * @return the converted calendar instance
	 */
	public static Calendar toCalendar(ZonedDateTime zonedDateTime) {
		Objects.requireNonNull(zonedDateTime, "Zoned date time is required");

		TimeZone timeZone = TimeZone.getTimeZone(zonedDateTime.getZone());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(zonedDateTime.toInstant().toEpochMilli());
		calendar.setTimeZone(timeZone);
		return calendar;
	}

	/**
	 * Convert the given {@link ZonedDateTime} instance to {@link Calendar} instance with the same time and time zone
	 *
	 * @param zonedDateTime
	 *            the date time to convert
	 * @return the converted calendar instance
	 */
	public static Date toDate(ZonedDateTime zonedDateTime) {
		return toCalendar(zonedDateTime).getTime();
	}
}
