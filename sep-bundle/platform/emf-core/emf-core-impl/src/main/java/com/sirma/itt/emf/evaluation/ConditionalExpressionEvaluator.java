package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Conditional expression evaluation. Supports a condition checking and returning one of 2 results.
 * Supported operations are: <, >, <>, ==, <=, >=. Also is supported direct boolean evaluations if
 * boolean value is provided as: true or false. The evaluator supports and pattern validation as:
 * <code>(to_validate).matches(pattern)</code>
 *
 * @author BBonev
 */
public class ConditionalExpressionEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -419531746328478497L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{if\\((.+?)\\).then\\((.+?)\\)(?:.else\\((.+?)\\))?\\}", Pattern.DOTALL);

	/** The Constant EXPRESSION_PATTERN. */
	private static final Pattern EXPRESSION_PATTERN = Pattern
			.compile("(.*?)\\s*(<>|<=|>=|==|>|<)\\s*(.*?)");

	/** The Constant MATCHER_PATTERN. */
	private static final Pattern MATCHER_PATTERN = Pattern
			.compile("\\((.*?)\\)\\.?matches\\((?<matches>.+?)\\)");
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
		Boolean booleanExpression = null;
		if (StringUtils.isNotNullOrEmpty(expression)) {
			// check if already evaluated to boolean
			if (expression.length() <= 5) {
				String toLower = expression.toLowerCase();
				if (toLower.equals("true")) {
					booleanExpression = Boolean.TRUE;
				} else if (toLower.equals("false")) {
					booleanExpression = Boolean.FALSE;
				}
			}
			if (booleanExpression == null) {
				booleanExpression = evaluateExpression(expression);
			}
			if (booleanExpression == null) {
				booleanExpression = evaluateMatcher(expression);
			}
		}
		String result;
		if (Boolean.TRUE.equals(booleanExpression)) {
			result = matcher.group(2);
		} else {
			result = matcher.group(3);
		}

		if ((result == null) || "null".equals(result)) {
			return "";
		}
		return result;
	}

	/**
	 * Evaluate matcher expression.
	 *
	 * @param expression
	 *            the expression
	 * @return the boolean or <code>null</code> not such
	 */
	private Boolean evaluateMatcher(String expression) {
		Matcher matcher = MATCHER_PATTERN.matcher(expression);
		if (matcher.matches()) {
			return isResultValid(matcher.group(1), matcher);
		}
		return null;
	}

	/**
	 * Evaluate compare expression.
	 *
	 * @param expression
	 *            the expression
	 * @return the boolean or <code>null</code> if not such expression
	 */
	private Boolean evaluateExpression(String expression) {
		Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
		if (matcher.matches()) {
			String first = matcher.group(1);
			String sign = matcher.group(2);
			String second = matcher.group(3);
			if ("<>".equals(sign)) {
				return !first.equalsIgnoreCase(second);
			} else if ("==".equals(sign)) {
				return first.equalsIgnoreCase(second);
			} else if ("<".equals(sign)) {
				try {
					BigDecimal firstInt = new BigDecimal(first);
					BigDecimal secondInt = new BigDecimal(second);
					return firstInt.compareTo(secondInt) < 0;
				} catch (NumberFormatException e) {
					return first.compareToIgnoreCase(second) < 0;
				}
			} else if ("<=".equals(sign)) {
				try {
					BigDecimal firstInt = new BigDecimal(first);
					BigDecimal secondInt = new BigDecimal(second);
					return firstInt.compareTo(secondInt) <= 0;
				} catch (NumberFormatException e) {
					return first.compareToIgnoreCase(second) <= 0;
				}
			} else if (">".equals(sign)) {
				try {
					BigDecimal firstInt = new BigDecimal(first);
					BigDecimal secondInt = new BigDecimal(second);
					return firstInt.compareTo(secondInt) > 0;
				} catch (NumberFormatException e) {
					return first.compareToIgnoreCase(second) > 0;
				}
			} else if (">=".equals(sign)) {
				try {
					BigDecimal firstInt = new BigDecimal(first);
					BigDecimal secondInt = new BigDecimal(second);
					return firstInt.compareTo(secondInt) >= 0;
				} catch (NumberFormatException e) {
					return first.compareToIgnoreCase(second) >= 0;
				}
			}
		}
		return null;
	}

}
