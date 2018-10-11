package com.sirma.itt.seip.rule.matchers;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;

/**
 * Instance rule matcher that matches properties of the current instance against the matched instance.
 *
 * @author hlungov
 */
@Named(PropertyMatcher.PROPERTY_MATCHER_NAME)
public class PropertyMatcher extends BaseRuleMatcher {

	public static final String PROPERTY_MATCHER_NAME = "propertyMatcher";

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyMatcher.class);

	private Collection<String> checkForProperties;
	private Collection<String> searchInProperties;
	private boolean invertMatching;

	private boolean disabled;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private ExpressionsManager expressionsManager;

	@Override
	public String getPrimaryOperation() {
		return PROPERTY_MATCHER_NAME;
	}

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			disabled = true;
			return false;
		}
		checkForProperties = configuration.getIfSameType(CHECK_FOR_PROPERTIES, Collection.class);
		searchInProperties = configuration.getIfSameType(SEARCH_IN_PROPERTIES, Collection.class);
		invertMatching = configuration.getIfSameType(INVERT_MATCHING, Boolean.class, Boolean.FALSE).booleanValue();
		if (invertMatching) {
			// if we have invert matching the contains option should be enabled otherwise it will do nothing it will be
			// the same as the non inverted matcher
			containsMatch = Boolean.TRUE;
		}
		if (CollectionUtils.isEmpty(checkForProperties) || CollectionUtils.isEmpty(searchInProperties)) {
			LOGGER.warn(
					"Content matcher will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					configuration);
			disabled = true;
		}
		return !disabled;
	}

	@Override
	public boolean match(Context<String, Object> processingContext, Instance instanceToMatch,
			Context<String, Object> context) {
		Instance processedInstance = getProcessedInstance(processingContext);

		// depending on the configuration invertMatching the search is performed in the specified direction
		// the normal case is processedInstance -> instanceToMatch
		// the inverted case is instanceToMatch -> processedInstance

		Instance searchFrom = invertMatching ? instanceToMatch : processedInstance;
		Instance searchIn = invertMatching ? processedInstance : instanceToMatch;

		ExpressionContext searchFromContext = expressionsManager.createDefaultContext(searchFrom, null, null);
		ExpressionContext searchInContext = expressionsManager.createDefaultContext(searchIn, null, null);

		Collection<String> lookForProperties = invertMatching ? searchInProperties : checkForProperties;
		Collection<String> lookIntoProperties = invertMatching ? checkForProperties : searchInProperties;

		return doMaching(searchFrom, searchIn, searchFromContext, searchInContext, lookForProperties,
				lookIntoProperties);
	}

	private boolean doMaching(Instance searchFrom, Instance searchIn, ExpressionContext searchFromContext,
			ExpressionContext searchInContext, Collection<String> lookForProperties,
			Collection<String> lookIntoProperties) {
		for (String toCheckProperty : lookForProperties) {
			Serializable value = evaluateOrGetValue(toCheckProperty, searchFrom, searchFromContext);
			if (checkValue(value, searchInContext, searchIn, lookIntoProperties)) {
				return matcherResult(true);
			}
		}
		return matcherResult(false);
	}

	/**
	 * Checks if is match property value.
	 *
	 * @param searchFor
	 *            value to be checked.
	 * @param searchInContext
	 *            the instance to match expression context
	 * @param toMatchIn
	 *            the to match in instance
	 * @param lookIntoProperties
	 *            the look into properties
	 * @return true, if is match property value
	 */
	@SuppressWarnings("unchecked")
	private boolean checkValue(Serializable searchFor, ExpressionContext searchInContext, Instance toMatchIn,
			Collection<String> lookIntoProperties) {
		for (String toMatchProperty : lookIntoProperties) {
			Serializable searchIn = evaluateOrGetValue(toMatchProperty, toMatchIn, searchInContext);
			boolean isMatched = false;
			if (searchFor instanceof String && searchIn instanceof String) {
				isMatched = checkProperties((String) searchFor, (String) searchIn);
			} else if (searchFor instanceof Collection<?> && searchIn instanceof String) {
				isMatched = checkValueInCollection((Collection<Serializable>) searchFor, searchIn);
			} else if (searchFor instanceof String && searchIn instanceof Collection<?>) {
				isMatched = checkValueInCollection((Collection<Serializable>) searchIn, searchFor);
			}
			if (isMatched) {
				return true;
			}
		}
		return false;
	}

	private Serializable evaluateOrGetValue(String property, Instance source, ExpressionContext context) {
		if (expressionsManager.isExpression(property)) {
			return expressionsManager.evaluateRule(property, Serializable.class, context);
		}
		return source.get(property);
	}

	/**
	 * Check value in collection.
	 *
	 * @param collection
	 *            the collection values to check
	 * @param toMatchValue
	 *            the value to match in the given collection
	 * @return true, if successful
	 */
	private boolean checkValueInCollection(Collection<Serializable> collection, Serializable toMatchValue) {
		for (Serializable inToCheckValue : collection) {
			String valueToCheck = null;
			if (inToCheckValue instanceof String) {
				valueToCheck = (String) inToCheckValue;
			} else {
				// try to convert the collection value to string if not
				valueToCheck = typeConverter.tryConvert(String.class, inToCheckValue);
			}
			if (valueToCheck != null && checkProperties(valueToCheck, (String) toMatchValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check two properties.
	 *
	 * @param searchFor
	 *            the to check value
	 * @param searchIn
	 *            the to match value
	 * @return true, if successful
	 */
	private boolean checkProperties(String searchFor, String searchIn) {
		if (searchFor.length() > searchIn.length() || !isLengthAllowed(searchFor, minimalLength.intValue())) {
			// if the value we are going to search in is shorter than the search pattern
			// or the search pattern does not reach the minimum length, no need to continue
			return false;
		}
		Pattern pattern = buildPattern(searchFor, exactMatch.booleanValue(), ignoreCase.booleanValue());
		Matcher matcher = pattern.matcher(searchIn);
		if (containsMatch.booleanValue()) {
			return matcher.find();
		}
		return matcher.matches();
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		if (disabled) {
			return false;
		}
		return super.isApplicable(context);
	}

	@Override
	public String getName() {
		return PROPERTY_MATCHER_NAME;
	}
}
