package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator for setting a variable. The evaluate method does not return the set value.
 *
 * @author yasko
 */
@Singleton
public class VariableSetEvaluator extends VariableEvaluator {
	private static final long serialVersionUID = -6992551762082434550L;

	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{var\\.(\\w+)\\s*=\\s*(.*)\\}",
			Pattern.DOTALL);

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext ctx, Serializable... values) {
		String name = VAR_PREFIX + matcher.group(1);
		String value = matcher.group(2);
		ctx.put(name, value);
		return "";
	}

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}
}
