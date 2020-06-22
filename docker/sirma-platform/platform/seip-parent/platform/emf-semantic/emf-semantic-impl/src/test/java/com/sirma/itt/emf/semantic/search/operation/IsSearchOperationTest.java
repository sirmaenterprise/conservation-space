package com.sirma.itt.emf.semantic.search.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link IsSearchOperation}.
 *
 * @author smustafov
 */
public class IsSearchOperationTest {

	private IsSearchOperation isSearchOperation;

	@Before
	public void initialize() {
		isSearchOperation = new IsSearchOperation();
	}

	@Test
	public void testIsApplicable() {
		Rule rule = SearchOperationUtils.createRule("booleanField", "boolean", "is", "value");
		assertTrue(isSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("booleanField", "boolean", "is_not", "value");
		assertFalse(isSearchOperation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("stringField", "string", "equals", "value");
		assertFalse(isSearchOperation.isApplicable(rule));

		// multiple values
		rule = SearchOperationUtils.createRule("stringField", "string", "is", Arrays.asList("value", "value"));
		assertFalse(isSearchOperation.isApplicable(rule));

		// empty value
		rule = SearchOperationUtils.createRule("stringField", "string", "is", Collections.emptyList());
		assertFalse(isSearchOperation.isApplicable(rule));

		// null value
		rule = SearchOperationUtils.createRule("stringField", "string", "is");
		assertFalse(isSearchOperation.isApplicable(rule));
	}

	@Test
	public void testBuildOperation() {
		StringBuilder builder = new StringBuilder();
		Rule rule = SearchOperationUtils.createRule("booleanField", "boolean", "is", Collections.singletonList("true"));

		isSearchOperation.buildOperation(builder, rule);
		String expected = " ?instance booleanField \"true\"^^xsd:boolean. ";
		assertEquals(expected, builder.toString());
	}

}
