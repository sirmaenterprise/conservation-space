package com.sirma.itt.seip.instance.save.expression.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;

/**
 * Evaluator for duedate expression that extracts the duedate according to the current instance passed that can be task
 * or workflow. Extracted date is returned as result or null otherwise.
 *
 * @author svelikov
 */
@Singleton
public class DueDateExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -1100861079872842546L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{duedate\\(([\\w]+)\\)\\}");

	@Inject
	private InstanceContextService contextService;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "duedate";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String instanceKey = matcher.group(1);
		if (StringUtils.isNotBlank(instanceKey) && context.containsKey(instanceKey)) {
			Instance instance = (Instance) context.get(instanceKey);
			Object plannedEndTime = instance.getProperties().get(DefaultProperties.PLANNED_END_DATE);
			if (plannedEndTime == null) {
				Instance taskContext = contextService.getContext(instance).map(InstanceReference::toInstance).orElse(
						null);
				if (taskContext != null) {
					plannedEndTime = taskContext.getProperties().get(DefaultProperties.PLANNED_END_DATE);
				}
			}
			return converter.convert(String.class, plannedEndTime);
		}
		return null;
	}

}
