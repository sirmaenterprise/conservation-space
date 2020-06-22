package com.sirma.itt.emf.semantic.search.operation.inversed;

import com.sirma.itt.emf.semantic.search.operation.inverse.DoesNotEqualSearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link DoesNotEqualSearchOperation}.
 *
 * @author Mihail Radkov
 */
public class DoesNotEqualSearchOperationTest {

	private DoesNotEqualSearchOperation doesNotEqualSearchOperation;

	@Before
	public void initialize() {
		doesNotEqualSearchOperation = new DoesNotEqualSearchOperation();
	}

	@Test
	public void testIsApplicableString() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_equal", "test");
		Assert.assertTrue(doesNotEqualSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "equals", "test");
		Assert.assertFalse(doesNotEqualSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "string", "", "test");
		Assert.assertFalse(doesNotEqualSearchOperation.isApplicable(rule));
	}

	@Test
	public void testIsApplicableNumeric() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "does_not_equal", "67");
		Assert.assertTrue(doesNotEqualSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "equals", "32");
		Assert.assertFalse(doesNotEqualSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "", "123");
		Assert.assertFalse(doesNotEqualSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_equal",
				Collections.singletonList("test"));
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E$).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationLiteral() {
		Rule rule = SearchOperationUtils.createRule("field", "rdfs:Literal", "does_not_equal",
				Collections.singletonList("test"));
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E$).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationWithMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "string", "does_not_equal", Arrays.asList("test", "abv"));
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ FILTER ( NOT EXISTS  {  ?instance field VAR. } ) }";
		expected += " UNION { ?instance field VAR.  FILTER ( regex(lcase(str(VAR)), \"^(?!\\\\Qtest\\\\E$).*$\", \"i\")";
		expected += " && regex(lcase(str(VAR)), \"^(?!\\\\Qabv\\\\E$).*$\", \"i\")) }";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationNumeric() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "does_not_equal", "12");
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance field VAR.  FILTER ( VAR != 12 ) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationNumericMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "does_not_equal", Arrays.asList("21", "56"));
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationNumericEmptyValue() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "does_not_equal");
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationNumericEmptyValues() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "does_not_equal", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationWithUnknownType() {
		Rule rule = SearchOperationUtils.createRule("field", "datetime", "does_not_equal", "data");
		StringBuilder builder = new StringBuilder();

		doesNotEqualSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

}
