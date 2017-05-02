package com.sirma.itt.emf.semantic.search.operation.inversed;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.emf.semantic.search.operation.inverse.DoesNotContainSearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Tests the query building in {@link DoesNotContainSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class DoesNotContainSearchOperationTest {

	private DoesNotContainSearchOperation doesNotContainSearchOperation;

	@Before
	public void initialize() {
		doesNotContainSearchOperation = new DoesNotContainSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain", "test");
		Assert.assertTrue(doesNotContainSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "contains", "test");
		Assert.assertFalse(doesNotContainSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "", "test");
		Assert.assertFalse(doesNotContainSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", null, "test");
		Assert.assertFalse(doesNotContainSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain",
													Collections.singletonList("test"));
		StringBuilder builder = new StringBuilder();

		doesNotContainSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR  . } ) }";
		expected += " UNION { ?instance field VAR  FILTER ( regex(lcase(str(VAR)), \"^((?!\\\\Qtest\\\\E).)*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationWithMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain",
				Arrays.asList("test", "abv"));
		StringBuilder builder = new StringBuilder();

		doesNotContainSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR  . } ) }";
		expected += " UNION { ?instance field VAR  FILTER ( regex(lcase(str(VAR)), \"^((?!\\\\Qtest\\\\E).)*$\", \"i\")";
		expected += " && regex(lcase(str(VAR)), \"^((?!\\\\Qabv\\\\E).)*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationWithoutValues() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain");
		StringBuilder builder = new StringBuilder();

		doesNotContainSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationWithEmptyValues() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		doesNotContainSearchOperation.buildOperation(builder, rule);

		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationWithQuotesInValue() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_contain",
													Arrays.asList("test \"with quotes\""));
		StringBuilder builder = new StringBuilder();

		doesNotContainSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR  . } ) }";
		expected += " UNION { ?instance field VAR  FILTER ( regex(lcase(str(VAR)), \"^((?!\\\\Qtest \\\"with quotes\\\"\\\\E).)*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}
}
