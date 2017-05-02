package com.sirma.itt.emf.semantic.search.operation;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Tests the query building in {@link ContainsSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class ContainsSearchOperationTest {

	private ContainsSearchOperation containsSearchOperation;

	@Before
	public void initialize() {
		containsSearchOperation = new ContainsSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test");
		Assert.assertTrue(containsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "fts", "contains", "test");
		Assert.assertFalse(containsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
		Assert.assertFalse(containsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "string", null, "test");
		Assert.assertFalse(containsSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "dateTime", null, "test");
		Assert.assertFalse(containsSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals("{ ?instance a VAR  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest\\\\E\", \"i\")) }", query);
	}

	@Test
	public void testBuildOperationWithNoValue() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals("", query);
	}

	@Test
	public void testBuildOperationWithAnyField() {
		Rule rule = SearchOperationUtils.createRule("anyField", "string", "contains", "test");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals("{ ?instance VAR VAR  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest\\\\E\", \"i\")) }", query);
	}

	@Test
	public void testBuildOperationWithEmptyValue() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals("", query);
	}

	@Test
	public void testBuildOperationWithQuotesAsValue() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test \"with quotes\"");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals(
				"{ ?instance a VAR  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest \\\"with quotes\\\"\\\\E\", \"i\")) }", query);
	}

}
