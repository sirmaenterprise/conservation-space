/*
 *
 */
package com.sirma.itt.seip.rule.matchers;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.rule.util.ContentLoader;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class ContentMatcherTest.
 *
 * @author BBonev
 */
@Test
public class ContentMatcherTest extends EmfTest {

	@Mock
	private ExpressionsManager expressionsManager;

	@Mock
	private ContentLoader contentLoader;

	/** The content matcher. */
	@InjectMocks
	private ContentMatcher contentMatcher;

	/**
	 * Before method.
	 */
	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		Mockito.when(contentLoader.loadContent(Matchers.any(Instance.class))).then(invocation -> {
			Instance instance = (Instance) invocation.getArguments()[0];
			return instance.getProperties().get(DefaultProperties.CONTENT);
		});
	}

	/**
	 * Test normal matching.
	 */
	public void testNormalMatching() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT, "some test content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap("name", "test"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Test normal matching_ignore case.
	 */
	public void testNormalMatching_ignoreCase() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		configuration.put("ignoreCase", Boolean.TRUE);
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT, "some TEST content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap("name", "teSt"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Test escaped matching.
	 */
	public void testEscapedMatching() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT,
				"some (&Aacute;llatgy&oacute;gy&aacute;szati prescription) content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap("name", "Állatgyógyászati"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Word matching.
	 */
	public void wordMatching() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT,
				"some (&Aacute;llatgy&oacute;gy&aacute;szati prescription) content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(
				Collections.<String, Serializable> singletonMap("name", "Állatgyógyászati prescription"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Exact matching_no match.
	 */
	public void exactMatching_noMatch() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		configuration.put(RuleMatcher.EXACT_MATCH, true);
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT,
				"some (&Aacute;llatgy&oacute;gy&aacute;szati - prescription) content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(
				Collections.<String, Serializable> singletonMap("name", "Állatgyógyászati prescription"));
		assertFalse(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Exact matching_match.
	 */
	public void exactMatching_match() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		configuration.put(RuleMatcher.EXACT_MATCH, true);
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT,
				"some (&Aacute;llatgy&oacute;gy&aacute;szati prescription) content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(
				Collections.<String, Serializable> singletonMap("name", "Állatgyógyászati prescription"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Exact matching_minimal length.
	 */
	public void exactMatching_minimalLength() {
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList("name"));
		configuration.put(RuleMatcher.MINIMAL_LENGTH, 4);
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap("name", "som"));
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT,
				"som (&Aacute;llatgy&oacute;gy&aacute;szati prescription) content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		assertFalse(contentMatcher.match(processingContext, instanceToMatch, context));
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap("name", "Állatgyógyászati"));
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Test normal matching with el expression.
	 */
	public void testNormalMatchingElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList(EXPRESSION_NAME));
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT, "some test content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap(EXPRESSION_NAME, "test"));
		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(instanceToMatch, null, null)).thenReturn(
				expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(EXPRESSION_NAME, Serializable.class, expressionContext))
					.thenReturn("test");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertTrue(contentMatcher.match(processingContext, instanceToMatch, context));
	}

	/**
	 * Test not matching with el expression.
	 */
	public void testNormalNotMatchingElExpression() {
		String EXPRESSION_NAME = "${get([name])}";
		Context<String, Object> configuration = new Context<>();
		configuration.put("checkForProperties", Collections.singletonList(EXPRESSION_NAME));
		contentMatcher.configure(configuration);

		EmfInstance EmfInstance = new EmfInstance();
		EmfInstance.setId("normalDocument");
		EmfInstance.setProperties(CollectionUtils.<String, Serializable> emptyMap());
		Context<String, Object> context = new Context<>();
		Map<String, Object> createHashMap = CollectionUtils.createHashMap(2);
		createHashMap.put(RuleContext.PROCESSING_INSTANCE, EmfInstance);
		createHashMap.put(DefaultProperties.CONTENT, "some test content");
		Context<String, Object> processingContext = new Context<String, Object>(createHashMap);
		Instance instanceToMatch = new EmfInstance();
		instanceToMatch.setId("someInstance");
		instanceToMatch.setProperties(Collections.<String, Serializable> singletonMap(EXPRESSION_NAME, "test"));
		ExpressionContext expressionContext = Mockito.mock(ExpressionContext.class);
		Mockito.when(expressionsManager.createDefaultContext(instanceToMatch, null, null)).thenReturn(
				expressionContext);
		Mockito
				.when(expressionsManager.evaluateRule(EXPRESSION_NAME, Serializable.class, expressionContext))
					.thenReturn("testNotMatched");
		Mockito.when(expressionsManager.isExpression(EXPRESSION_NAME)).thenReturn(true);
		assertFalse(contentMatcher.match(processingContext, instanceToMatch, context));
	}
}
