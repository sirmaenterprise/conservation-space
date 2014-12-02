package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;

/**
 * Evaluator expression that handles the current user fetching.
 * 
 * @author BBonev
 */
public class CurrentUserEvaluator extends UserPropertiesEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1129558861631498869L;

	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{currentUser\\.?([\\w]+)?\\}");

	@Inject
	private Instance<AuthenticationService> service;

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
		User currentUser = null;
		try {
			currentUser = service.get().getCurrentUser();
		} catch (ContextNotActiveException e) {
			currentUser = SecurityContextManager.getFullAuthentication();
			logger.trace("Context not active returing user from thread local", e);
		}
		if (currentUser == null) {
			return null;
		}
		String property = matcher.group(1);
		return getUserData(currentUser, property);
	}

}
