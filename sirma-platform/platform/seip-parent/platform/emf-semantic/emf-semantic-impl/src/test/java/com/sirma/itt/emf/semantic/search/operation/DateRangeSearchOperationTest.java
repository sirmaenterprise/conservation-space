package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests the query building in {@link DateRangeSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class DateRangeSearchOperationTest {

	private DateRangeSearchOperation dateRangeSearchOperation;

	@Before
	public void initialize() {
		dateRangeSearchOperation = new DateRangeSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "between", Arrays.asList("date", "date"));
		Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", Arrays.asList("date", "date"));
		Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "within", Arrays.asList("date", "date"));
		Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "date", "is", Arrays.asList("date", "date"));
		Assert.assertTrue(dateRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "", Arrays.asList("date", "date"));
		Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "is", Collections.emptyList());
		Assert.assertFalse(dateRangeSearchOperation.isApplicable(rule));
	}

	@Test
	public void testDateRange() {
		List<String> dates = Arrays.asList("from", "to");
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", dates);
		StringBuilder builder = new StringBuilder();

		dateRangeSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance dateField VAR.  FILTER ( VAR >= xsd:dateTime(\"from\")) ";
		expected += " FILTER ( VAR < xsd:dateTime(\"to\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testDateRangeWithEmptyDate() {
		// To is empty
		List<String> dates = Arrays.asList("from", "");
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", dates);
		StringBuilder builder = new StringBuilder();

		dateRangeSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance dateField VAR.  FILTER ( VAR >= xsd:dateTime(\"from\")) ";
		Assert.assertEquals(expected, query);

		// From is empty
		builder = new StringBuilder();
		dates = Arrays.asList("", "to");
		rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", dates);

		dateRangeSearchOperation.buildOperation(builder, rule);

		query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		expected = " ?instance dateField VAR.  FILTER ( VAR < xsd:dateTime(\"to\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testDateRangeWithBothDatesEmpty() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", Arrays.asList("", ""));
		StringBuilder builder = new StringBuilder();

		dateRangeSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}
}
