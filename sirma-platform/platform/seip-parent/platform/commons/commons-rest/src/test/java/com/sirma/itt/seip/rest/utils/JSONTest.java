package com.sirma.itt.seip.rest.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Test for {@link JSON}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings({ "static-method", "boxing" })
public class JSONTest {

	private static final JsonObject EMPTY_JSON_OBJECT = Json.createObjectBuilder().build();

	private static final TimeZone DTZ = TimeZone.getDefault();

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Before
	public void setup() {
		TimeZone.setDefault(UTC);
	}

	@After
	public void after() {
		TimeZone.setDefault(DTZ);
	}

	@Test
	public void getDateRange_nullJson() {
		assertNull(JSON.getDateRange(null));
	}

	@Test
	public void getDateRange_emptyJson() {
		assertNull(JSON.getDateRange(EMPTY_JSON_OBJECT));
	}

	@Test
	public void getDateRange_emptyDateRangeJson() {
		JsonObject emptyDateRangeJson = Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, EMPTY_JSON_OBJECT).build();
		assertNull(JSON.getDateRange(emptyDateRangeJson));
	}

	@Test
	public void getDateRange_withEmptyStartDate() {
		DateRange dateRange = JSON.getDateRange(buildDateRangeTestJson("", ISO8601DateFormat.format(new Date())));
		assertNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_withEmptyEndDate() {
		DateRange dateRange = JSON.getDateRange(buildDateRangeTestJson(ISO8601DateFormat.format(new Date()), ""));
		assertNotNull(dateRange.getFirst());
		assertNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_withBothEmpty() {
		assertNull(JSON.getDateRange(buildDateRangeTestJson("", "")));
	}

	@Test
	public void getDateRange_withOneDate() {
		JsonObjectBuilder dates = Json.createObjectBuilder().add(JsonKeys.END, ISO8601DateFormat.format(new Date()));
		JsonObject datesObject = Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, dates).build();
		DateRange dateRange = JSON.getDateRange(datesObject);
		assertNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_dateRangeWithBothDates() {
		DateRange dateRange = JSON.getDateRange(
				buildDateRangeTestJson(ISO8601DateFormat.format(new Date()), ISO8601DateFormat.format(new Date())));
		assertNotNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	private static JsonObject buildDateRangeTestJson(String start, String end) {
		JsonObjectBuilder dates = Json.createObjectBuilder().add(JsonKeys.START, start).add(JsonKeys.END, end);
		return Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, dates).build();
	}

}
