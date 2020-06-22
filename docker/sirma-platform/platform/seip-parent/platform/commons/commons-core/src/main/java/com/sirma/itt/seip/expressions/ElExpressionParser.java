package com.sirma.itt.seip.expressions;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;

/**
 * Parses EL expressions.
 *
 * @author BBonev
 */
public class ElExpressionParser {

	public static final char DEFAULT_EXPRESSION_ID = '$';
	public static final char LAZY_EXPRESSION_ID = '#';

	private static final char CLOSE_EXP = '}';
	private static final char OPEN_EXP = '{';

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

	@SuppressWarnings("squid:S3776")
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
				int old = openBrackets;
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
	 * Checks if is expression. The given string is an expression when the first non white space character is one of the
	 * {@link #DEFAULT_EXPRESSION_ID} or {@link #LAZY_EXPRESSION_ID}.
	 *
	 * @param expression
	 *            the expression to check
	 * @return true, if is expression
	 */
	public static boolean isExpression(String expression) {
		if (StringUtils.isBlank(expression)) {
			return false;
		}
		int i = 0;
		char firstChar;
		do {
			firstChar = expression.charAt(i++);
		} while (Character.isWhitespace(firstChar));
		return firstChar == DEFAULT_EXPRESSION_ID || firstChar == LAZY_EXPRESSION_ID;
	}


}
