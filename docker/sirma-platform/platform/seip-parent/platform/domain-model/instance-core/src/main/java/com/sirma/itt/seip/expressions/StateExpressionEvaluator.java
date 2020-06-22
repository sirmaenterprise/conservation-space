package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;

/**
 * Extract the state field from the instance (currently only task instances have this field). Extracted field is
 * compared to the second argument to check if the instance is in that state or not.
 *
 * @author svelikov
 */
@Singleton
public class StateExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -3932064353645457296L;

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{checkState\\(([\\w]+)(,[\\w]+)?\\)}");

	@Inject
	private StateService stateService;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "checkState";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String instanceKey = matcher.group(1);
		String state = matcher.group(2);
		if (StringUtils.isNotBlank(instanceKey) && context.containsKey(instanceKey)) {
			Instance instance = (Instance) context.get(instanceKey);
			return Boolean.valueOf(stateService.isInState(PrimaryStates.from(state), instance));
		}
		return Boolean.FALSE;
	}

}