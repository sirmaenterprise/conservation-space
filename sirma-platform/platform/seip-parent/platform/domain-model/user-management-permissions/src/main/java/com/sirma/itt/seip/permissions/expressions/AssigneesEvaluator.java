package com.sirma.itt.seip.permissions.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluator;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.permissions.PermissionService;

/**
 * Expression that returns the assignee users to the current instance.
 *
 * @author BBonev
 */
@Singleton
public class AssigneesEvaluator extends BaseEvaluator {
	private static final long serialVersionUID = 753070861787488965L;

	private static final Pattern PATTERN = Pattern.compile(EXPRESSION_START + "\\{assignees\\}");

	@Inject
	private PermissionService permissionService;

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "assignees";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		return permissionService
				.getPermissionAssignments(((Instance) getCurrentInstance(context, values)).toReference())
					.size();
	}

}
