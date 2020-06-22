package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.FixedSizeMap;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.util.DigestUtils;

/**
 * Expression evaluator that parses and evaluates multiple expressions.
 *
 * @author BBonev
 */
@Singleton
public class EvalEvaluator extends BaseEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(EvalEvaluator.class);

	/**
	 * The variable name for the {@link ExpressionContext} that holds the current evaluation cache. It contains past
	 * evaluation results.
	 */
	public static final String EVALUATION_CACHE = "evalCache";
	private static final Pattern CLEAR_QUOTES = Pattern.compile("(?<=\\{|\\})'|'(?=\\{|\\})");
	private static final Pattern CLEAR_NULLS = Pattern
			.compile("(?:<b>[^>\")\\w]*\\bnull\\b[^<\"\\w]*</b>[^<\"\\w]*)|(?:[^>\")\\w]*\\bnull\\b[^<>\"\\w]*)");
	private static final Pattern CLEAR_MULTI_WS = Pattern.compile("(?<=>)\\s+(?=<)|(?=\\s{2,})(\\s)?\\s{1,}");

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
		return eval(elExpression, manager, converter, context, mode, values);
	}

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

	private synchronized Map<String, ElExpression> getCache() {
		if (expressionCache == null) {
			expressionCache = new FixedSizeMap<>(1024);
		}
		return expressionCache;
	}

	private static Serializable eval(ElExpression exp, ExpressionsManager manager, TypeConverter converter,
			ExpressionContext context, char expressionId, Serializable... values) {
		Serializable eval = evalInternal(exp, manager, converter, context, expressionId, values);
		if (eval instanceof String) {
			// removes the nulls at the end of the evaluation due to if removed before that breaks
			// most of the expression evaluators due to the fact that they expect data even null data
			eval = CLEAR_NULLS.matcher(eval.toString()).replaceAll(" ");
			eval = CLEAR_MULTI_WS.matcher(eval.toString()).replaceAll("$1");
		}
		return eval;
	}

	private static Serializable evalInternal(ElExpression exp, ExpressionsManager manager, TypeConverter converter,
			ExpressionContext context, char expressionId, Serializable... values) {
		if (exp.getSubExpressions().isEmpty()) {
			if (ElExpressionParser.isExpression(exp.getExpression())) {
				return evalSingleExpression(exp, exp.getExpression(), manager, context, expressionId, values);
			}
			return exp.getExpression();
		}
		List<Serializable> params = evalSubExpressions(exp, manager, converter, context, expressionId, values);

		String formatedExpression = MessageFormat.format(exp.getExpression(), params.toArray());
		// if we had a complex expression evaluate it
		if (ElExpressionParser.isExpression(formatedExpression)) {
			return evalSingleExpression(exp, formatedExpression, manager, context, expressionId, values);
		}
		return clearQuotes(formatedExpression);
	}

	private static List<Serializable> evalSubExpressions(ElExpression exp, ExpressionsManager manager,
			TypeConverter converter, ExpressionContext context, char expressionId, Serializable... values) {
		List<Serializable> params = new ArrayList<>(exp.getSubExpressions().size());
		for (ElExpression subExpression : exp.getSubExpressions()) {
			Serializable serializable = evalInternal(subExpression, manager, converter, context, expressionId, values);
			String convert = converter.convert(String.class, serializable);
			params.add(convert);
		}
		return params;
	}

	private static Serializable evalSingleExpression(ElExpression exp, String expression, ExpressionsManager manager,
			ExpressionContext context, char expressionId, Serializable... values) {
		String localExp = expression;
		if (StringUtils.isBlank(localExp)) {
			return "";
		}
		if (!shouldEvalExpression(localExp, expressionId)) {
			return clearQuotes(localExp);
		}
		Map<String, Serializable> cache = getEvalCache(context);
		localExp = clearQuotes(localExp);
		Serializable previsousEvaluated = cache.get(localExp);
		if (previsousEvaluated != null) {
			return previsousEvaluated;
		}

		ExpressionEvaluator evaluator = getEvaluator(exp, manager, localExp);

		if (evaluator == null) {
			LOGGER.error("No evaluator found for expression {}", expression);
			return null;
		}

		Serializable evaluated = evaluator.evaluate(localExp, context, values);
		// does not store expression values that does not support caching like sequence expression
		if (evaluator.isCachingSupported()) {
			cache.put(localExp, evaluated);
		}
		return evaluated;
	}

	private static ExpressionEvaluator getEvaluator(ElExpression exp, ExpressionsManager manager, String localExp) {
		ExpressionEvaluator evaluator = exp.getEvaluator();
		if (evaluator == null) {
			evaluator = manager.getEvaluator(exp.getExpressionId(), localExp);
			// cache the evaluator that can execute the current expression to minimize the search
			// for supported evaluator this is useful now when we have cache of parsed tree.
			exp.setEvaluator(evaluator);
		}

		return evaluator;
	}

	private static String clearQuotes(String exp) {
		return CLEAR_QUOTES.matcher(exp).replaceAll("");
	}

	private static boolean shouldEvalExpression(String expression, char currentExpId) {
		if (StringUtils.isBlank(expression)) {
			return false;
		}
		return expression.charAt(0) == currentExpId;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Serializable> getEvalCache(ExpressionContext context) {
		return (Map<String, Serializable>) context.computeIfAbsent(EVALUATION_CACHE, key -> new LinkedHashMap<String, Serializable>());
	}

}
