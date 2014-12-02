package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.emf.script.ScriptEvaluator;

/**
 * Expression evaluator that handles arithmetic expression using JavaScript API to execute the
 * expressions
 *
 * @author BBonev
 */
public class ArithmeticEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7512143144437762612L;
	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{=([\\d+-/*\\)\\(\\.,\\s%\\|\\&nul\"]*+)\\}");
	private static final Pattern NULL_PATTERN = Pattern.compile("null");
	/** The script evaluator. */
	@Inject
	private ScriptEvaluator scriptEvaluator;
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
	private String escapeExpression(String expression) {
		String escaped = NULL_PATTERN.matcher(expression).replaceAll("0");
		return new StringBuilder(escaped.length() + 10).append('(').append(escaped)
				.append(")+\"\"").toString();
	}

}
