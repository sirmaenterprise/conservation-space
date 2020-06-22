package com.sirma.itt.emf.semantic.search.operation.query;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.regex.Pattern;

/**
 * Test the set to query search operation building.
 *
 * @author nvelkov
 */
public class SetToQuerySearchOperationTest {

	@Mock
	private SearchQueryBuilder searchQueryBuilder;

	@Mock
	private JsonToConditionConverter converter;

	@Mock
	private SemanticSearchConfigurations configurations;

	@InjectMocks
	private SetToQuerySearchOperation operation;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the is applicable method. The rule should be applicable if the operation is "set_to_query".
	 */
	@Test
	public void testIsApplicable() {
		QuerySearchOperationTestHelper.mockConverter(converter);

		Rule rule = SearchOperationUtils.createRule("field", "object", "set_to_query", "{\"id\":\"asd\"}");
		Assert.assertTrue("The operation isn't applicable but it should be", operation.isApplicable(rule));

		rule = SearchOperationUtils.createRule("field", "object", "set_to", "{\"id\":\"asd\"}");
		Assert.assertFalse("The operation is applicable but it shouldn't be", operation.isApplicable(rule));
	}

	/**
	 * Test the build Operation method. The returned result should contain the generated query with a generated embedded
	 * query with the instance variable replaced.
	 */
	@Test
	public void testBuildOperation() {
		QuerySearchOperationTestHelper.mockConverter(converter);
		QuerySearchOperationTestHelper.mockConfigurations(configurations, "sectionInstance, caseInstance");
		QuerySearchOperationTestHelper.mockSearchQueryBuilder(searchQueryBuilder,"?instance emf:hasRelation emf:Admin.");

		Pattern pattern = Pattern
				.compile(" \\?instance emf:hasRelation (?!\\?instance).*\\. "
						+ QuerySearchOperationTestHelper.PATTERN_IS_DELETED + " \\.  "
						+ QuerySearchOperationTestHelper.PATTERN_INSTANCE_TYPES + System.lineSeparator()
						+ "(?!\\?instance).* emf:hasRelation emf:Admin\\.");
		Assert.assertTrue("The returned result didn't match the expression ",
						  QuerySearchOperationTestHelper.operationMatches(operation, pattern));
	}
}
