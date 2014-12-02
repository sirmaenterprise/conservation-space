package com.sirma.itt.emf.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link ISO8601DateFormat}.
 * 
 * @author Adrian Mitev
 */
@Test
public class ISO8601DateFormatTest {

	/**
	 * Tests {@link ISO8601DateFormat#parse(String)} and
	 * {@link ISO8601DateFormat#format(java.util.Date)},
	 * 
	 * @throws ParseException
	 */
	public void testFormatAndParse() throws ParseException {
		final String DATE = "2013-05-27";

		Date date_27_May_2013 = new SimpleDateFormat("yyyy-MM-dd").parse(DATE);
		int offset = TimeZone.getDefault().getOffset(date_27_May_2013.getTime()) / (60 * 60 * 1000);
		StringBuilder offsetBuilder = new StringBuilder();
		offsetBuilder.append(offset);

		if (offset >= 0) {
			offsetBuilder.insert(0, "+");
		}

		if (Math.abs(offset) < 10) {
			offsetBuilder.insert(1, "0");
		}

		offsetBuilder.append(":00");

		String isoDate = DATE + "T00:00:00.000" + offsetBuilder.toString();
		Assert.assertEquals(isoDate, ISO8601DateFormat.format(ISO8601DateFormat.parse(isoDate)));
	}

	/**
	 * Test Zulu time conversion.
	 */
	public void testZuluTimeConversion() {
		TimeZone current = TimeZone.getDefault();
		// set to Zulu (00:00) time zone
		TimeZone.setDefault(TimeZone.getTimeZone("Z"));
		String isoDate = "2013-05-27T00:00:00.000Z";
		Assert.assertEquals(isoDate, ISO8601DateFormat.format(ISO8601DateFormat.parse(isoDate)));
		TimeZone.setDefault(current);
	}

	/**
	 * Test with invalid data.
	 */
	public void testWithInvalidData() {
		Assert.assertNull(ISO8601DateFormat.format((Date) null));
		Assert.assertNull(ISO8601DateFormat.parse(null));
		Assert.assertNull(ISO8601DateFormat.parse(""));
		Assert.assertNull(ISO8601DateFormat.parse("    "));
	}

}
