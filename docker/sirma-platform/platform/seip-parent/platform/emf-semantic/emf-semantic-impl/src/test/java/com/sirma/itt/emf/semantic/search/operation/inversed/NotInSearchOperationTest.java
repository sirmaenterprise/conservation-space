package com.sirma.itt.emf.semantic.search.operation.inversed;

import com.sirma.itt.emf.semantic.search.operation.inverse.NotInSearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link NotInSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class NotInSearchOperationTest {

	private NotInSearchOperation notInSearchOperation;

	@Before
	public void initialize() {
		notInSearchOperation = new NotInSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("field", "codelist", "not_in", "test");
		Assert.assertTrue(notInSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "codelist", "in", "test");
		Assert.assertFalse(notInSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "codelist", "", "test");
		Assert.assertFalse(notInSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "codelist", "not_in", Collections.singletonList("test"));
		StringBuilder builder = new StringBuilder();

		notInSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E$).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationWithMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "codelist", "not_in", Arrays.asList("test", "abv"));
		StringBuilder builder = new StringBuilder();

		notInSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E$).*$\", \"i\")";
		expected += " && regex(lcase(str(VAR)), \"^(?!\\\\Qabv\\\\E$).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}
}
