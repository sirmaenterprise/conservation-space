package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.exception.EmfConfigurationException;

/**
 * EL expression parser. The parser offers methods for building expression tree and evaluating the tree.
 *
 * @author BBonev
 */
public class ElExpressionParser {

	public static final char DEFAULT_EXPRESSION_ID = '$';
	public static final char LAZY_EXPRESSION_ID = '#';

	private static final char CLOSE_EXP = '}';
	private static final char OPEN_EXP = '{';
	/**
	 * The variable name for the {@link ExpressionContext} that holds the current evaluation cache. It contains past
	 * evaluation results.
	 */
	public static final String EVALUATION_CACHE = "evalCache";
	private static final Pattern CLEAR_QUOTES = Pattern.compile("(?<=\\{|\\})'|'(?=\\{|\\})");
	private static final Pattern CLEAR_NULLS = Pattern
			.compile("(?:<b>[^>\")\\w]*\\bnull\\b[^<\"\\w]*</b>[^<\"\\w]*)|(?:[^>\")\\w]*\\bnull\\b[^<>\"\\w]*)");
	private static final Pattern CLEAR_MULTI_WS = Pattern.compile("(?<=>)\\s+(?=<)|(?=\\s{2,})(\\s)?\\s{1,}");

	/**
	 * Instantiates a new el expression parser.
	 */
	private ElExpressionParser() {
		// not needed constructor
	}

	/**
	 * Parses the given expression to {@link ElExpression} tree. The parser will look only for expression starting with
	 * the given expression id char.
	 *
	 * @param expression
	 *            the expression
	 * @param expressionId
	 *            the expression id char to look for
	 * @return the el expression tree
	 */
	public static ElExpression parse(String expression, char expressionId) {
		ElExpression elExpression = new ElExpression();
		StringBuilder builder = new StringBuilder();
		char[] data = expression.toCharArray();
		Pair<ElExpression, Integer> pair = parseInternal(data, 0, expressionId);
		if (pair.getSecond().intValue() == expression.length()) {
			return pair.getFirst();
		}
		builder.append(OPEN_EXP).append(elExpression.getSubExpressions().size()).append(CLOSE_EXP);
		elExpression.getSubExpressions().add(pair.getFirst());
		while (pair.getSecond().intValue() < expression.length()) {
			pair = parseInternal(data, pair.getSecond().intValue() + 1, expressionId);
			if (pair.getFirst().getExpression().isEmpty()) {
				break;
			}
			builder.append(OPEN_EXP).append(elExpression.getSubExpressions().size()).append(CLOSE_EXP);
			elExpression.getSubExpressions().add(pair.getFirst());
		}

		elExpression.setExpression(builder.toString());
		return elExpression;
	}

	/**
	 * Parses the given expression to {@link ElExpression} tree.
	 *
	 * @param expression
	 *            the expression
	 * @return the el expression tree
	 */
	public static ElExpression parse(String expression) {
		return parse(expression, DEFAULT_EXPRESSION_ID);
	}

