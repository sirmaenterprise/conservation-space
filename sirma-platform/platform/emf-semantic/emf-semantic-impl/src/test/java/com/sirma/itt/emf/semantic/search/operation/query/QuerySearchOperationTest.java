package com.sirma.itt.emf.semantic.search.operation.query;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.semantic.search.SearchOperationUtils;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

/**
 * Test the query search operation building.
 *
 * @author nvelkov
 */
public class QuerySearchOperationTest {

	@Mock
	private JsonToConditionConverter converter;

	// For the sake of testing the common logic we will use the set to query operation.
	@InjectMocks
	protected QuerySearchOperation operation = new SetToQuerySearchOperation();

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsApplicable() {
		QuerySearchOperationTestHelper.mockConverter(converter);

		Rule rule = SearchOperationUtils.createRule("field", "object", "set_to_query", "{\"id\":\"asd\"}");
		Assert.assertTrue(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", Collections.emptyList());
		Assert.assertFalse(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "object", "set_to_query", Arrays.asList("a", "b"));
		Assert.assertFalse(operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "object", "set_to_query",
				"{\"rules\":{\"values\":{\"id\":\"tooManyLevels\"}}}");
		QuerySearchOperationTestHelper.mockConverter(converter, createInvalidCondition());
		Assert.assertFalse(operation.isApplicable(rule));
	}

	private static Condition createInvalidCondition() {
		Condition condition = new Condition();
		condition.setCondition(Junction.AND);

		Condition subCondition = new Condition();
		subCondition.setCondition(Junction.AND);

		Rule rule = new Rule();
		rule.setField("anyRelation");
		rule.setValues(Arrays.asList("{\"id\":\"tooManyLevels\"}"));

		subCondition.setRules(Arrays.asList(rule));
		condition.setRules(Arrays.asList(subCondition));
		return condition;
	}
}
