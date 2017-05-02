package com.sirma.itt.emf.semantic.search.operation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Tests the query building in {@link DateBeforeSearchOperation}.
 * 
 * @author Mihail Radkov
 */
public class DateBeforeSearchOperationTest {

	private DateBeforeSearchOperation dateBeforeSearchOperation;

	@Before
	public void initialize() {
		dateBeforeSearchOperation = new DateBeforeSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
		Assert.assertTrue(dateBeforeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "after", "date");
		Assert.assertFalse(dateBeforeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", "", "date");
		Assert.assertFalse(dateBeforeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("dateField", "dateTime", null, "date");
		Assert.assertFalse(dateBeforeSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBeforeDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "date");
		StringBuilder builder = new StringBuilder();

		dateBeforeSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ ?instance dateField VAR  FILTER ( VAR < xsd:dateTime(\"date\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testWithEmptyBeforeDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before", "");
		StringBuilder builder = new StringBuilder();

		dateBeforeSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testWithNullBeforeDate() {
		Rule rule = SearchOperationUtils.createRule("dateField", "dateTime", "before");
		StringBuilder builder = new StringBuilder();

		dateBeforeSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}
}
