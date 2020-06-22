package com.sirma.itt.seip.rule.matchers;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.rule.BaseRuleTest;
import com.sirma.itt.seip.rule.matchers.ComplexMatcher;
import com.sirma.itt.seip.rule.model.EntityRecognitionConfigBuilder;

/**
 * The Class ComplexMatcherTest.
 *
 * @author Hristo Lungov
 */
public class ComplexMatcherTest extends BaseRuleTest {

	@Mock
	private RuleMatcher propertyMatcher;

	@Mock
	private RuleMatcher patternMatcher;

	@Mock
	private EntityRecognitionConfigBuilder entityRecognitionConfigBuilder;

	@InjectMocks
	private ComplexMatcher complexMatcher;

	/**
	 * Configuration is incorrect no submatchers
	 */
	@Test
	public void incorrect_config_test() {
		complexMatcher.configure(buildDefaultConfiguration(Boolean.FALSE));
		Assert.assertFalse(
				complexMatcher.isApplicable(buildRuleContext(documentInstance, previousVerDocInstance, null)));
	}

	/**
	 * Configuration is correct, have two submatchers.
	 */
	@Test
	public void correct_config_test() {
		complexMatcher.configure(buildDefaultConfiguration(Boolean.TRUE));
		Assert.assertTrue(
				complexMatcher.isApplicable(buildRuleContext(documentInstance, previousVerDocInstance, null)));
	}

	/**
	 * Configuration is correct and two submatchers return true
	 */
	@Test
	public void submatchers_true_test() {
		complexMatcher.configure(buildDefaultConfiguration(Boolean.TRUE));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Mockito.when(propertyMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.TRUE);
		Mockito.when(patternMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.TRUE);
		Assert.assertTrue(complexMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Configuration is correct and only submatchers return true
	 */
	@Test
	public void one_of_submatchers_true_test() {
		complexMatcher.configure(buildDefaultConfiguration(Boolean.TRUE));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Mockito.when(propertyMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.TRUE);
		Mockito.when(patternMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.FALSE);
		Assert.assertFalse(complexMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Configuration is correct and two submatchers return false
	 */
	@Test
	public void both_of_submatchers_false_test() {
		complexMatcher.configure(buildDefaultConfiguration(Boolean.TRUE));
		Context<String, Object> buildRuleContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		Mockito.when(propertyMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.FALSE);
		Mockito.when(patternMatcher.match(buildRuleContext, objectInstance, null)).thenReturn(Boolean.FALSE);
		Assert.assertFalse(complexMatcher.match(buildRuleContext, objectInstance, null));
	}

	/**
	 * Builds the default configuration.
	 *
	 * @param withMatchers
	 *            the with matchers
	 * @return the context
	 */
	private Context<String, Object> buildDefaultConfiguration(boolean withMatchers) {
		configuration.clear();
		configuration.put(ComplexMatcher.MATCHERS, Arrays.asList(propertyMatcher, patternMatcher));
		if (withMatchers) {
			Mockito
					.doReturn(Arrays.asList(propertyMatcher, patternMatcher))
						.when(entityRecognitionConfigBuilder)
						.buildSubElements(ComplexMatcher.MATCHERS, RuleMatcher.class, configuration);
		}
		return configuration;
	}

}
