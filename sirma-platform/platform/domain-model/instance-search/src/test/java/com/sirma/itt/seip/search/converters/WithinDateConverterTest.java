package com.sirma.itt.seip.search.converters;

import java.io.StringReader;
import java.util.Arrays;

import javax.json.JsonObject;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.search.converters.WithinDateConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Tests for WithinDateConverter.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class WithinDateConverterTest {
	
	private WithinDateConverter withinDateConverter = new WithinDateConverter();
	
	/**
	 * Tests method getSupportedOperators
	 */
	@Test
	public void getSupportedOperatorsTest() {
		Assert.assertEquals(withinDateConverter.getSupportedOperators(), Arrays.asList("within"));
	}
	
	/**
	 * Tests method canConvert().
	 *
	 * @param operator the operator
	 * @param expectedResult the expected result
	 * @param errorMessage the error message
	 */
	@Test(dataProvider = "canConvertDP")
	public void canConvertTest(String operator, boolean expectedResult, String errorMessage) {
		Assert.assertEquals(withinDateConverter.canConvert(operator), expectedResult, errorMessage);
	}
	
	/**
	 * Data provider for canConvertTest.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] canConvertDP() {
		return new Object[][] {
			{null, Boolean.FALSE, "null operator test"},
			{"is", Boolean.FALSE, "test without correct operator"},
			{"within", Boolean.TRUE, "test with correct operator"},
		};
	}
	
	/**
	 * Test method convert scenario missing value.
	 */
	@Test
	public void convertWithoutValueTest() {
		DateRange convert = withinDateConverter.convert(new DateTime(), JSON.read(new StringReader("{\"field\":\"emf:createdOn\",\"operator\":\"is\",\"type\":\"dateTime\"}"), JsonObject.class::cast));
		AbstractDateRangeConverterTest.assertDateRange(convert, new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert scenario with wrong size of value.
	 */
	@Test
	public void convertNotCorrectSizeOfArrayTest() {
		DateRange convert = withinDateConverter.convert(new DateTime(), JSON.read(new StringReader("{\"field\":\"emf:createdOn\",\"operator\":\"is\",\"type\":\"dateTime\",\"value\":[\"today\",1]}"), JsonObject.class::cast));
		AbstractDateRangeConverterTest.assertDateRange(convert, new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test all methods.
	 *
	 * @param filterCriteriaAsString the filter criteria as string
	 * @param expectedResult the expected result
	 * @param errorMessage the error message
	 */
	@Test(dataProvider = "convertDP")
	public void convertTest(DateTime current, String filterCriteriaAsString, DateRange expectedResult, String errorMessage) {
		JsonObject filterCriteria = JSON.read(new StringReader(filterCriteriaAsString.toString()), JsonObject.class::cast);
		DateRange converted= withinDateConverter.convert(current, filterCriteria);
		AbstractDateRangeConverterTest.assertDateRange(converted, expectedResult, AbstractDateRangeConverterTest.SDF, errorMessage);
	}
	
	/**
	 * Data provider for convertTest.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public Object[][] convertDP() {
		
		DateTime current = new DateTime();
		
		//test today
		String todayFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"today\",1,\"days\"]}";
		DateRange expectedResultToday = new DateRange(current.minusDays(1).toDate(), current.toDate());
		
		//next tests
		//test next 1 day
		String nextOneDayFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"days\"]}";
		DateRange expectedResultNextOneDay = new DateRange(current.toDate(), current.plusDays(1).toDate());
		//test next 1 hour
		String nextOneHourFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"hours\"]}";
		DateRange expectedResultNextOneHour = new DateRange(current.toDate(), current.plusHours(1).toDate());
		//test next 1 week
		String nextOneWeekFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"weeks\"]}";
		DateRange expectedResulNtextOneWeek = new DateRange(current.toDate(), current.plusWeeks(1).toDate());
		//test next 1 month
		String nextOneMonthFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"months\"]}";
		DateRange expectedResultNextOneMonth = new DateRange(current.toDate(), current.plusMonths(1).toDate());
		//test next 1 year
		String nextOneYearFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"years\"]}";
		DateRange expectedResultNextOneYear = new DateRange(current.toDate(), current.plusYears(1).toDate());

		//last tests
		//test last 1 day
		String lastOneDayFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"days\"]}";
		DateRange expectedResultLastOneDay = new DateRange(current.minusDays(1).toDate(), current.toDate());
		//test last 1 hour
		String lastOneHourFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"hours\"]}";
		DateRange expectedResultLastOneHour = new DateRange(current.minusHours(1).toDate(), current.toDate());
		//test last 1 week
		String lastOneWeekFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"weeks\"]}";
		DateRange expectedResultLastOneWeek = new DateRange(current.minusWeeks(1).toDate(), current.toDate());
		//test last 1 month
		String lastOneMonthFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"months\"]}";
		DateRange expectedResultLastOneMonth = new DateRange(current.minusMonths(1).toDate(), current.toDate());
		//test last 1 year
		String lastOneYearFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"years\"]}";
		DateRange expectedResultLastOneYear = new DateRange(current.minusYears(1).toDate(), current.toDate());
		
		//after tests
		//test after 1 day
		String afterOneDayFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"days\"]}";
		DateRange expectedResultAfterOneDay = new DateRange(current.plusDays(1).toDate(), null);
		//test after 1 hour
		String afterOneHourFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"hours\"]}";
		DateRange expectedResultAfterOneHour = new DateRange(current.plusHours(1).toDate(), null);
		//test after 1 week
		String afterOneWeekFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"weeks\"]}";
		DateRange expectedResultAfterOneWeek = new DateRange(current.plusWeeks(1).toDate(), null);
		//test after 1 month
		String afterOneMonthFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"months\"]}";
		DateRange expectedResultAfterOneMonth = new DateRange(current.plusMonths(1).toDate(), null);
		//test after 1 year
		String afterOneYearFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"years\"]}";
		DateRange expectedResultAfterOneYear = new DateRange(current.plusYears(1).toDate(), null);
		
		//before tests
		//test before 1 day
		String beforeOneDayFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"days\"]}";
		DateRange expectedResultBeforeOneDay = new DateRange(null, current.minusDays(1).toDate());
		//test before 1 hour
		String beforeOneHourFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"hours\"]}";
		DateRange expectedResultBeforeOneHour = new DateRange(null, current.minusHours(1).toDate());
		//test before 1 week
		String beforeOneWeekFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"weeks\"]}";
		DateRange expectedResultBeforeOneWeek = new DateRange(null, current.minusWeeks(1).toDate());
		//test before 1 month
		String beforeOneMonthFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"months\"]}";
		DateRange expectedResultBeforeOneMonth = new DateRange(null, current.minusMonths(1).toDate());
		//test before 1 year
		String beforeOneYearFilterCriteriaAsString = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"years\"]}";
		DateRange expectedResultBeforeOneYear = new DateRange(null, current.minusYears(1).toDate());
		
		
		//test default of convertInternal
		String defaultConvertInternalJson = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"unknown\",1,\"days\"]}";
		DateRange expectedResultDefaultConvertInterna = new DateRange(null, null);
		
		//test default of createNextRange
		String defaultCreateNextRangeJson = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"next\",1,\"unknown\"]}";
		DateRange expectedResultCreateNext = new DateRange(current.toDate(), null);
		
		//test default of createLastRange
		String defaultCreateLastRangeJson = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"last\",1,\"unknown\"]}";
		DateRange expectedResultCreateLastRange = new DateRange(null, current.toDate());
		
		//test default of createAfterRange
		String defaultCreateAfterRangeJson = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"after\",1,\"unknown\"]}";
		DateRange expectedResultCreateAfterRange = new DateRange(null, null);
		
		//test default of createBeforeRange
		String defaultCreateBeforeRangeJson = "{\"field\":\"emf:createdOn\",\"operator\":\"within\",\"type\":\"dateTime\",\"value\":[\"before\",1,\"unknown\"]}";
		DateRange expectedResultCreateBeforeRange = new DateRange(null, null);
		
		
		return new Object[][] {
			{current, todayFilterCriteriaAsString, expectedResultToday, "today test "},
			
			{current, nextOneDayFilterCriteriaAsString, expectedResultNextOneDay, "next one day test "},
			{current, nextOneHourFilterCriteriaAsString, expectedResultNextOneHour, "next one hour test "},
			{current, nextOneWeekFilterCriteriaAsString, expectedResulNtextOneWeek, "next one week test "},
			{current, nextOneMonthFilterCriteriaAsString, expectedResultNextOneMonth, "next one month test "},
			{current, nextOneYearFilterCriteriaAsString, expectedResultNextOneYear, "next one year test "},
			
			{current, lastOneDayFilterCriteriaAsString, expectedResultLastOneDay, "last one day test "},
			{current, lastOneHourFilterCriteriaAsString, expectedResultLastOneHour, "last one hour test "},
			{current, lastOneWeekFilterCriteriaAsString, expectedResultLastOneWeek, "last one week test "},
			{current, lastOneMonthFilterCriteriaAsString, expectedResultLastOneMonth, "last one month test "},
			{current, lastOneYearFilterCriteriaAsString, expectedResultLastOneYear, "last one year test "},
			
			{current, afterOneDayFilterCriteriaAsString, expectedResultAfterOneDay, "after one day test "},
			{current, afterOneHourFilterCriteriaAsString, expectedResultAfterOneHour, "after one hour test "},
			{current, afterOneWeekFilterCriteriaAsString, expectedResultAfterOneWeek, "after one week test "},
			{current, afterOneMonthFilterCriteriaAsString, expectedResultAfterOneMonth, "after one month test "},
			{current, afterOneYearFilterCriteriaAsString, expectedResultAfterOneYear, "after one year test "},
			
			{current, beforeOneDayFilterCriteriaAsString, expectedResultBeforeOneDay, "before one day test "},
			{current, beforeOneHourFilterCriteriaAsString, expectedResultBeforeOneHour, "before one hour test "},
			{current, beforeOneWeekFilterCriteriaAsString, expectedResultBeforeOneWeek, "before one week test "},
			{current, beforeOneMonthFilterCriteriaAsString, expectedResultBeforeOneMonth, "before one month test "},
			{current, beforeOneYearFilterCriteriaAsString, expectedResultBeforeOneYear, "before one year test "},
			
			{current, defaultConvertInternalJson, expectedResultDefaultConvertInterna, "test default of convertInternal "},
			{current, defaultCreateNextRangeJson, expectedResultCreateNext, "test default of createNextRange "},
			{current, defaultCreateLastRangeJson, expectedResultCreateLastRange, "test default of createLastRange "},
			{current, defaultCreateAfterRangeJson, expectedResultCreateAfterRange, "test default of createAfterRange "},
			{current, defaultCreateBeforeRangeJson, expectedResultCreateBeforeRange, "test default of createBeforeRange "}
		};
	}
}
