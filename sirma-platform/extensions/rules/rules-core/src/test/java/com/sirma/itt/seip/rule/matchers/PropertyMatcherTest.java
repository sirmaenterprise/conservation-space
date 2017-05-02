package com.sirma.itt.seip.rule.matchers;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.rule.BaseRuleTest;

/**
 * The Class PropertyMatcherTest.
 *
 * @author hlungov
 */
@Test
public class PropertyMatcherTest extends BaseRuleTest {

	/** The instance service. */
	@Mock
	private InstanceService instanceService;

	@Mock
	private ExpressionsManager expressionsManager;

	/** The property matcher. */
	@InjectMocks
	private PropertyMatcher propertyMatcher;

	/**
	 * Match equals test_no properties.
	 */
	public void matchEqualsTest_noProperties() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_matchingProperty() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_invertMatching() {
		propertyMatcher.configure(
				buildDefaultConfiguration(Arrays.asList("title1"), Arrays.asList("title2"), true, true, -1, true));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title1", "test, tessss");
		objectInstance.getProperties().put("title2", "test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void notMatchEqualsTest_matchingPropertyElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		propertyMatcher.configure(
				buildDefaultConfiguration(Arrays.asList(EXPRESSION_NAME), Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("testNotMatched");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_matchingPropertyElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		propertyMatcher.configure(
				buildDefaultConfiguration(Arrays.asList(EXPRESSION_NAME), Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void notMatchEqualsTest_matchingSearchInPropertiesElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		propertyMatcher.configure(
				buildDefaultConfiguration(Arrays.asList("title"), Arrays.asList(EXPRESSION_NAME), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("testNotMatched");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_SearchInPropertiesElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		propertyMatcher.configure(
				buildDefaultConfiguration(Arrays.asList("title"), Arrays.asList(EXPRESSION_NAME), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_SearchInPropertiesElExpressionLastMatch() {
		String EXPRESSION_NAME = "${get([name])}";
		String EXPRESSION_TITLE = "${get([title])}";

		List<String> searchInProperties = new LinkedList<String>();
		searchInProperties.add(EXPRESSION_TITLE);
		searchInProperties.add(EXPRESSION_NAME);

		propertyMatcher
				.configure(buildDefaultConfiguration(Arrays.asList("title"), searchInProperties, true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_TITLE), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("differentTitle");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		Mockito.when(expressionsManager.isExpression(EXPRESSION_TITLE)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void notMatchEqualsTest_SearchInPropertiesElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		String EXPRESSION_TITLE = "${get([title])}";

		List<String> searchInProperties = new LinkedList<String>();
		searchInProperties.add(EXPRESSION_TITLE);
		searchInProperties.add(EXPRESSION_NAME);

		propertyMatcher
				.configure(buildDefaultConfiguration(Arrays.asList("title"), searchInProperties, true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "test");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("differentName");
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_TITLE), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("differentTitle");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		Mockito.when(expressionsManager.isExpression(EXPRESSION_TITLE)).thenReturn(true);
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void matchEqualsTest_ElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		String EXPRESSION_TITLE = "${get([title])}";
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList(EXPRESSION_TITLE),
				Arrays.asList(EXPRESSION_NAME), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "testtitle");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);

		ExpressionContext instanceToMatchxpressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(documentInstance, null, null)).thenReturn(
				instanceToMatchxpressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_TITLE), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_TITLE)).thenReturn(true);

		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_matching property.
	 */
	public void notMatchEqualsTest_ElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		String EXPRESSION_TITLE = "${get([title])}";
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList(EXPRESSION_TITLE),
				Arrays.asList(EXPRESSION_NAME), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		objectInstance.getProperties().put("title", "test");
		documentInstance.getProperties().put("title", "testtitle");

		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(objectInstance, null, null)).thenReturn(expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_NAME), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);

		ExpressionContext instanceToMatchxpressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(documentInstance, null, null)).thenReturn(
				instanceToMatchxpressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(Matchers.eq(EXPRESSION_TITLE), Matchers.eq(Serializable.class),
						Matchers.any(ExpressionContext.class)))
					.thenReturn("testtitle");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_TITLE)).thenReturn(true);

		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_not matching properties.
	 */
	public void matchEqualsTest_notMatchingProperties() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test2");
		objectInstance.getProperties().put("title", "test3");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_different properties.
	 */
	public void matchEqualsTest_differentProperties() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("name", "name");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_ignore case_false.
	 */
	public void matchEqualsTest_ignoreCase_false() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), false, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("title", "Test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_ignore case_true.
	 */
	public void matchEqualsTest_ignoreCase_true() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("title", "Test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_minimal length_under minimum.
	 */
	public void matchEqualsTest_minimalLength_underMinimum() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, 5, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("title", "Test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_minimal length_pass the minimum.
	 */
	public void matchEqualsTest_minimalLength_passTheMinimum() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), true, false, 4, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("title", "Test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_contains_true.
	 */
	public void matchEqualsTest_contains_true() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), false, true, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test");
		objectInstance.getProperties().put("title", "test value");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_exact.
	 */
	public void matchEqualsTest_contains_false() {
		propertyMatcher.configure(buildDefaultConfiguration(Arrays.asList("title"), false, false, -1, false));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test value");
		objectInstance.getProperties().put("title", "test");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Match equals test_exact.
	 */
	public void matchEqualsTest_exact_false() {
		Context<String, Object> context = buildDefaultConfiguration(Arrays.asList("title"), false, false, -1, false);
		context.put(RuleMatcher.EXACT_MATCH, false);

		propertyMatcher.configure(context);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test value");
		objectInstance.getProperties().put("title", "test - value");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), true);
	}

	/**
	 * Match equals test_exact_true.
	 */
	public void matchEqualsTest_exact_true() {
		Context<String, Object> context = buildDefaultConfiguration(Arrays.asList("title"), false, false, -1, false);
		context.put(RuleMatcher.EXACT_MATCH, true);

		propertyMatcher.configure(context);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);

		documentInstance.getProperties().put("title", "test - value");
		objectInstance.getProperties().put("title", "test value");
		assertEquals(propertyMatcher.match(buildRuleContext, objectInstance, null), false);
	}

	/**
	 * Builds the default configuration.
	 *
	 * @param props
	 *            the props
	 * @param ignoreCase
	 *            the ignore case
	 * @param contains
	 *            the contains
	 * @param minimalLength
	 *            the minimal length
	 * @param invertMatching
	 * @return the context
	 */
	private Context<String, Object> buildDefaultConfiguration(List<String> props, boolean ignoreCase, boolean contains,
			int minimalLength, boolean invertMatching) {
		return buildDefaultConfiguration(props, props, ignoreCase, contains, minimalLength, invertMatching);
	}

	/**
	 * Builds the default configuration.
	 *
	 * @param ignoreCase
	 *            the ignore case
	 * @param contains
	 *            the contains
	 * @param minimalLength
	 *            the minimal length
	 * @param invertMatching
	 * @param props
	 *            the props
	 * @return the context
	 */
	private Context<String, Object> buildDefaultConfiguration(List<String> checkForProperties,
			List<String> searchInProperties, boolean ignoreCase, boolean contains, int minimalLength, boolean invertMatching) {
		configuration.clear();
		configuration.put(RuleMatcher.CHECK_FOR_PROPERTIES, checkForProperties);
		configuration.put(RuleMatcher.SEARCH_IN_PROPERTIES, searchInProperties);
		configuration.put(RuleMatcher.CONTAINS, contains);
		configuration.put(RuleMatcher.IGNORE_CASE, ignoreCase);
		configuration.put(RuleMatcher.MINIMAL_LENGTH, minimalLength);
		configuration.put(RuleMatcher.INVERT_MATCHING, invertMatching);
		return configuration;
	}
}
