package com.sirma.itt.seip.search.converters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import javax.json.JsonObject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.DateRange;

import junit.framework.AssertionFailedError;

/**
 * Tests for JsonToDateRangeConverter.
 *
 * @author Boyan Tonchev
 */
public class JsonToDateRangeConverterTest {

	private static final String TEST_JSON_FILE = "json-to-daterange-converter.json";
	private JsonObject filterCriteria = JSON.read(
			new StringReader(
					"{\"field\": \"emf:createdOn\",\"operator\": \"after\",\"type\": \"dateTime\",\"value\": \"2016-12-06T22:00:00.000Z\"}"),
			JsonObject.class::cast);

	@Mock
	@ExtensionPoint(AbstractDateRangeConverter.PLUGIN_NAME)
	private Plugins<DateRangeConverter> dateRangeConverters;

	@Spy
	private JsonToConditionConverter convertor = new JsonToConditionConverter();

	@InjectMocks
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("boxing")
	public void convertDateRangeConverterTest() {
		DateRangeConverter afterDateRangeConvertor = mock(DateRangeConverter.class);
		when(afterDateRangeConvertor.canConvert("after")).thenReturn(true);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(afterDateRangeConvertor).iterator());

		jsonToDateRangeConverter.convertDateRange(filterCriteria);

		verify(afterDateRangeConvertor).convert(any(), eq(filterCriteria));
	}

	@Test
	public void convertDateRangeWithoutFoundConverterTest() {
		DateRangeConverter withinDateConverter = new WithinDateConverter();
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverter).iterator());
		AbstractDateRangeConverterTest.assertDateRange(jsonToDateRangeConverter.convertDateRange(filterCriteria),
				new DateRange(null, null), AbstractDateRangeConverterTest.SDF);
	}

	@Test
	public void populateConditionRuleWithDateRange_nullCondition_convertersNeverCalled() {
		populateConditionRuleWithDateRange_unsuccessful(null);
	}

	@Test
	public void populateConditionRuleWithDateRange_nullRulesInCondition_convertersNeverCalled() {
		Condition condition = new Condition();
		condition.setRules(null);
		populateConditionRuleWithDateRange_unsuccessful(condition);
	}

	@Test
	public void populateConditionRuleWithDateRange_emptyRulesInCondition_convertersNeverCalled() {
		Condition condition = new Condition();
		condition.setRules(new ArrayList<>());
		populateConditionRuleWithDateRange_unsuccessful(condition);
	}

	private void populateConditionRuleWithDateRange_unsuccessful(Condition condition) {
		DateRangeConverter withinDateConverterSpy = spy(WithinDateConverter.class);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverterSpy).iterator());

		jsonToDateRangeConverter.populateConditionRuleWithDateRange(condition);

		verify(withinDateConverterSpy, never()).canConvert("within");
		verify(withinDateConverterSpy, never()).convert(any(DateTime.class), any(JsonObject.class));
	}

	@Test
	public void populateConditionRuleWithDateRangeTest() {
		JsonObject searchCriteria = loadTestResource(TEST_JSON_FILE);
		Condition tree = convertor.parseCondition(searchCriteria);
		DateRangeConverter withinDateConverterSpy = spy(WithinDateConverter.class);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverterSpy).iterator());
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(tree);
		verify(withinDateConverterSpy).canConvert("within");
		verify(withinDateConverterSpy).convert(any(DateTime.class), any(JsonObject.class));
	}

	private JsonObject loadTestResource(String resource) {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
			return JSON.readObject(stream, Function.identity());
		} catch (IOException e) {
			throw new AssertionFailedError(e.getMessage());
		}
	}

}
