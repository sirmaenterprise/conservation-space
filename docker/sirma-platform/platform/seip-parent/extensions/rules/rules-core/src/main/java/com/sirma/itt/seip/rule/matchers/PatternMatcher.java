package com.sirma.itt.seip.rule.matchers;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The Class PatternMatcher.
 *
 * @author Hristo Lungov
 */
@Named(PatternMatcher.PATTERN_MATCHER_NAME)
public class PatternMatcher extends BaseRuleMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcher.class);

	public static final String PATTERN_MATCHER_NAME = "patternMatcher";

	public static final String CURRENT_INSTANCE_PROPS = "currentInstanceProps";
	public static final String FOUND_INSTANCE_PROPS = "foundInstanceProps";
	public static final String CURRENT_INSTANCE_REGEX = "currentInstanceRegex";
	public static final String FOUND_INSTANCE_REGEX = "foundInstanceRegex";

	private Collection<String> currentInstanceProps;

	private Collection<String> foundInstanceProps;

	private Pattern currentInstancePattern = null;

	private Pattern foundInstancePattern = null;

	private boolean disabled = false;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			disabled = true;
			return false;
		}
		currentInstanceProps = configuration.getIfSameType(CURRENT_INSTANCE_PROPS, Collection.class);
		String currentInstanceRegex = configuration.getIfSameType(CURRENT_INSTANCE_REGEX, String.class);

		foundInstanceProps = configuration.getIfSameType(FOUND_INSTANCE_PROPS, Collection.class);
		String foundInstanceRegex = configuration.getIfSameType(FOUND_INSTANCE_REGEX, String.class);

		if (!isValidConfig(currentInstanceProps, currentInstanceRegex)
				&& !isValidConfig(foundInstanceProps, foundInstanceRegex)) {
			LOGGER.warn(
					"PatternMatcher will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					configuration);
			disabled = true;
		} else {
			disabled = false;
		}
		currentInstancePattern = compilePattern(currentInstanceRegex);
		foundInstancePattern = compilePattern(foundInstanceRegex);
		return !disabled;
	}

	/**
	 * Compile and validate the given pattern.
	 *
	 * @param pattern
	 *            the pattern to parse and compile
	 * @return the pattern
	 */
	private Pattern compilePattern(String pattern) {
		if (StringUtils.isNotBlank(pattern)) {
			try {
				return buildPattern(pattern, ignoreCase);
			} catch (Exception e) {
				LOGGER.error("Error compiling patern: {} due to {}", pattern, e.getMessage());
				LOGGER.trace("Error compiling patern: {}", pattern, e);
				disabled = true;
			}
		}
		return null;
	}

	/**
	 * Configuration checker.
	 *
	 * @param collection
	 *            the collection
	 * @param regex
	 *            the regex
	 * @return true, if successful
	 */
	private boolean isValidConfig(Collection<String> collection, String regex) {
		if (CollectionUtils.isEmpty(collection) || StringUtils.isBlank(regex)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean match(Context<String, Object> processingContext, Instance instanceToMatch,
			Context<String, Object> context) {
		if (disabled) {
			return false;
		}
		boolean proccesingMatch = true;
		boolean foundMatch = true;
		if (CollectionUtils.isNotEmpty(currentInstanceProps) && currentInstancePattern != null) {
			Instance processedInstance = getProcessedInstance(processingContext);
			proccesingMatch = matchInternal(processedInstance, currentInstanceProps, currentInstancePattern);
		}
		if (CollectionUtils.isNotEmpty(foundInstanceProps) && foundInstancePattern != null) {
			foundMatch = matchInternal(instanceToMatch, foundInstanceProps, foundInstancePattern);
		}
		return proccesingMatch && foundMatch;
	}

	/**
	 * Match internal.
	 *
	 * @param instance
	 *            the instance
	 * @param props
	 *            the props
	 * @param pattern
	 *            the pattern
	 * @return true, if successful
	 */
	private boolean matchInternal(Instance instance, Collection<String> props, Pattern pattern) {
		if (CollectionUtils.isNotEmpty(props) && pattern != null) {
			Map<String, Serializable> instanceToMatchProperties = instance.getProperties();
			for (String foundInstanceProp : props) {
				Serializable toCheckValue = instanceToMatchProperties.get(foundInstanceProp);
				if (toCheckValue instanceof String) {
					return checkProperty(toCheckValue.toString(), pattern);
				} else if (toCheckValue instanceof Collection<?>) {
					return checkInCollection((Collection<Serializable>) toCheckValue, pattern);
				}
			}

		}
		return false;
	}

	/**
	 * Check value in collection.
	 *
	 * @param collection
	 *            the collection
	 * @param pattern
	 *            the pattern
	 * @return true, if successful
	 */
	private boolean checkInCollection(Collection<Serializable> collection, Pattern pattern) {
		for (Serializable inToCheckValue : collection) {
			String valueToCheck = null;
			if (inToCheckValue instanceof String) {
				valueToCheck = (String) inToCheckValue;
			} else {
				// try to convert the collection value to string if not
				valueToCheck = typeConverter.tryConvert(String.class, inToCheckValue);
			}
			if (valueToCheck != null && checkProperty(valueToCheck, pattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check property.
	 *
	 * @param toCheckValue
	 *            the to check value
	 * @param pattern
	 *            the pattern
	 * @return true, if successful
	 */
	private boolean checkProperty(String toCheckValue, Pattern pattern) {
		if (pattern.matcher(toCheckValue).matches()) {
			return true;
		}
		return false;
	}

	@Override
	public String getPrimaryOperation() {
		return PATTERN_MATCHER_NAME;
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
		return PATTERN_MATCHER_NAME;
	}

}
