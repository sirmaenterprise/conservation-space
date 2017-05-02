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
import com.sirma.itt.seip.search.converters.BeforeDateConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Tests for BeforeDateConverter.
 * 
 * @author Boyan Tonchev
 *
 */
public class BeforeDateConverterTest {
	@Mock
	protected DateConverter dateConverter;

	@InjectMocks
	private BeforeDateConverter beforeDateConverter;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Test method convert() scenario with empty value.
	 */
	@Test
	public void convertEmptyValuesTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"before\",\"type\": \"dateTime\"}"), JsonObject.class::cast);
		Assert.assertEquals(beforeDateConverter.convert(null, filterCriteria), new DateRange(null, null));
	}
	
	/**
	 * Test method convert() scenario with empty dates.
	 */
	@Test
	public void convertEmptyDatesTest() {
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"before\",\"type\": \"dateTime\",\"value\": \"\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(beforeDateConverter.convert(null, filterCriteria), new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method convert().
	 */
	@Test
	public void convertTest() {
		SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");
		Date endDate = Calendar.getInstance().getTime();
		String formatedStartDate = df.format(endDate);
		Mockito.when(dateConverter.parseDate(formatedStartDate)).thenReturn(endDate);
		JsonObject filterCriteria = JSON.read(new StringReader("{\"field\": \"emf:createdOn\",\"operator\": \"before\",\"type\": \"dateTime\",\"value\":\"" + formatedStartDate + "\"}"), JsonObject.class::cast);
		AbstractDateRangeConverterTest.assertDateRange(beforeDateConverter.convert(null, filterCriteria), new DateRange(null, endDate), AbstractDateRangeConverterTest.SDF);
	}
	
	/**
	 * Test method getSupportedOperators
	 */
	@Test
	public void getSupportedOperatorsTest() {
		Assert.assertEquals(beforeDateConverter.getSupportedOperators(), Arrays.asList("before"));
	}
}
