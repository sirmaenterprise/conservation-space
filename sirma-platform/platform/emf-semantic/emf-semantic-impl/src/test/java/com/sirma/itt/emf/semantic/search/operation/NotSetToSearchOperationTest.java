package com.sirma.itt.emf.semantic.search.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Test the not set to operation.
 *
 * @author nvelkov
 */
public class NotSetToSearchOperationTest {
	/**
	 * The expression that should be matched from the generated not set operator.
	 */
	private static String EXPRESSION = "(\\{ FILTER \\( NOT EXISTS  \\{  \\?instance emf\\:hasChild emf\\:uri.  \\. \\} \\) \\})*";

	private NotSetToSearchOperation operation = new NotSetToSearchOperation();

	/**
	 * Test the is applicable method. The rule should be applicable if the operation is "not_set_to".
	 */
	@Test
	public void testIsApplicable() {
		Rule rule = new Rule();
		rule.setOperation("not_set_to");
		Assert.assertTrue("The operation isn't applicable but it should be", operation.isApplicable(rule));
	}

	/**
	 * Test the build Operation method. The returned result should match the filter not bound expression.
	 */
	@Test
	public void testBuildOperation() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Arrays.asList("emf:uri1"));
		operation.buildOperation(builder, rule);
		Pattern pattern = Pattern.compile(EXPRESSION);
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}

	/**
	 * Test the build Operation method with an empty value. The returned result should contain the two instance uris
	 * contained in MINUS conditions.
	 */
	@Test
	public void testBuildOperationEmptyValues() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Collections.emptyList());
		operation.buildOperation(builder, rule);
		Assert.assertEquals("The generated query should be empty but it wasn't", "", builder.toString());
	}

	/**
	 * Test the build Operation method with multiple values. The returned result should match the filter not bound
	 * expression.
	 */
	@Test
	public void testBuildOperationMultipleValues() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Arrays.asList("emf:uri1", "emf:uri2", "emf:uri3"));
		operation.buildOperation(builder, rule);
		Pattern pattern = Pattern.compile(EXPRESSION);
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}
}
