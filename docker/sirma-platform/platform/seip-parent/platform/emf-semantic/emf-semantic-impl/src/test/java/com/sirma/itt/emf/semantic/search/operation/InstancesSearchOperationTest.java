package com.sirma.itt.emf.semantic.search.operation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;

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

	/**
	 * Test that the operation builds the correct filter clause with more than 1000 instance ids. It should build VALUES
	 * clause instead of FILTER
	 */
	@Test
	public void testBuildOperationLargeSetOfInstances() {
		List<String> instanceIds = new LinkedList<>();
		for (int counter = 0; counter < 1001; counter++) {
			instanceIds.add("instance" + counter);
		}
		Rule rule = SearchOperationUtils.createRule("a", "object", "instanceIds", instanceIds);
		StringBuilder builder = new StringBuilder();

		operation.buildOperation(builder, rule);

		String query = builder.toString();
		Assert.assertTrue(query.contains(SPARQLQueryHelper.VALUES));
		for (int counter = 0; counter < 1001; counter++) {
			Assert.assertTrue(query.contains("instance" + counter));
		}

	}

}
