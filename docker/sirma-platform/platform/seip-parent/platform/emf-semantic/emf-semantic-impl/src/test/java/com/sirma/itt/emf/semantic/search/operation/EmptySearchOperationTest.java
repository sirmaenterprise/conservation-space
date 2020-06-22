package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * Tests the query building in {@link EmptySearchOperation} for properties that have a value or not.
 *
 * @author Mihail Radkov
 */
public class EmptySearchOperationTest {

	private EmptySearchOperation emptySearchOperation;

	@Before
	public void initialize() {
		emptySearchOperation = new EmptySearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("property", "string", "empty", Collections.emptyList());
		Assert.assertTrue(emptySearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("property", "string", "contains", "test");
		Assert.assertFalse(emptySearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperationForIsEmpty() {
		Rule rule = SearchOperationUtils.createRule("property", "string", "empty", "true");
		StringBuilder builder = new StringBuilder();

		emptySearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " FILTER NOT EXISTS  {  ?instance property VAR.  } ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationForIsNotEmpty() {
		Rule rule = SearchOperationUtils.createRule("property", "string", "not_empty", "false");
		StringBuilder builder = new StringBuilder();

		emptySearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance property VAR. ";
		Assert.assertEquals(expected, query);
	}

	@Test
	public void testBuildOperationForEmptyValue() {
		Rule rule = SearchOperationUtils.createRule("property", "string", "not_empty", "");
		StringBuilder builder = new StringBuilder();

		emptySearchOperation.buildOperation(builder, rule);

		String query = builder.toString().replaceAll("\\?v.{32}", "VAR");
		String expected = " ?instance property VAR. ";
		Assert.assertEquals(expected, query);
	}

}
