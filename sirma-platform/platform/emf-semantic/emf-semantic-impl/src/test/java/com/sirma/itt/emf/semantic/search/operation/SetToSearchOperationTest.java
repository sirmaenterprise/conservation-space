package com.sirma.itt.emf.semantic.search.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Test the set to operation.
 *
 * @author nvelkov
 */
public class SetToSearchOperationTest {

	private SetToSearchOperation operation = new SetToSearchOperation();

	/**
	 * Test the is applicable method. The rule should be applicable if the operation is "set_to".
	 */
	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("anyRelation", "object", "set_to", Collections.singletonList("1"));
		Assert.assertTrue("The operation isn't applicable but it should be", operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("anyRelation", "object", "set_to", Collections.emptyList());
		Assert.assertFalse("The operation isn't applicable but it should be", operation.isApplicable(rule));
	}

	/**
	 * Test the build Operation method with a single values. The returned result should contain the instance uri.
	 */
	@Test
	public void testBuildOperation() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Collections.singletonList("emf:uri1"));
		operation.buildOperation(builder, rule);
		Assert.assertEquals("The returned result didn't contain a triple with the instance uri",
				"{ ?instance emf:hasChild emf:uri1 }", builder.toString());
	}

	/**
	 * Test the build Operation method with a any relation value. The returned result should contain the instance uri
	 * and a randomly generated variable name as the predicate.
	 */
	@Test
	public void testBuildOperationAnyRelation() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("anyRelation");
		rule.setValues(Collections.singletonList("emf:uri1"));
		operation.buildOperation(builder, rule);

		Pattern pattern = Pattern.compile("\\{ \\?instance .* emf:uri1 \\}");
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}

	/**
	 * Test the build Operation method with multiple values. The returned result should contain the two instance uris
	 * with a union between them.
	 */
	@Test
	public void testBuildOperationMultipleValues() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Arrays.asList("emf:uri1", "emf:uri2"));
		operation.buildOperation(builder, rule);
		Assert.assertEquals("The returned result didn't contain two triples with the instance uris",
				"{ ?instance emf:hasChild emf:uri1 } UNION { ?instance emf:hasChild emf:uri2 }", builder.toString());
	}

	/**
	 * Test the build Operation method with an any object.
	 */
	@Test
	public void testBuildOperationAnyObject() {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("emf:hasChild");
		rule.setValues(Collections.singletonList("anyObject"));
		operation.buildOperation(builder, rule);

		Pattern pattern = Pattern.compile("\\{ \\?instance emf:hasChild .* \\}");
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}
}
