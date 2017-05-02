package com.sirma.itt.seip.time;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Test for {@link DateUtil}
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class DateUtilTest {

	@Test
	public void toOffsetDateTimeShouldConvertToTheTargetZone() throws Exception {
		TimeZone defaultZone = TimeZone.getDefault();
		// when in NY time parse EST time to LA time and should get LA time
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			Date localDateTime = ISO8601DateFormat.parse("2017-02-10T16:10:19.777+02:00");
			String remoteTime = DateUtil
					.toOffsetDateTime(localDateTime, TimeZone.getTimeZone("America/Los_Angeles"))
						.toString();
			assertEquals("2017-02-10T06:10:19.777-08:00", remoteTime);
		} finally {
			// reset the zone
			TimeZone.setDefault(defaultZone);
		}
	}

	@Test
	public void offsetToCalendar() throws Exception {
		TimeZone defaultZone = TimeZone.getDefault();
		// when in NY time parse EST time to LA time and should get LA time
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			OffsetDateTime localDateTime = OffsetDateTime.parse("2017-02-10T00:00:00.000+02:00");
			Calendar remoteTime = DateUtil.toCalendar(localDateTime);
			assertEquals("The time conversion should not be affectied by the local time zone",
					"2017-02-10T00:00:00.000+02:00", ISO8601DateFormat.format(remoteTime));
		} finally {
			// reset the zone
			TimeZone.setDefault(defaultZone);
		}
	}

	@Test
	public void offsetToDate() throws Exception {
		TimeZone defaultZone = TimeZone.getDefault();
		// when in NY time parse EST time to LA time and should get LA time
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			OffsetDateTime localDateTime = OffsetDateTime.parse("2017-02-10T00:00:00.000+02:00");
			Date remoteTime = DateUtil.toDate(localDateTime);
			assertEquals("The time conversion should be in local time zone",
					"2017-02-09T17:00:00.000-05:00", ISO8601DateFormat.format(remoteTime));
		} finally {
			// reset the zone
			TimeZone.setDefault(defaultZone);
		}
	}

	@Test
	public void zonedToCalendar() throws Exception {
		TimeZone defaultZone = TimeZone.getDefault();
		// when in NY time parse EST time to LA time and should get LA time
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			ZonedDateTime localDateTime = OffsetDateTime.parse("2017-02-10T00:00:00.000+02:00").toZonedDateTime();
			Calendar remoteTime = DateUtil.toCalendar(localDateTime);
			assertEquals("The time conversion should not be affectied by the local time zone",
					"2017-02-10T00:00:00.000+02:00", ISO8601DateFormat.format(remoteTime));
		} finally {
			// reset the zone
			TimeZone.setDefault(defaultZone);
		}
	}

	@Test
	public void zonedToDate() throws Exception {
		TimeZone defaultZone = TimeZone.getDefault();
		// when in NY time parse EST time to LA time and should get LA time
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			ZonedDateTime localDateTime = OffsetDateTime.parse("2017-02-10T00:00:00.000+02:00").toZonedDateTime();
			Date remoteTime = DateUtil.toDate(localDateTime);
			assertEquals("The time conversion should be in local time zone", "2017-02-09T17:00:00.000-05:00",
					ISO8601DateFormat.format(remoteTime));
		} finally {
			// reset the zone
			TimeZone.setDefault(defaultZone);
		}
	}
}
