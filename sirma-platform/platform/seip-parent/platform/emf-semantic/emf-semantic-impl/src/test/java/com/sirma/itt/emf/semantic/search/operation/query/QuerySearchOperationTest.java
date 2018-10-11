package com.sirma.itt.emf.semantic.search.operation.query;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test the query search operation building.
 *
 * @author nvelkov
 */
public class QuerySearchOperationTest {

	@Spy
	private JsonToConditionConverter converter = new JsonToConditionConverter();

	// For the sake of testing the common logic we will use the set to query operation.
	@InjectMocks
	private QuerySearchOperation operation = new SetToQuerySearchOperation();

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsApplicable() {

		Rule rule = SearchOperationUtils.createRule("field", "object", "set_to_query", Collections.emptyList());
		Assert.assertFalse(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", Arrays.asList("a", "b"));
		Assert.assertFalse(operation.isApplicable(rule));

		// Two query levels
		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", "{\"id\":\"asd\"}");
		Assert.assertTrue(operation.isApplicable(rule));

		// Three query levels
		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", loadFile("one_embedded_query.json"));
		Assert.assertTrue(operation.isApplicable(rule));

		// Four query levels - should not be allowed
		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", loadFile("two_embedded_queries.json"));
		Assert.assertFalse(operation.isApplicable(rule));
	}

	private String loadFile(String fileName) {
		try {
			return IOUtils.toString(getClass().getResource(fileName));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
