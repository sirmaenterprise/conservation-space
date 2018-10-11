package com.sirma.itt.emf.semantic.search.operation.inversed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sirma.itt.emf.semantic.search.operation.inverse.IsNotSearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link IsNotSearchOperation}.
 *
 * @author smustafov
 */
public class IsNotSearchOperationTest {

	private IsNotSearchOperation isNotSearchOperation;

	@Before
	public void initialize() {
		isNotSearchOperation = new IsNotSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not", "smt");
		assertTrue(isNotSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("booleanField", "boolean", "is", "smt");
		assertFalse(isNotSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("stringField", "string", "endsWith", "smt");
		assertFalse(isNotSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not",
				Arrays.asList("true", "not specified"));
		assertFalse(isNotSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not", new ArrayList<>());
		assertFalse(isNotSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not");
		assertFalse(isNotSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation_false() {
		StringBuilder builder = new StringBuilder();
		Rule rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not",
				Collections.singletonList("false"));

		isNotSearchOperation.buildOperation(builder, rule);
		String expected = " ?instance booleanField \"true\"^^xsd:boolean. ";
		assertEquals(expected, builder.toString());
	}

	@Test
	public void testBuildOperation_true() {
		StringBuilder builder = new StringBuilder();
		Rule rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not",
				Collections.singletonList("true"));

		isNotSearchOperation.buildOperation(builder, rule);
		String expected = " ?instance booleanField \"false\"^^xsd:boolean. ";
		assertEquals(expected, builder.toString());
	}

}
