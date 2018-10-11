package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the query building in {@link LessThanSearchOperation}.
 * 
 * @author svetlozar.iliev
 */
public class LessThanSearchOperationTest {

	private LessThanSearchOperation isLesserSearchOperation;

	@Before
	public void initialize() {
		isLesserSearchOperation = new LessThanSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "less_than", "23");
		Assert.assertTrue(isLesserSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "numeric", "", "56");
		Assert.assertFalse(isLesserSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "less_than", "12");
		StringBuilder builder = new StringBuilder();

		isLesserSearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance field VAR.  FILTER ( VAR < 12 ) ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationEmptyValue() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "less_than");
		StringBuilder builder = new StringBuilder();

		isLesserSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationMultipleValues() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "less_than", Arrays.asList("12", "34"));
		StringBuilder builder = new StringBuilder();

		isLesserSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}

	@Test
	public void testBuildOperationEmptyValues() {
		Rule rule = SearchOperationUtils.createRule("field", "numeric", "less_than", Collections.emptyList());
		StringBuilder builder = new StringBuilder();

		isLesserSearchOperation.buildOperation(builder, rule);
		Assert.assertEquals("", builder.toString());
	}
}
