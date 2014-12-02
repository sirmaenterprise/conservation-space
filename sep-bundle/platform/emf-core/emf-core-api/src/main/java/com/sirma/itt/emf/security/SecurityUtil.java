package com.sirma.itt.emf.security;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.provider.ProviderRegistry;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.User;

/**
 * Utility class for some common operations involving security and permissions management.
 * 
 * @author BBonev
 */
public class SecurityUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

	/**
	 * Creates a list of actions by name for the given target class.
	 * 
	 * @param target
	 *            the target
	 * @param actionRegistry
	 *            the action registry
	 * @param actions
	 *            the actions
	 * @return the list
	 */
	public static List<Pair<Class<?>, Action>> createActionsList(Class<?> target,
			ProviderRegistry<Pair<Class<?>, String>, Action> actionRegistry, String... actions) {
		List<Pair<Class<?>, Action>> resultActions = new ArrayList<Pair<Class<?>, Action>>(
				actions.length);
		for (String s : actions) {
			Pair<Class<?>, String> key = new Pair<Class<?>, String>(target, s);
			Action action = actionRegistry.find(key);
			if (action != null) {
				resultActions.add(new Pair<Class<?>, Action>(target, action));
			} else {
				LOGGER.warn("Action not found: " + key);
			}
		}
		return resultActions;
	}

	/**
	 * Sets the current user to.
	 * 
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 * @param authenticationService
	 *            the authentication service
	 */
	public static void setCurrentUserTo(PropertyModel model, String key,
			Instance<AuthenticationService> authenticationService) {
		User user = SecurityContextManager.getFullAuthentication();
		if ((user != null) && !SecurityContextManager.isSystemUser(user)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						"Setting current user {} to property {} in {} from SecurityContextManager",
						user.getName(), key, model.getClass().getSimpleName());
			}
			model.getProperties().put(key, user.getName());
			return;
		}
		if (authenticationService.isUnsatisfied() || authenticationService.isAmbiguous()) {
			LOGGER.error("Identity module not installed!");
			return;
		}
		String currentUserId = null;
		try {
			currentUserId = authenticationService.get().getCurrentUserId();
		} catch (ContextNotActiveException e) {
			// if the context is not active we use the user fetched from the current context event
			// it was the System
			// this is only valid for non context calls like via web service
			if (user != null) {
				LOGGER.debug("Not active context will try to set the user from the current thread context (probably System).");
				currentUserId = user.getIdentifier();
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Setting current user {} to property {} in {} from AuthenticationService",
					currentUserId, key, model.getClass().getSimpleName());
		}
		model.getProperties().put(key, currentUserId);
	}
}
