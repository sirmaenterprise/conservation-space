package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluates AND expressions.
 * 
 * @author BBonev
 */
@Singleton
public class AndExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = 1902725440217374509L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{and\\(((?:true|false|TRUE|FALSE|\\s+|and|AND|not|NOT)+)\\)\\}");

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*and\\s*|\\s+");

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "and";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String group = matcher.group(1);
		String[] split = SPLIT_PATTERN.split(group.toLowerCase());
		boolean result = true;
		boolean not = false;
		for (String string : split) {
			if (!string.isEmpty()) {
				if ("not".equals(string)) {
					// next value will be negative
					not = true;
					continue;
				}
				Boolean valueOf = Boolean.valueOf(string);
				result &= evalNot(valueOf.booleanValue(), not);
				// reset negative
				not = false;
			}
		}
		return Boolean.valueOf(result);
	}

	/**
	 * Evaluates not.
	 *
	 * @param booleanValue
	 *            the boolean value
	 * @param not
	 *            the not
	 * @return true, if successful
	 */
	private static boolean evalNot(boolean booleanValue, boolean not) {
		if (not) {
			return !booleanValue;
		}
		return booleanValue;
	}

}
