package com.sirma.sep.camunda.expression.evaluators;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Task count evaluator to return active/inactive tasks count for current instance.
 *
 * @author BBonev
 */
@Singleton
public class TaskCountEvaluator extends BaseEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = 4532849536272067602L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{taskCount(?:\\((active|inactive)\\))?\\}");

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "taskCount";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		LOGGER.warn("Using not implemented expression {}", getExpressionId());
		return 0;
	}
}