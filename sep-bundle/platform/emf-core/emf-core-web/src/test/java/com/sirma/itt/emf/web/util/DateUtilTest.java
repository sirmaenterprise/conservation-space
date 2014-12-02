package com.sirma.itt.emf.web.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;

import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;

/**
 * Test for DateUtil class.
 * 
 * @author svelikov
 */
@Test
public class DateUtilTest {

	private final DateUtil dateUtil;
	private final TypeConverter typeConverter;

	/**
	 * Instantiates a new date util test.
	 */
	public DateUtilTest() {
		dateUtil = new DateUtil();

		typeConverter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setField(dateUtil, "typeConverter", typeConverter);
		ReflectionUtils.setField(dateUtil, "converterDatetimeFormatPattern", "dd.MM.yyyy, HH:mm");
		ReflectionUtils.setField(dateUtil, "converterDateFormatPattern", "dd.MM.yyyy");
	}

	/**
	 * Inits the test class.
	 */
	@BeforeTest
	public void initTestClass() {
		dateUtil.init();
	}

	/**
	 * Test for init method.
	 */
	public void initTest() {
		dateUtil.init();
		DateFormat dateTimeFormat = (DateFormat) ReflectionUtils.getField(dateUtil,
				"dateTimeFormat");
		assertNotNull(dateTimeFormat);
		DateFormat dateFormat = (DateFormat) ReflectionUtils.getField(dateUtil, "dateFormat");
		assertNotNull(dateFormat);
	}

	/**
	 * Test for getFormattedDateTime method.
	 */
	public void getFormattedDateTimeTest() {
		String formattedDateTime = dateUtil.getFormattedDateTime(null);
		assertNull(formattedDateTime);

		formattedDateTime = dateUtil.getFormattedDateTime(new Date());
		assertNotNull(formattedDateTime);
	}

	/**
	 * Test for getFormattedDate method.
	 */
	public void getFormattedDateTest() {
		String formattedDate = dateUtil.getFormattedDate(null);
		assertNull(formattedDate);

		formattedDate = dateUtil.getFormattedDate(new Date());
		assertNotNull(formattedDate);
	}

	/**
	 * Test for getISOFormattedDateTime method.
	 */
	public void getISOFormattedDateTimeTest() {
		String formattedDate = dateUtil.getISOFormattedDateTime(null);
		assertNull(formattedDate);

		Date date = new Date();
		Mockito.when(typeConverter.convert(String.class, date)).thenReturn("converted date");
		formattedDate = dateUtil.getISOFormattedDateTime(date);
		assertNotNull(formattedDate);
	}

	/**
	 * Test for getISODateTime method.
	 */
	public void getISODateTimeTest() {
		Date isoDateTime = dateUtil.getISODateTime(null);
		assertNull(isoDateTime);

		Date date = new Date();
		Mockito.when(typeConverter.convert(Date.class, "formatted date")).thenReturn(date);
		isoDateTime = dateUtil.getISODateTime("formatted date");
		assertNotNull(isoDateTime);
	}

	/**
	 * Convert minutes to time string test.
	 */
	public void convertMinutesToTimeStringTest() {
		String converted = dateUtil.convertMinutesToTimeString(null);
		assertTrue(converted.length() == 0);

		converted = dateUtil.convertMinutesToTimeString(0);
		assertEquals(converted, "");

		converted = dateUtil.convertMinutesToTimeString(-1);
		assertEquals(converted, "");

		converted = dateUtil.convertMinutesToTimeString(1);
		assertEquals(converted, "1m");

		converted = dateUtil.convertMinutesToTimeString(60);
		assertEquals(converted, "1h");

		converted = dateUtil.convertMinutesToTimeString(61);
		assertEquals(converted, "1h 1m");

		converted = dateUtil.convertMinutesToTimeString(480);
		assertEquals(converted, "1d");

		converted = dateUtil.convertMinutesToTimeString(481);
		assertEquals(converted, "1d 1m");

		converted = dateUtil.convertMinutesToTimeString(540);
		assertEquals(converted, "1d 1h");

		converted = dateUtil.convertMinutesToTimeString(541);
		assertEquals(converted, "1d 1h 1m");

		converted = dateUtil.convertMinutesToTimeString(48000);
		assertEquals(converted, "100d");
	}

}
