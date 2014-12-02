package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.emf.configuration.SystemConfiguration;

/**
 * Expression evaluator that handles the configuration extractions. The expression supports a
 * default value. The expression key must contains: alphanumeric, dot, dash, underscore.<br>
 * NOTE: The evaluator always returns string.
 * 
 * @author BBonev
 */
public class ConfigurationEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 5780733456651576962L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{config\\(([\\w\\.\\-]+)\\s*,?\\s*(.*?)\\)\\}");

	/** The system configuration. */
	@Inject
	private SystemConfiguration systemConfiguration;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String configKey = matcher.group(1);
		String defaultValue = matcher.group(2);
		return systemConfiguration.getConfiguration(configKey, defaultValue);
	}

}
