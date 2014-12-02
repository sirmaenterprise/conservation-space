package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.collections.FixedSizeMap;
import com.sirma.itt.emf.evaluation.el.ElExpression;
import com.sirma.itt.emf.evaluation.el.ElExpressionParser;
import com.sirma.itt.emf.util.DigestUtils;

/**
 * Expression evaluator that parses and evaluates multiple expressions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class EvalEvaluator extends BaseEvaluator {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3598831824682553159L;

	/**
	 * Matcher for EVAL expression. The expression could be on multiple lines that's why we have the
	 * DOTALL flag active.
	 */
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			"\\s*(\\$|#)\\{eval\\((.+?)\\)\\}\\s*", Pattern.DOTALL);

	/**
	 * The expression cache for compiled expressions mapped by digest of the string content. The
	 * cache has overflow policy not to store more than configured size.
	 */
	private transient Map<String, ElExpression> expressionCache = new FixedSizeMap<>(1024);

	/** The manager. */
	@Inject
	private ExpressionsManager manager;

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
		synchronized (expressionCache) {
			ElExpression elExpression = expressionCache.get(expression);
			if (elExpression == null) {
				elExpression = ElExpressionParser.parse(expression, mode);
				// we cache parsed only default expression not dynamic ones
				if (mode == ElExpressionParser.DEFAULT_EXPRESSION_ID) {
					expressionCache.put(digest, elExpression);
				}
			}
			return elExpression;
		}
	}

}
