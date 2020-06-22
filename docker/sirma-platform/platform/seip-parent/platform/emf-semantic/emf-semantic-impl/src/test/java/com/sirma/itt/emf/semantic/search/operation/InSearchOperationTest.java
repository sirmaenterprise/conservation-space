package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link InSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class InSearchOperationTest {

	private InSearchOperation inSearchOperation;

	@Before
	public void initialize() {
		inSearchOperation = new InSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Collections.singletonList("1"));
		Assert.assertTrue(inSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("status", "codelist", "", Collections.singletonList("1"));
		Assert.assertFalse(inSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("instanceId", "codelist", "in", Collections.singletonList("1"));
		Assert.assertFalse(inSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperationForSingleValue() {
		Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Collections.singletonList("1"));
		StringBuilder builder = new StringBuilder();

		inSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance status VAR.  FILTER ( regex(lcase(str(VAR)), \"^\\\\Q1\\\\E$\", \"i\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationForMultipleValue() {
		Rule rule = SearchOperationUtils.createRule("status", "codelist", "in", Arrays.asList("1", "2"));
		StringBuilder builder = new StringBuilder();

		inSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance status VAR.  FILTER ( regex(lcase(str(VAR)), \"^\\\\Q1\\\\E$\", \"i\") || regex(lcase(str(VAR)), \"^\\\\Q2\\\\E$\", \"i\")) ";
		Assert.assertEquals(expected, query);
	}
}
