package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.FixedSizeMap;
import com.sirma.itt.seip.util.DigestUtils;

/**
 * Expression evaluator that parses and evaluates multiple expressions.
 *
 * @author BBonev
 */
@Singleton
public class EvalEvaluator extends BaseEvaluator {
	private static final long serialVersionUID = -3598831824682553159L;

	/**
	 * Matcher for EVAL expression. The expression could be on multiple lines that's why we have the DOTALL flag active.
	 */
	private static final Pattern FIELD_PATTERN = Pattern.compile("\\s*(\\$|#)\\{eval\\((.+?)\\)\\}\\s*",
			Pattern.DOTALL);

	/**
	 * The expression cache for compiled expressions mapped by digest of the string content. The cache has overflow
	 * policy not to store more than configured size.
	 */
	private transient Map<String, ElExpression> expressionCache;

	@Inject
	private ExpressionsManager manager;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "eval";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		char mode = matcher.group(1).charAt(0);
		String expression = matcher.group(2);
		ElExpression elExpression = getParsedExpression(expression, mode);
		return ElExpressionParser.eval(elExpression, manager, converter, context, mode, values);
	}

	/**
	 * Gets the parsed expression.
	 *
	 * @param expression
	 *            the expression
	 * @param mode
	 *            expression parser mode
	 * @return the parsed expression
	 */
	private ElExpression getParsedExpression(String expression, char mode) {
		String digest = DigestUtils.calculateDigest(expression);
		ElExpression elExpression = getCache().get(expression);
		if (elExpression == null) {
			elExpression = ElExpressionParser.parse(expression, mode);
			// we cache parsed only default expression not dynamic ones
			if (mode == ElExpressionParser.DEFAULT_EXPRESSION_ID) {
				getCache().put(digest, elExpression);
			}
		}
		return elExpression;
	}

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	private synchronized Map<String, ElExpression> getCache() {
		if (expressionCache == null) {
			expressionCache = new FixedSizeMap<>(1024);
		}
		return expressionCache;
	}

}
