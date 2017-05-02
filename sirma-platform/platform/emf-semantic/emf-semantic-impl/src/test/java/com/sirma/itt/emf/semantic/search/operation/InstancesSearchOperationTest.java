package com.sirma.itt.emf.semantic.search.operation;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Tests the query building in {@link InstancesSearchOperation}.
 *
 * @author nvelkov
 */
public class InstancesSearchOperationTest {

	private InstancesSearchOperation operation;

	/**
	 * Init the operation.
	 */
	@Before
	public void initialize() {
		operation = new InstancesSearchOperation();
	}

	/**
	 * Test the operations applicability.
	 */
	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("instanceId", "object", "in", "test");
		Assert.assertTrue(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "fts", "contains", "test");
		Assert.assertFalse(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("a", "string", null, new ArrayList<>());
		Assert.assertFalse(operation.isApplicable(rule));
	}

	/**
	 * Test that the operation builds the correct filter clause.
	 */
	@Test
	public void testBuildOperation() {
		Rule rule = SearchOperationUtils.createRule("a", "object", "instanceIds",
				Arrays.asList("test1", "test2", "test3"));
		StringBuilder builder = new StringBuilder();

		operation.buildOperation(builder, rule);

		String query = builder.toString();
		Assert.assertEquals(" FILTER ( ?instance = test1 || ?instance = test2 || ?instance = test3 ) ", query);
	}

}
