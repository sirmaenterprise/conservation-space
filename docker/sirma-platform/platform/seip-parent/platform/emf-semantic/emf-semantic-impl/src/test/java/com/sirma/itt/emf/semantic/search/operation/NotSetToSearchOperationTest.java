package com.sirma.itt.emf.semantic.search.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.seip.domain.search.tree.CriteriaWildcards;
import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;

/**
 * Test the not set to operation.
 *
 * @author nvelkov
 */
public class NotSetToSearchOperationTest {
	/**
	 * The expression that should be matched from the generated not set operator.
	 */
	private static String EXPRESSION = "( FILTER \\( NOT EXISTS  \\{  \\?instance emf\\:hasChild emf\\:uri.\\. \\} \\) )*";

	private NotSetToSearchOperation operation = new NotSetToSearchOperation();

	/**
	 * Test the is applicable method. The rule should be applicable if the operation is "not_set_to".
	 */
	@Test
	public void testIsApplicable() {
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setOperation("not_set_to");
		rule.setField(CriteriaWildcards.ANY_RELATION);
		Assert.assertTrue("The operation isn't applicable but it should be", operation.isApplicable(rule.build()));
	}

	/**
	 * Test the build Operation method. The returned result should match the filter not bound expression.
	 */
	@Test
	public void testBuildOperation() {
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		rule.setValues(Arrays.asList("emf:uri1"));
		operation.buildOperation(builder, rule.build());
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
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		rule.setValues(Collections.emptyList());
		operation.buildOperation(builder, rule.build());
		Assert.assertEquals("The generated query should be empty but it wasn't", "", builder.toString());
	}

	/**
	 * Test the build Operation method with multiple values. The returned result should match the filter not bound
	 * expression.
	 */
	@Test
	public void testBuildOperationMultipleValues() {
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("equals");
		rule.setValues(Arrays.asList("emf:uri1", "emf:uri2", "emf:uri3"));
		operation.buildOperation(builder, rule.build());
		Pattern pattern = Pattern.compile(EXPRESSION);
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}
}
