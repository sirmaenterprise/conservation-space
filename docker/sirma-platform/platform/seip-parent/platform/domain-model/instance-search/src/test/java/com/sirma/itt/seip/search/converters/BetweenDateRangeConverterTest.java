package com.sirma.itt.seip.search.converters;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.json.JsonObject;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.search.converters.BetweenDateRangeConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Tests for BetweenDateRangeConverter.
 * 
 * @author Boyan Tonchev
 *
 */
public class BetweenDateRangeConverterTest {
	
	@Mock
	protected DateConverter dateConverter;

	@InjectMocks
	private BetweenDateRangeConverter betweenDateRangeConverter;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Test method convert() scenario with empty value.
	 */
	@Test
	public void convertEmptyValueTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"between\",\"type\": \"dateTime\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(betweenDateRangeConverter.convert(null, filterCriteria), new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert() scenario with empty dates.
	 */
	@Test
	public void convertEmptyDatesTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"between\",\"type\": \"dateTime\",\"value\": [\"\", \"\"]}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(betweenDateRangeConverter.convert(null, filterCriteria), new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert().
	 */
	@Test
	public void convertTest() {
		Date startDate = Calendar.getInstance().getTime();
		Date endDate = Calendar.getInstance().getTime();
		String formatedStartDate = AbstractDateRangeConverterTest.SDF.format(startDate);
		String formatedEndDate = AbstractDateRangeConverterTest.SDF.format(endDate);
		Mockito.when(dateConverter.parseDate(formatedStartDate)).thenReturn(startDate);
		Mockito.when(dateConverter.parseDate(formatedEndDate)).thenReturn(endDate);
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"between\",\"type\": \"dateTime\",\"value\": [\"" + formatedStartDate + "\",\"" + formatedEndDate + "\"]}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(betweenDateRangeConverter.convert(null, filterCriteria), new DateRange(startDate, endDate), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method getSupportedOperators
	 */
	@Test
	public void getSupportedOperatorsTest() {
		Assert.assertEquals(betweenDateRangeConverter.getSupportedOperators(), Arrays.asList("between", "is"));
	}
}
