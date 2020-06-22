package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.CriteriaWildcards;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

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
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals(" ?instance a VAR.  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest\\\\E\", \"i\")) ", query);
	}

	@Test
	public void testBuildOperationWithAnyField() {
		Rule rule = SearchOperationUtils.createRule(CriteriaWildcards.ANY_FIELD, "string", "contains", "test");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals(" ?instance VAR VAR.  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest\\\\E\", \"i\")) ", query);
	}

	@Test
	public void testBuildOperationWithQuotesAsValue() {
		Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test \"with quotes\"");
		StringBuilder builder = new StringBuilder();

		containsSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		Assert.assertEquals(
				" ?instance a VAR.  FILTER ( regex(lcase(str(VAR)), \"\\\\Qtest \\\"with quotes\\\"\\\\E\", \"i\")) ",
				query);
	}

}
