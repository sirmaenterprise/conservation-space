package com.sirma.itt.emf.semantic.search.operation.inversed;

import com.sirma.itt.emf.semantic.search.operation.inverse.DoesNotStartWithSearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link DoesNotStartWithSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class DoesNotStartWithSearchOperationTest {

	private DoesNotStartWithSearchOperation doesNotStartWithSearchOperation;

	@Before
	public void initialize() {
		doesNotStartWithSearchOperation = new DoesNotStartWithSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_start_with", "test");
		Assert.assertTrue(doesNotStartWithSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "starts_with", "test");
		Assert.assertFalse(doesNotStartWithSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "", "test");
		Assert.assertFalse(doesNotStartWithSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_start_with",
				Collections.singletonList("test"));
		StringBuilder builder = new StringBuilder();

		doesNotStartWithSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationWithMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_start_with",
				Arrays.asList("test", "abv"));
		StringBuilder builder = new StringBuilder();

		doesNotStartWithSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E).*$\", \"i\")";
		expected += " && regex(lcase(str(VAR)), \"^(?!\\\\Qabv\\\\E).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}
}
