package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Expression evaluator that handles the configuration extractions. The expression supports a default value. The
 * expression key must contains: alphanumeric, dot, dash, underscore.<br>
 * NOTE: The evaluator always returns string.
 *
 * @author BBonev
 */
@Singleton
public class ConfigurationEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 5780733456651576962L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{config\\(([\\w\\.\\-]+)\\s*,?\\s*(.*?)\\)\\}");

	@Inject
	private RawConfigurationAccessor configurationAccessor;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "config";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String configKey = matcher.group(1);
		String defaultValue = matcher.group(2);
		String value = configurationAccessor.getRawConfigurationValue(configKey);
		return escape(value == null ? defaultValue : value);
	}

}
