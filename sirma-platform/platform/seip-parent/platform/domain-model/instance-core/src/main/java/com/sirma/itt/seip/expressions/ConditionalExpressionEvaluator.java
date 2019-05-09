package com.sirma.itt.seip.expressions;

import static com.sirma.itt.seip.util.EqualsHelper.equalsTo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.ComputationChain;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Conditional expression evaluation. Supports a condition checking and returning one of 2 results. Supported operations
 * are: {@literal <, >, <>, ==, <=, >= }. Also is supported direct boolean evaluations if boolean value is provided as:
 * true or false. The evaluator supports and pattern validation as: <code>(to_validate).matches(pattern)</code>
 *
 * @author BBonev
 */
@Singleton
public class ConditionalExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -419531746328478497L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{if\\((.*?)\\).then\\((.*?)\\)(?:.else\\((.*?)\\))?\\}", Pattern.DOTALL);

	private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(.*?)\\s*(<>|<=|>=|==|>|<)\\s*(.*?)");

	private static final Pattern MATCHER_PATTERN = Pattern.compile("\\((.*?)\\)\\.?matches\\((?<matches>.+?)\\)");

	private ComputationChain<String, BiFunction<String, String, Boolean>> chain;

	private final Lock lock = new ReentrantLock();

	private static final List<String> FALSE_VALUES = Arrays.asList("", "false", "null");
	private static final String TRUE = "true";

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "if";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		if (eval(matcher.group(1))) {
			return org.apache.commons.lang3.StringUtils.trimToEmpty(matcher.group(2));
		}
		return org.apache.commons.lang3.StringUtils.trimToEmpty(matcher.group(3));
	}

	/**
	 * Evaluate the expression
	 *
	 * @param expression
	 *            the expression
	 * @return the boolean
	 */
	private Boolean eval(String expression) {
		if (StringUtils.isBlank(expression)) {
			return Boolean.FALSE;
		}

		String expLower = expression.trim().toLowerCase();
		if (FALSE_VALUES.contains(expLower)) {
			return Boolean.FALSE;
		}

		if (ConditionalExpressionEvaluator.TRUE.equals(expLower)) {
			return Boolean.TRUE;
		}

		return evaluateExpression(expression).orElse(evaluateMatcher(expression).orElse(Boolean.FALSE));
	}

	/**
	 * Evaluate matcher expression.
	 *
	 * @param expression
	 *            the expression
	 * @return {@link Optional} boolean or {@link Optional#empty()} if not such expression
	 */
	private Optional<Boolean> evaluateMatcher(String expression) {
		Matcher matcher = MATCHER_PATTERN.matcher(expression);
		if (matcher.matches()) {
			return Optional.of(isResultValid(matcher.group(1), matcher));
		}

		return Optional.empty();
	}

	/**
	 * Evaluate compare expression.
	 *
	 * @param expression
	 *            the expression
	 * @return {@link Optional} of boolean or {@link Optional#empty()} if not such expression
	 */
	private Optional<Boolean> evaluateExpression(String expression) {
		Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
		if (!matcher.matches()) {
			return Optional.empty();
		}

		String first = normalizeValue(matcher.group(1));
		String sign = matcher.group(2);
		String second = normalizeValue(matcher.group(3));
		BiFunction<String, String, Boolean> biFunction = getComputationChain().execute(sign);
		return Optional.of(biFunction.apply(first, second));
	}

	// make sure the empty string values are treated as nulls
	private String normalizeValue(String value) {
		if (StringUtils.isBlank(value)) {
			return "null";
		}
		return value;
	}

	/**
	 * Creates a computational chain if it's not initialized yet.
	 *
	 * @return the computational chain
	 */
	private ComputationChain<String, BiFunction<String, String, Boolean>> getComputationChain() {
		if (chain == null) {
			lock.lock();
			try {
				if (chain == null) {
					ComputationChain<String, BiFunction<String, String, Boolean>> aChain = new ComputationChain<>();
					buildComputationChain(aChain);
					// the default value will prevent a NPE then executing the chain
					aChain.addDefault((first, second) -> null);
					// when the chain is initialize assign it
					chain = aChain;
				}
			} finally {
				lock.unlock();
			}
		}
		return chain;
	}

	/**
	 * Populates the provided computational chain with specific evaluation steps.
	 *
	 * @param chain
	 *            the computational chain
	 */
	private static void buildComputationChain(ComputationChain<String, BiFunction<String, String, Boolean>> chain) {
		chain.addStep(equalsTo("<>"), (first, second) -> different(first, second));
		chain.addStep(equalsTo("=="), (first, second) -> first.equalsIgnoreCase(second));
		chain.addStep(equalsTo("<"), (first, second) -> less(first, second));
		chain.addStep(equalsTo("<="), (first, second) -> lessOrEqual(first, second));
		chain.addStep(equalsTo(">"), (first, second) -> more(first, second));
		chain.addStep(equalsTo(">="), (first, second) -> moreOrEqual(first, second));
	}

	/**
	 * Different.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the boolean
	 */
	private static Boolean different(String first, String second) {
		return !first.equalsIgnoreCase(second);
	}

	/**
	 * More or equal.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the boolean
	 */
	private static Boolean moreOrEqual(String first, String second) {
		try {
			BigDecimal firstInt = new BigDecimal(first);
			BigDecimal secondInt = new BigDecimal(second);
			return firstInt.compareTo(secondInt) >= 0;
		} catch (NumberFormatException e) {
			return first.compareToIgnoreCase(second) >= 0;
		}
	}

	/**
	 * More.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the boolean
	 */
	private static Boolean more(String first, String second) {
		try {
			BigDecimal firstInt = new BigDecimal(first);
			BigDecimal secondInt = new BigDecimal(second);
			return firstInt.compareTo(secondInt) > 0;
		} catch (NumberFormatException e) {
			return first.compareToIgnoreCase(second) > 0;
		}
	}

	/**
	 * Less or equal.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the boolean
	 */
	private static Boolean lessOrEqual(String first, String second) {
		try {
			BigDecimal firstInt = new BigDecimal(first);
			BigDecimal secondInt = new BigDecimal(second);
			return firstInt.compareTo(secondInt) <= 0;
		} catch (NumberFormatException e) {
			return first.compareToIgnoreCase(second) <= 0;
		}
	}

	/**
	 * Less.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the boolean
	 */
	private static Boolean less(String first, String second) {
		try {
			BigDecimal firstInt = new BigDecimal(first);
			BigDecimal secondInt = new BigDecimal(second);
			return firstInt.compareTo(secondInt) < 0;
		} catch (NumberFormatException e) {
			return first.compareToIgnoreCase(second) < 0;
		}
	}

}
