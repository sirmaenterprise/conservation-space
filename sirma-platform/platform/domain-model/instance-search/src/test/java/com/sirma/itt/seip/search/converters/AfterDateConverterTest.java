package com.sirma.itt.seip.search.converters;

import java.io.StringReader;
import java.text.SimpleDateFormat;
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
import com.sirma.itt.seip.search.converters.AfterDateConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Test method AfterDateConverter.
 * 
 * @author Boyan Tonchev
 *
 */
public class AfterDateConverterTest {
	
	@Mock
	protected DateConverter dateConverter;

	@InjectMocks
	private AfterDateConverter afterDateConverter;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Test method convert() scenario with empty value.
	 */
	@Test
	public void convertEmptyValueTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"after\",\"type\": \"dateTime\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(afterDateConverter.convert(null, filterCriteria), new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert() scenario with empty dates.
	 */
	@Test
	public void convertEmptyDatesTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"after\",\"type\": \"dateTime\",\"value\": \"\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(afterDateConverter.convert(null, filterCriteria), new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert().
	 */
	@Test
	public void convertTest() {
		SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");
		Date startDate = Calendar.getInstance().getTime();
		String formatedStartDate = df.format(startDate);
		Mockito.when(dateConverter.parseDate(formatedStartDate)).thenReturn(startDate);
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"after\",\"type\": \"dateTime\",\"value\":\"" + formatedStartDate + "\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(afterDateConverter.convert(null, filterCriteria), new DateRange(startDate, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method getSupportedOperators
	 */
	@Test
	public void getSupportedOperatorsTest() {
		Assert.assertEquals(afterDateConverter.getSupportedOperators(), Arrays.asList("after"));
	}
}
