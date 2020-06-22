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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.json.JsonObject;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
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
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	@InjectMocks
	private JsonToDateRangeConverter jsonToDateRangeConverter;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("boxing")
	public void convertDateRangeConverterTest() {
		DateRangeConverter afterDateRangeConverter = mock(DateRangeConverter.class);
		when(afterDateRangeConverter.canConvert("after")).thenReturn(true);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(afterDateRangeConverter).iterator());

		jsonToDateRangeConverter.convertDateRange(filterCriteria);

		verify(afterDateRangeConverter).convert(any(), eq(filterCriteria));
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
	public void populateConditionRuleWithDateRange_emptyRulesInCondition_convertersNeverCalled() {
		Condition condition = SearchCriteriaBuilder.createConditionBuilder().setRules(new ArrayList<>()).build();
		populateConditionRuleWithDateRange_unsuccessful(condition);
	}

	private void populateConditionRuleWithDateRange_unsuccessful(Condition condition) {
		DateRangeConverter withinDateConverterSpy = spy(WithinDateConverter.class);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverterSpy).iterator());

		ConditionBuilder builder = SearchCriteriaBuilder.createConditionBuilder();
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(builder, condition);

		verify(withinDateConverterSpy, never()).canConvert("within");
		verify(withinDateConverterSpy, never()).convert(any(DateTime.class), any(JsonObject.class));
	}

	@Test
	public void populateConditionRuleWithDateRangeTest() {
		JsonObject searchCriteria = loadTestResource(TEST_JSON_FILE);
		Condition tree = converter.parseCondition(searchCriteria);
		DateRangeConverter withinDateConverterSpy = spy(WithinDateConverter.class);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverterSpy).iterator());
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(conditionBuilder, tree);
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

	@Test
	public void testPopulateConditionRuleWithDateRangeWithNestedCondition() {
		RuleBuilder ruleBuilder = SearchCriteriaBuilder.createRuleBuilder();

		ruleBuilder.setField("emf:createdOn")
				.setOperation("after")
				.setType("dateTime")
				.addValue("2016-12-06T22:00:00.000Z");

		ConditionBuilder nestedConditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		nestedConditionBuilder.addRule(ruleBuilder.build());

		RuleBuilder topLevelRuleBuilder = SearchCriteriaBuilder.createRuleBuilder()
				.setField("emf:modifiedOn")
				.setOperation("before")
				.setType("dateTime")
				.addValue("2016-12-07T22:00:00.000Z");

		Condition conditionTree = SearchCriteriaBuilder.createConditionBuilder()
				.addRule(topLevelRuleBuilder.build())
				.addRule(nestedConditionBuilder.build())
				.build();

		ConditionBuilder populatedBuilder = SearchCriteriaBuilder.createConditionBuilder();
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(populatedBuilder, conditionTree);

		// Should not duplicate
		Condition populatedRootCondition = populatedBuilder.build();
		Assert.assertEquals(2, populatedRootCondition.getRules().size());

		Rule topLevelRule = (Rule) populatedRootCondition.getRules().get(0);
		Assert.assertEquals("emf:modifiedOn", topLevelRule.getField());

		// Should not duplicate
		List<SearchNode> nestedRules = ((Condition) populatedRootCondition.getRules().get(1)).getRules();
		Assert.assertEquals(1, nestedRules.size());

		Rule convertedRule = (Rule) nestedRules.get(0);
		Assert.assertEquals(Collections.singletonList("2016-12-06T22:00:00.000Z"), convertedRule.getValues());
	}

	@Test
	public void shouldNotConvertNonDateRulesWithMultipleValues() {
		JsonObject searchCriteria = loadTestResource("json-to-daterange-converter-multiple-values.json");
		Condition tree = converter.parseCondition(searchCriteria);
		DateRangeConverter withinDateConverterSpy = spy(WithinDateConverter.class);
		when(dateRangeConverters.iterator()).thenReturn(Arrays.asList(withinDateConverterSpy).iterator());
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		jsonToDateRangeConverter.populateConditionRuleWithDateRange(conditionBuilder, tree);

		Condition converted = conditionBuilder.build();
		Assert.assertEquals(1, converted.getRules().size());

		Condition innerCondition = (Condition) converted.getRules().get(0);
		Assert.assertEquals(1, innerCondition.getRules().size());

		Rule typeRule = (Rule) innerCondition.getRules().get(0);
		Assert.assertEquals(3, typeRule.getValues().size());

		Assert.assertEquals("TYPE1", typeRule.getValues().get(0));
		Assert.assertEquals("TYPE2", typeRule.getValues().get(1));
		Assert.assertEquals("TYPE3", typeRule.getValues().get(2));
	}
}
