package com.sirma.itt.seip.search.converters;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.search.converters.AfterDateConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Test for AbstractDateRangeConverter.
 * 
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class AbstractDateRangeConverterTest {

	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd.mm.yyyy");

	@Mock
	protected DateConverter dateConverter;

	@InjectMocks
	private AfterDateConverter afterDateConverter;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests method convertToDate scenarios with blank date.
	 * 
	 * @param dateAsString
	 *            date as string.
	 * @param expectedResult
	 *            the expected result.
	 * @param errorMessage
	 *            - error message if fail.
	 */
	@Test(dataProvider = "convertToDateDP")
	public void convertToDateTest(String dateAsString, Date expectedResult, String errorMessage) {
		Assert.assertEquals(afterDateConverter.convertToDate(dateAsString), expectedResult, errorMessage);
	}

	/**
	 * Data provider for convertToDateTest.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] convertToDateDP() {
		return new Object[][] { { null, null, "test null date as string" }, { null, null, "test empty date" } };
	}

	public static void assertDateRange(DateRange first, DateRange second, SimpleDateFormat sdf, String errorMessage) {
		assertDate(first.getFirst(), second.getFirst(), sdf, errorMessage);
		assertDate(first.getSecond(), second.getSecond(), sdf, errorMessage);
	}

	public static void assertDateRange(DateRange first, DateRange second, SimpleDateFormat sdf) {
		assertDateRange(first, second, sdf, "");
	}

	private static void assertDate(Date first, Date second, SimpleDateFormat sdf, String errorMessage) {
		if (first != null && second != null) {
			Assert.assertEquals(sdf.format(first), sdf.format(second), errorMessage);
		} else {
			Assert.assertNull(first, errorMessage);
			Assert.assertNull(second, errorMessage);
		}
	}
}
