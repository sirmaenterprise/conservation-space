package com.sirma.cmf.web.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;

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
		if (StringUtils.isNotNullOrEmpty(instanceKey) && context.containsKey(instanceKey)) {
			Instance instance = (Instance) context.get(instanceKey);
			Object plannedEndTime = instance.getProperties().get(DefaultProperties.PLANNED_END_DATE);
			if (plannedEndTime == null) {
				Instance taskContext = InstanceUtil.getDirectParent(instance);
				if (taskContext != null) {
					plannedEndTime = taskContext.getProperties().get(DefaultProperties.PLANNED_END_DATE);
				}
			}
			return converter.convert(String.class, plannedEndTime);
		}
		return null;
	}

}
