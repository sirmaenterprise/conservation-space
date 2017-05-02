package com.sirma.itt.emf.web.util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for DateUtil class.
 *
 * @author svelikov
 */
@Test
public class DateUtilTest {

	private static final String dateValue = "10.10.2015";
	private static final String dateTimeValue = "10.10.2015, 10:10";

	@InjectMocks
	private DateUtil dateUtil;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	DateConverter dateConverter;

	/**
	 * Instantiates a new date util test.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(dateConverter.getConverterDatetimeFormatPattern())
				.thenReturn(new ConfigurationPropertyMock<>("dd.MM.yyyy, HH:mm"));
		when(dateConverter.getConverterDateFormatPattern()).thenReturn(new ConfigurationPropertyMock<>("dd.MM.yyyy"));

		when(dateConverter.formatDate(any())).thenReturn("date");
		when(dateConverter.formatDateTime(any())).thenReturn("datetime");

		when(dateConverter.parseDate(dateValue)).thenReturn(new Date());
		when(dateConverter.parseDate(dateTimeValue)).thenThrow(new TypeConversionException("Some random text."));
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

	/**
	 * Test for parseDate.
	 */
	public void parseDateTest() {
		Date result = dateUtil.parseDate(dateValue);
		assertNotNull(result);

		result = dateUtil.parseDate(dateTimeValue);
		assertNull(result);
	}

}
