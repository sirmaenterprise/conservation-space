package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Evaluator for retrieving previously set variables. If a variable has not been set a {@link RuntimeException} is
 * thrown.
 *
 * @author yasko
 */
@Singleton
public class VariableGetEvaluator extends VariableEvaluator {
	private static final long serialVersionUID = -8856379398543960034L;

	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{var\\.(\\w+)\\}");

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext ctx, Serializable... values) {
		String name = VAR_PREFIX + matcher.group(1);
		if (!ctx.containsKey(name)) {
			throw new EmfRuntimeException("Variable " + name + " is not defined");
		}
		return ctx.getIfSameType(name, String.class);
	}

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}
}
