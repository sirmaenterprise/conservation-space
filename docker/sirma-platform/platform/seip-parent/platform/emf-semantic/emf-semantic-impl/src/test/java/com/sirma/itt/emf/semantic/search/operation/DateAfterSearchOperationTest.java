package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the query building in {@link DateAfterSearchOperation}.
 * 
 * @author Mihail Radkov
 */
public class DateAfterSearchOperationTest {

	private DateAfterSearchOperation dateAfterSearchOperation;

	@Before
	public void initialize() {
		dateAfterSearchOperation = new DateAfterSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
		Assert.assertTrue(dateAfterSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
		Assert.assertFalse(dateAfterSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "", "date");
		Assert.assertFalse(dateAfterSearchOperation.isApplicable(rule));
	}

	@Test
	public void testAfterDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
		StringBuilder builder = new StringBuilder();

		dateAfterSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance dateField VAR.  FILTER ( VAR >= xsd:dateTime(\"date\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testWithEmptyAfterDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "");
		StringBuilder builder = new StringBuilder();

		dateAfterSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testWithNullAfterDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "after");
		StringBuilder builder = new StringBuilder();

		dateAfterSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}
}