	/**
	 * Parses the internal.
	 *
	 * @param data
	 *            the data
	 * @param begin
	 *            the begin
	 * @param expressionId
	 *            the expression id to look for
	 * @return the el expression
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private static Pair<ElExpression, Integer> parseInternal(char[] data, int begin, char expressionId) {
		// create buffer with length for remaining data to parse
		StringBuilder parsed = new StringBuilder(data.length - begin);
		StringBuilder expId = new StringBuilder();
		ElExpression expression = new ElExpression();
		boolean readingExpId = false;
		boolean isEscape = false;
		boolean escapedSequence = false;
		int i = begin;
		int openBrackets = 0;
		for (; i < data.length; i++) {
			char c = data[i];
			if (isEscape) {
				parsed.append(c);
				isEscape = false;
			} else if (c == '\\') {
				isEscape = true;
			} else if (isBlockEscape(data, expressionId, i, c)) {
				escapedSequence = !escapedSequence;
				i++;
				parsed.append(expressionId);
				parsed.append(expressionId);
			} else if (escapedSequence) {
				parsed.append(c);
			} else if (isSubExpression(data, begin, expressionId, i, c)) {
				i = readSubExression(data, expressionId, parsed, expression, i);
			} else if (c == OPEN_EXP) {
				if (expId.length() == 0) {
					readingExpId = true;
				}
				openBrackets++;
				// escape the open and close brackets so when parsing with message format not to
				// have errors
				parsed.append("'{'");
			} else if (c == CLOSE_EXP) {
				int old = Integer.valueOf(openBrackets).intValue();
				openBrackets--;
				parsed.append("'}'");
				if (old > 0 && openBrackets == 0) {
					break;
				}
			} else {
				parsed.append(c);
				readingExpId = isReadingExpression(expId, readingExpId, c);
			}
		}
		expression.setExpressionId(expId.toString());
		expression.setExpression(parsed.toString());
		return new Pair<>(expression, i);
	}

	private static boolean isSubExpression(char[] data, int begin, char expressionId, int i, char c) {
		return c == expressionId && i + 1 < data.length && data[i + 1] == OPEN_EXP && i > begin;
	}

	private static boolean isBlockEscape(char[] data, char expressionId, int i, char c) {
		return c == expressionId && i + 1 < data.length && data[i + 1] == expressionId;
	}

	private static boolean isReadingExpression(StringBuilder expId, boolean readingExpId, char c) {
		if (readingExpId) {
			if (Character.isAlphabetic(c)) {
				expId.append(c);
			} else {
				return false;
			}
		}
		return readingExpId;
	}

	private static int readSubExression(char[] data, char expressionId, StringBuilder parsed, ElExpression expression,
			int lastStart) {
		// append placeholder for the expression value that will be evaluated later
		parsed.append(OPEN_EXP).append(expression.getSubExpressions().size()).append(CLOSE_EXP);
		// parse the sub expression form the current index
		Pair<ElExpression, Integer> internal = parseInternal(data, lastStart, expressionId);
		// update the index with the last char read from the sub expression - not to read it again
		expression.getSubExpressions().add(internal.getFirst());
		return internal.getSecond().intValue();
	}

	/**
	 * Evaluates the given expression tree to final result.
	 *
	 * @param exp
	 *            the expression
	 * @param manager
	 *            the manager
	 * @param converter
	 *            the converter
	 * @param context
	 *            the context
	 * @param expressionId
	 *            the expression id to evaluate
	 * @param values
	 *            the values
	 * @return the serializable result
	 */
	public static Serializable eval(ElExpression exp, ExpressionsManager manager, TypeConverter converter,
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

	/**
	 * Eval internal.
	 *
	 * @param exp
	 *            the exp
	 * @param manager
	 *            the manager
	 * @param converter
	 *            the converter
	 * @param context
	 *            the context
	 * @param expressionId
	 *            the expresion id
	 * @param values
	 *            the values
	 * @return the serializable
	 */
	private static Serializable evalInternal(ElExpression exp, ExpressionsManager manager, TypeConverter converter,
			ExpressionContext context, char expressionId, Serializable... values) {
		if (exp.getSubExpressions().isEmpty()) {
			if (isExpression(exp.getExpression())) {
				return evalSingleExpression(exp, exp.getExpression(), manager, context, expressionId, values);
			}
			return exp.getExpression();
		}
		List<Serializable> params = evalSubExpressions(exp, manager, converter, context, expressionId, values);

		String formatedExpression = MessageFormat.format(exp.getExpression(), params.toArray());
		// if we had a complex expression evaluate it
		if (isExpression(formatedExpression)) {
			return evalSingleExpression(exp, formatedExpression, manager, context, expressionId, values);
		}
		return clearQuotes(formatedExpression);
	}

	/**
	 * Eval sub expressions.
	 *
	 * @param exp
	 *            the exp
	 * @param manager
	 *            the manager
	 * @param converter
	 *            the converter
	 * @param context
	 *            the context
	 * @param expressionId
	 *            the expresion id
	 * @param values
	 *            the values
	 * @return the list
	 */
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

	/**
	 * Eval single expression.
	 *
	 * @param exp
	 *            the expression object being executed to set the expression evaluator if possible.
	 * @param expression
	 *            the expression
	 * @param manager
	 *            the manager
	 * @param context
	 *            the context
	 * @param expressionId
	 *            the expression id
	 * @param values
	 *            the values
	 * @return the serializable
	 */
	private static Serializable evalSingleExpression(ElExpression exp, String expression, ExpressionsManager manager,
			ExpressionContext context, char expressionId, Serializable... values) {
		String localExp = expression;
		if (StringUtils.isNullOrEmpty(localExp)) {
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

		Serializable evaluated = evaluator.evaluate(localExp, context, values);
		// does not store expression values that does not support caching like sequence expression
		if (evaluator.isCachingSupported()) {
			cache.put(localExp, evaluated);
		}
		return evaluated;
	}

	/**
	 * Gets the evaluator from the expression cache or builds one. If nothing is found {@link EmfConfigurationException}
	 * will be thrown. *
	 *
	 * @param exp
	 *            the exp
	 * @param manager
	 *            the manager
	 * @param localExp
	 *            the local exp
	 * @return the evaluator
	 */
	private static ExpressionEvaluator getEvaluator(ElExpression exp, ExpressionsManager manager, String localExp) {
		ExpressionEvaluator evaluator = exp.getEvaluator();
		if (evaluator == null) {
			evaluator = manager.getEvaluator(exp.getExpressionId(), localExp);
			// cache the evaluator that can execute the current expression to minimize the search
			// for supported evaluator this is useful now when we have cache of parsed tree.
			exp.setEvaluator(evaluator);
		}

		if (evaluator == null) {
			// if reaching here we have not supported expression or one with invalid syntax
			throw new EmfConfigurationException("Expression " + localExp + " not supported!");
		}
		return evaluator;
	}

	/**
	 * Clear quotes. * *
	 *
	 * @param exp
	 *            the exp
	 * @return the string
	 */
	private static String clearQuotes(String exp) {
		return CLEAR_QUOTES.matcher(exp).replaceAll("");
	}

	/**
	 * Checks if is expression * *
	 *
	 * @param expression
	 *            the expression
	 * @param currentExpId
	 *            the current exp id
	 * @return true, if is expression
	 */
	private static boolean shouldEvalExpression(String expression, char currentExpId) {
		if (StringUtils.isNullOrEmpty(expression)) {
			return false;
		}
		return expression.charAt(0) == currentExpId;
	}

	/**
	 * Checks if is expression. The given string is an expression when the first non white space character is one of the
	 * {@link #DEFAULT_EXPRESSION_ID} or {@link #LAZY_EXPRESSION_ID}.
	 *
	 * @param expression
	 *            the expression to check
	 * @return true, if is expression
	 */
	public static boolean isExpression(String expression) {
		if (StringUtils.isNullOrEmpty(expression)) {
			return false;
		}
		int i = 0;
		char firstChar;
		do {
			firstChar = expression.charAt(i++);
		} while (Character.isWhitespace(firstChar));
		return firstChar == DEFAULT_EXPRESSION_ID || firstChar == LAZY_EXPRESSION_ID;
	}

	/**
	 * Gets the eval cache.
	 *
	 * @param context
	 *            the context
	 * @return the eval cache
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Serializable> getEvalCache(ExpressionContext context) {
		Map<String, Serializable> map = (Map<String, Serializable>) context.get(EVALUATION_CACHE);
		if (map == null) {
			map = new LinkedHashMap<>();
			context.put(EVALUATION_CACHE, (Serializable) map);
		}
		return map;
	}
}
