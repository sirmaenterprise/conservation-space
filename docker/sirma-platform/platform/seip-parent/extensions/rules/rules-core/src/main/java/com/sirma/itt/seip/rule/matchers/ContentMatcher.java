package com.sirma.itt.seip.rule.matchers;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.rule.util.ContentLoader;

/**
 * Matcher that search for properties from passed instance in the content of the current processing instance.
 *
 * @author BBonev
 */
@Named(ContentMatcher.CONTENT_MATCHER_NAME)
public class ContentMatcher extends BaseRuleMatcher {

	private static final String UNESCAPED = "unescaped";

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentMatcher.class);

	public static final String CONTENT_MATCHER_NAME = "contentMatcher";

	@Inject
	private ExpressionsManager expressionsManager;

	@Inject
	private ContentLoader contentLoader;

	private Collection<String> checkForProperties;

	@Override
	public String getPrimaryOperation() {
		return CONTENT_MATCHER_NAME;
	}

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}
		checkForProperties = configuration.getIfSameType("checkForProperties", Collection.class);
		if (CollectionUtils.isEmpty(checkForProperties)) {
			LOGGER.warn(
					"Content matcher will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					configuration);
		}
		return CollectionUtils.isNotEmpty(checkForProperties);
	}

	@Override
	public boolean match(Context<String, Object> processingContext, Instance instanceToMatch,
			Context<String, Object> context) {
		Instance processedInstance = getProcessedInstance(processingContext);
		String content = loadContent(processedInstance, processingContext);
		if (!match(content, instanceToMatch)) {
			content = getUnescapedContent(content, context);
		} else {
			// found the first time no need to match again
			return matcherResult(true);
		}
		return matcherResult(match(content, instanceToMatch));
	}

	/**
	 * Gets the unescaped content.
	 *
	 * @param content
	 *            the content
	 * @param context
	 *            the context
	 * @return the unescaped content
	 */
	private String getUnescapedContent(String content, Context<String, Object> context) {
		if (content == null) {
			return null;
		}
		String unescaped = context.getIfSameType(UNESCAPED, String.class);
		if (unescaped == null) {
			unescaped = StringEscapeUtils.unescapeHtml(content);
			/*
			 * if the lengths are equal then the content are the same due to the fact that when unescaping the length
			 * should change. We set an empty string in the cache to prevent multiple unescaping of the same string.
			 * Also this will force the matcher to return false.
			 */
			if (content.length() == unescaped.length()) {
				unescaped = null;
				context.put(UNESCAPED, "");
			} else {
				context.put(UNESCAPED, unescaped);
			}
		} else if (unescaped.isEmpty()) {
			// we have already check for unescaping so just return null
			unescaped = null;
		}

		return unescaped;
	}

	/**
	 * Match.
	 *
	 * @param content
	 *            the content
	 * @param found
	 *            the found
	 * @return true, if successful
	 */
	private boolean match(String content, Instance found) {
		if (content == null || CollectionUtils.isEmpty(checkForProperties)) {
			return false;
		}
		ExpressionContext expressionContext = expressionsManager.createDefaultContext(found, null, null);
		for (String property : checkForProperties) {
			Serializable value;
			if (expressionsManager.isExpression(property)) {
				value = expressionsManager.evaluateRule(property, Serializable.class, expressionContext);
			} else {
				value = found.get(property);
			}
			if (value instanceof String && isLengthAllowed(value.toString(), minimalLength)) {
				Pattern pattern = buildPattern(value.toString(), exactMatch, ignoreCase);
				if (pattern.matcher(content).find()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Load content.
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param context
	 *            the context
	 * @return the string
	 */
	private String loadContent(Instance currentInstance, Context<String, Object> context) {

		String currentContent = context.getIfSameType(DefaultProperties.CONTENT, String.class);
		if (currentContent != null) {
			return currentContent;
		}

		currentContent = contentLoader.loadContent(currentInstance);

		if (currentContent == null) {
			LOGGER.warn("Instance content not found for {} with id={}", currentInstance.getClass().getSimpleName(),
					currentInstance.getId());
		} else {
			context.put(DefaultProperties.CONTENT, currentContent);
		}

		return currentContent;
	}

	@Override
	public String getName() {
		return CONTENT_MATCHER_NAME;
	}
}
