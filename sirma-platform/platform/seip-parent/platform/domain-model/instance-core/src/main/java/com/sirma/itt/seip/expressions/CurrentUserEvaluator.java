package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Evaluator expression that handles the current user fetching.
 *
 * @author BBonev
 */
public class CurrentUserEvaluator extends UserPropertiesEvaluator {

	private static final long serialVersionUID = 1129558861631498869L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START + "\\{currentUser\\.?([\\w:]+)?\\}");

	@Inject
	private SecurityContext securityContext;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "currentUser";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		User currentUser = securityContext.getAuthenticated();
		String property = matcher.group(1);
		Resource loadedUser = resourceService.findResource(currentUser);
		if (loadedUser == null) {
			// user was not found (probably system user).
			// added the check because noticed an exception during system user insert and probably this is linked
			// but could happen not only for system user
			return null;
		}
		return escape(getUserData(loadedUser, property));
	}

}
