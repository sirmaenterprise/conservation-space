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
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * A helper for the embedded query operation testing.
 *
 * @author nvelkov
 */
public class QuerySearchOperationTestHelper {

	public static final String PATTERN_IS_DELETED = ".* emf:isDeleted \"false\"\\^\\^xsd:boolean";
	public static final String PATTERN_INSTANCE_TYPES = ".* emf:instanceType \\?.{33}_type  FILTER \\( \\?.{33}_type != \"sectionInstance\" && \\?.{33}_type != \"caseInstance\" \\) ";

	/**
	 * Check if the SPARQL query generated from the query operation matches the given pattern.
	 *
	 * @param operation
	 *            the operation that builds the query
	 * @param pattern
	 *            the pattern to match
	 * @param generatedSubquerySPARQL
	 *            the SPARQL query that is going to be generated for the embedded query
	 */
	public static boolean operationMatches(QuerySearchOperation operation, Pattern pattern,
			String generatedSubquerySPARQL) {
		StringBuilder builder = new StringBuilder();
		Rule rule = new Rule();
		rule.setField("anyRelation");
		rule.setValues(Arrays.asList("{\"key\":\"value\"}"));
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
		Condition condition = new Condition();
		condition.setCondition(Junction.AND);

		Rule rule = new Rule();
		rule.setField("anyRelation");
		rule.setValues(Arrays.asList("emf:Admin"));

		condition.setRules(Arrays.asList(rule));
		mockConverter(converter, condition);
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
