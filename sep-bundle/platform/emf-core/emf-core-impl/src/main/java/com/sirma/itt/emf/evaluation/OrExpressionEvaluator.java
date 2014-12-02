package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author BBonev
 */
public class OrExpressionEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6334446977467789167L;

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{or\\(((?:true|false|TRUE|FALSE|\\s+|or|OR|not|NOT)+)\\)\\}");

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*or\\s*|\\s+");
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
		String group = matcher.group(1);
		String[] split = SPLIT_PATTERN.split(group.toLowerCase());
		boolean result = false;
		boolean not = false;
		for (String string : split) {
			if (!string.isEmpty()) {
				if ("not".equals(string)) {
					// next value will be negative
					not = true;
					continue;
				}
				Boolean valueOf = Boolean.valueOf(string);
				result |= evalNot(valueOf.booleanValue(), not);
				// reset negative
				not = false;
			}
		}
		return Boolean.valueOf(result);
	}

	/**
	 * Eval not.
	 * 
	 * @param booleanValue
	 *            the boolean value
	 * @param not
	 *            the not
	 * @return true, if successful
	 */
	private boolean evalNot(boolean booleanValue, boolean not) {
		if (not) {
			return !booleanValue;
		}
		return booleanValue;
	}

}
