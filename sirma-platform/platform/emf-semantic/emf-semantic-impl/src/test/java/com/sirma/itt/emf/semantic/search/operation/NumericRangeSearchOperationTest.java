package com.sirma.itt.emf.semantic.search.operation;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Tests the query building in {@link NumericRangeSearchOperation}.
 *
 * @author svetlozar.iliev
 */
public class NumericRangeSearchOperationTest {

	private NumericRangeSearchOperation numericRangeSearchOperation;

	@Before
	public void initialize() {
		numericRangeSearchOperation = new NumericRangeSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "between", Arrays.asList("23", "101"));
		Assert.assertTrue(numericRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "", Arrays.asList("23", "101"));
		Assert.assertFalse(numericRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", null, Arrays.asList("23", "101"));
		Assert.assertFalse(numericRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "is_lesser");
		Assert.assertFalse(numericRangeSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "is_lesser", "15");
		Assert.assertFalse(numericRangeSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "between", Arrays.asList("23", "101"));
		StringBuilder builder = new StringBuilder();

		numericRangeSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = "{ ?instance field VAR  FILTER ( VAR > 23 )  FILTER ( VAR < 101 ) }";
		Assert.assertEquals(expected, query);
	}

}
