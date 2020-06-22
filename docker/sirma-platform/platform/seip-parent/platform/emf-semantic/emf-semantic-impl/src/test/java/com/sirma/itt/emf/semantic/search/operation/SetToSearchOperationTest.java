package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;
import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_RELATION;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;

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
		Rule rule = SearchOperationUtils.createRule(ANY_RELATION, "object", "set_to", Collections.singletonList("1"));
		Assert.assertTrue("The operation isn't applicable but it should be", operation.isApplicable(rule));
	}

	/**
	 * Test the build Operation method with a single values. The returned result should contain the instance uri.
	 */
	@Test
	public void testBuildOperation() {
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		rule.setValues(Collections.singletonList("emf:uri1"));
		operation.buildOperation(builder, rule.build());
		Assert.assertEquals("The returned result didn't contain a triple with the instance uri",
				" ?instance emf:hasChild emf:uri1. ", builder.toString());
	}

	/**
	 * Test the build Operation method with a any relation value. The returned result should contain the instance uri
	 * and a randomly generated variable name as the predicate.
	 */
	@Test
	public void testBuildOperationAnyRelation() {
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField(ANY_RELATION);
		rule.setOperation("set_to");
		rule.setValues(Collections.singletonList("emf:uri1"));
		operation.buildOperation(builder, rule.build());

		Pattern pattern = Pattern.compile(" \\?instance .* emf:uri1. ");
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
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		rule.setValues(Arrays.asList("emf:uri1", "emf:uri2"));
		operation.buildOperation(builder, rule.build());
		Assert.assertEquals("The returned result didn't contain two triples with the instance uris",
				"{ ?instance emf:hasChild emf:uri1. } UNION { ?instance emf:hasChild emf:uri2. }", builder.toString());
	}

	/**
	 * Test the build Operation method with an any object.
	 */
	@Test
	public void testBuildOperationAnyObject() {
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		rule.setValues(Collections.singletonList(ANY_OBJECT));
		operation.buildOperation(builder, rule.build());

		Pattern pattern = Pattern.compile(" \\?instance emf:hasChild .* ");
		Matcher matcher = pattern.matcher(builder.toString());
		Assert.assertTrue("The returned result didn't match the expression ", matcher.matches());
	}

	/**
	 * Test the build Operation method with more than 1000 values. The returned result should contain VALUES block with
	 * all instances from the list
	 */
	@Test
	public void testBuildOperationLargeValueList() {
		int instancesCount = 1001;
		
		StringBuilder builder = new StringBuilder();
		RuleBuilder rule = SearchCriteriaBuilder.createRuleBuilder();
		rule.setField("emf:hasChild");
		rule.setOperation("set_to");
		List<String> instanceIds = new LinkedList<>();
		for (int counter = 0; counter < instancesCount; counter++) {
			instanceIds.add("instance" + counter);
		}
		rule.setValues(instanceIds);
		operation.buildOperation(builder, rule.build());
		String query = builder.toString();
		Assert.assertTrue(query.contains(SPARQLQueryHelper.VALUES));
		for (int counter = 0; counter < instancesCount; counter++) {
			Assert.assertTrue(query.contains("instance" + counter));
		}
	}
}
