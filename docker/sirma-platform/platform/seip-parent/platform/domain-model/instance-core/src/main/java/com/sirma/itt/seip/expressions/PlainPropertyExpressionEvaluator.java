package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

/**
 * Evaluates expression to plain text with escaped html characters.
 *
 * @author smustafov
 */
public class PlainPropertyExpressionEvaluator extends PropertyExpressionEvaluator {

	private static final long serialVersionUID = -156256114629723071L;
	private static final String EXPRESSION_ID = "getPlain";
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			EXPRESSION_START + "\\{" + EXPRESSION_ID + "\\((\\[[\\w:]+\\])\\s*,?\\s*(?:cast\\((.+?)\\s*as\\s*(.+?)\\)|(.*?))\\)"
					+ FROM_PATTERN + MATCHES + "\\}");

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return EXPRESSION_ID;
	}

	@Override
	protected Serializable escapeConditional(Serializable valueToEscape, boolean escape) {
		if (valueToEscape instanceof String) {
			return Jsoup.parse((String) valueToEscape).text();
		}
		return valueToEscape;
	}

}
