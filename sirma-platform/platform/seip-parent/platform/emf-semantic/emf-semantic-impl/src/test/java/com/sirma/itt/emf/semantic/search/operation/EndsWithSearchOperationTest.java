package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link EndsWithSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class EndsWithSearchOperationTest {

	private EndsWithSearchOperation endsWithSearchOperation;

	@Before
	public void initialize() {
		endsWithSearchOperation = new EndsWithSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Collections.singletonList("123"));
		Assert.assertTrue(endsWithSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("title", "string", "", Collections.singletonList("123"));
		Assert.assertFalse(endsWithSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperationForSingleValue() {
		Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Collections.singletonList("123"));
		StringBuilder builder = new StringBuilder();

		endsWithSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance title VAR.  FILTER ( regex(lcase(str(VAR)), \"\\\\Q123\\\\E$\", \"i\")) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationForMultipleValue() {
		Rule rule = SearchOperationUtils.createRule("title", "string", "ends_with", Arrays.asList("123", "abc"));
		StringBuilder builder = new StringBuilder();

		endsWithSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance title VAR.  FILTER ( regex(lcase(str(VAR)), \"\\\\Q123\\\\E$\", \"i\") || regex(lcase(str(VAR)), \"\\\\Qabc\\\\E$\", \"i\")) ";
		Assert.assertEquals(expected, query);
	}
}
