package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.script.ScriptEvaluator;

/**
 * Expression evaluator that handles arithmetic expression using JavaScript API to execute the expressions
 *
 * @author BBonev
 */
@Singleton
public class ArithmeticEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 7512143144437762612L;
	private static final Pattern NULL_PATTERN = Pattern.compile("null");
	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{=([\\d+-/*\\)\\(\\.,\\s%\\|\\&nul\"]*+)\\}");

	@Inject
	private ScriptEvaluator scriptEvaluator;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String expression = matcher.group(1);

		Object object = scriptEvaluator.eval(escapeExpression(expression), null);
		if (object instanceof Serializable) {
			return (Serializable) object;
		}
		return Integer.valueOf(0);
	}

	/**
	 * Remove all null values that have been evaluated with zeroes not to cause exceptions
	 *
	 * @param expression
	 *            the expression
	 * @return the string
	 */
	private static String escapeExpression(String expression) {
		String escaped = NULL_PATTERN.matcher(expression).replaceAll("0");
		return new StringBuilder(escaped.length() + 10).append('(').append(escaped).append(")+\"\"").toString();
	}

}
