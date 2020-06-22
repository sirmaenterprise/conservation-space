package com.sirma.itt.emf.semantic.search.operation.query;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonObject;

import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.search.SearchQueryBuilder;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_RELATION;

/**
 * A helper for the embedded query operation testing.
 *
 * @author nvelkov
 */
public class QuerySearchOperationTestHelper {

	public static final String PATTERN_IS_DELETED = ".* emf:isDeleted \"false\"\\^\\^xsd:boolean";
	public static final String PATTERN_INSTANCE_TYPES = ".* emf:instanceType \\?.{33}_type\\.  FILTER \\( \\?.{33}_type != \"sectionInstance\" && \\?.{33}_type != \"caseInstance\" \\) ";

	/**
	 * Check if the SPARQL query generated from the query operation matches the given pattern.
	 *
	 * @param operation
	 *            the operation that builds the query
	 * @param pattern
	 *            the pattern to match
	 */
	public static boolean operationMatches(QuerySearchOperation operation, Pattern pattern) {
		StringBuilder builder = new StringBuilder();
		Rule rule = SearchCriteriaBuilder.createRuleBuilder()
				.setField(ANY_RELATION)
				.setOperation("set_to")
				.setType("string")
				.setValues(Arrays.asList("{\"key\":\"value\"}"))
				.build();
		operation.buildOperation(builder, rule);

		Matcher matcher = pattern.matcher(builder.toString());
		return matcher.matches();
	}

	/**
	 * Mock the given search query builder to return the generated sparql. Used when generating the embedded query.
	 *
	 * @param searchQueryBuilder
	 *            the search query builder
	 * @param generatedSPARQL
	 *            the generated query
	 */
	public static void mockSearchQueryBuilder(SearchQueryBuilder searchQueryBuilder, String generatedSPARQL) {
		Mockito.when(searchQueryBuilder.build(Matchers.any(Condition.class))).thenReturn(generatedSPARQL);
	}

	/**
	 * Mock the json to condition converter so it returns an AND condition with a set rule.
	 *
	 * @param converter
	 *            the json to condition converter
	 */
	public static void mockConverter(JsonToConditionConverter converter) {
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder().setCondition(Junction.AND);

		Rule rule = SearchCriteriaBuilder.createRuleBuilder()
				.setField(ANY_RELATION)
				.setType("string")
				.setOperation("set_to")
				.setValues(Arrays.asList("emf:Admin"))
				.build();

		conditionBuilder.setRules(Arrays.asList(rule));
		mockConverter(converter, conditionBuilder.build());
	}

	public static void mockConverter(JsonToConditionConverter converter, Condition condition) {
		Mockito.when(converter.parseCondition(Matchers.any(JsonObject.class))).thenReturn(condition);
	}

	/**
	 * Mock the configurations.
	 *
	 * @param configurations
	 *            the configurations object to be mocked
	 * @param ignoredTypes
	 *            the ignored types, separated by comma
	 */
	public static void mockConfigurations(SemanticSearchConfigurations configurations, String ignoredTypes) {
		Mockito.when(configurations.getIgnoreInstanceTypes()).thenReturn(new ConfigurationPropertyMock<>(ignoredTypes));
	}
}
