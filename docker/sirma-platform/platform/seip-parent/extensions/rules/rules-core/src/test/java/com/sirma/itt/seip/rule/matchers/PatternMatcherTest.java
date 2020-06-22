package com.sirma.itt.seip.rule.matchers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.rule.BaseRuleTest;
import com.sirma.itt.seip.rule.matchers.PatternMatcher;

/**
 * The Class PatternMatcherTest.
 *
 * @author Hristo Lungov
 */
public class PatternMatcherTest extends BaseRuleTest {

	private static final String NAME = "name";

	@Mock
	private TypeConverter typeConverter;

	@InjectMocks
	private PatternMatcher patternMatcher;

	/**
	 * Test configure method with valid context.
	 */
	@Test
	public void test_correct_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}[A-Z]$|^\\d{6}[A-Z]{2}$", "^\\d{6}$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test configure method with valid context only for processing instance.
	 */
	@Test
	public void test_only_currentInstance_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME), null,
				"^\\d{6}[A-Z]$|^\\d{6}[A-Z]{2}$", null, true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test configure method with valid context only for found instance.
	 */
	@Test
	public void test_only_foundInstance_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(null, Arrays.asList(NAME), null,
				"^\\d{6}[A-Z]$|^\\d{6}[A-Z]{2}$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test configure method with invalid context where no regex passed.
	 */
	@Test
	public void test_incorrect_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), null, null, true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test configure method with invalid context where wrong regex passed.
	 */
	@Test
	public void test_incorrect_regex_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{asdad", "\\d{asdaddas", true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test configure method with invalid context where nothing is passed.
	 */
	@Test
	public void test_empty_Config() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(null, null, null, null, true);
		patternMatcher.configure(buildDefaultConfiguration);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.isApplicable(buildRuleContext));
	}

	/**
	 * Test match method with correct context and all data is correct.
	 */
	@Test
	public void test_correct_both_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, "011145A");
		documentInstance.getProperties().put(NAME, "011145");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method with correct context and instance's properties are collections with strings.
	 */
	@Test
	public void test_correct_both_collection_with_strings_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, (Serializable) Arrays.asList("011145A"));
		documentInstance.getProperties().put(NAME, (Serializable) Arrays.asList("011145"));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method with correct context and instance's properties are collections with objects.
	 */
	@Test
	public void test_both_collection_objects_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		List<Object> objs = new ArrayList<Object>(1);
		StringBuilder obj = new StringBuilder("011145A");
		objs.add(obj);
		objectInstance.getProperties().put(NAME, (Serializable) objs);
		List<Object> docs = new ArrayList<Object>(1);
		StringBuilder doc = new StringBuilder("011145");
		docs.add(doc);
		documentInstance.getProperties().put(NAME, (Serializable) docs);
		Mockito.when(typeConverter.tryConvert(String.class, doc)).thenReturn("011145").thenReturn(null);
		Mockito.when(typeConverter.tryConvert(String.class, obj)).thenReturn("011145A").thenReturn(null);
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.match(buildRuleContext, objectInstance, null));
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where properties won't match against regex.
	 */
	@Test
	public void test_incorrect_both_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, "011145A.pdf");
		documentInstance.getProperties().put(NAME, "011145.pdf");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where only properties of processed instance won't match against regex.
	 */
	@Test
	public void test_processed_of_both_incorrect_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, "011145A");
		documentInstance.getProperties().put(NAME, "011145.pdf");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where only properties of found instance won't match against regex.
	 */
	@Test
	public void test_found_of_both_incorrect_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME),
				Arrays.asList(NAME), "^\\d{6}$", "^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		documentInstance.getProperties().put(NAME, "011145");
		objectInstance.getProperties().put(NAME, "011145A.pdf");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where properties of processed instance will match against regex.
	 */
	@Test
	public void test_correct_processed_only_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME), null,
				"^\\d{6}$", null, true);
		patternMatcher.configure(buildDefaultConfiguration);
		documentInstance.getProperties().put(NAME, "011145");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where properties of processed instance won't match against regex.
	 */
	@Test
	public void test_incorrect_processed_only_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(Arrays.asList(NAME), null,
				"^\\d{6}$", null, true);
		patternMatcher.configure(buildDefaultConfiguration);
		documentInstance.getProperties().put(NAME, "011145.pdf");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where properties of found instance will match against regex.
	 */
	@Test
	public void test_correct_found_only_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(null, Arrays.asList(NAME), null,
				"^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, "011145A");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertTrue(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Test match method where properties of found instance won't match against regex.
	 */
	@Test
	public void test_incorrect_found_only_Match() {
		Context<String, Object> buildDefaultConfiguration = buildDefaultConfiguration(null, Arrays.asList(NAME), null,
				"^\\d{6}[A-Z]$", true);
		patternMatcher.configure(buildDefaultConfiguration);
		objectInstance.getProperties().put(NAME, "011145A.pdf");
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Assert.assertFalse(patternMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Builds the test configuration.
	 *
	 * @param currentInstanceProps
	 *            the current instance props
	 * @param foundInstanceProps
	 *            the found instance props
	 * @param currentInstanceRegex
	 *            the current instance regex
	 * @param foundInstanceRegex
	 *            the found instance regex
	 * @param ignoreCase
	 *            the ignore case
	 * @return the context
	 */
	private Context<String, Object> buildDefaultConfiguration(List<String> currentInstanceProps,
			List<String> foundInstanceProps, String currentInstanceRegex, String foundInstanceRegex,
			boolean ignoreCase) {
		configuration.clear();
		if (CollectionUtils.isNotEmpty(currentInstanceProps)) {
			configuration.put(PatternMatcher.CURRENT_INSTANCE_PROPS, new ArrayList<>(currentInstanceProps));
		}
		if (CollectionUtils.isNotEmpty(foundInstanceProps)) {
			configuration.put(PatternMatcher.FOUND_INSTANCE_PROPS, new ArrayList<>(foundInstanceProps));
		}
		if (StringUtils.isNotBlank(currentInstanceRegex)) {
			configuration.put(PatternMatcher.CURRENT_INSTANCE_REGEX, currentInstanceRegex);
		}
		if (StringUtils.isNotBlank(foundInstanceRegex)) {
			configuration.put(PatternMatcher.FOUND_INSTANCE_REGEX, foundInstanceRegex);
		}
		configuration.put(RuleMatcher.IGNORE_CASE, ignoreCase);
		return configuration;
	}

}
