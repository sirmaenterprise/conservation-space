package com.sirma.cmf.web.actionsmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.web.config.InstanceProvider;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Common logic for actions loading.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ActionsProvider {

	private static final Logger LOG = LoggerFactory.getLogger(ActionsProvider.class);

	@Inject
	private ActionContext actionContext;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private InstanceProvider instanceProvider;

	@Inject
	protected InstanceContextInitializer instanceContextInitializer;

	private TimeTracker timeTracker;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private DocumentContext documentContext;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		timeTracker = new TimeTracker();
	}

	/**
	 * Evaluate actions by instance.
	 *
	 * @param instance
	 *            the instance
	 * @param placeholder
	 *            the placeholder
	 * @param context
	 *            the context
	 * @return the list
	 */
	public List<Action> evaluateActionsByInstance(Instance instance, String placeholder, Instance context) {
		if (instance == null || StringUtils.isNullOrEmpty(placeholder)) {
			LOG.warn("Can't evaluate actions for null instance ot missing placeholder!");
			return CollectionUtils.emptyList();
		}
		return evaluateActions(instance.getId(), instance.getClass().getSimpleName().toLowerCase(), placeholder,
				context);
	}

	/**
	 * Evaluate actions.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param placeholder
	 *            the placeholder
	 * @param context
	 *            the context
	 * @return the list
	 */
	private List<Action> evaluateActions(Serializable instanceId, String instanceType, String placeholder,
			Instance context) {
		if (instanceId == null || StringUtils.isNullOrEmpty(instanceType) || StringUtils.isNullOrEmpty(placeholder)) {
			LOG.warn("Can't evaluate actions for null instanceId or instanceType!");
			return CollectionUtils.emptyList();
		}

		timeTracker.begin();

		List<Action> actions = new LinkedList<>();
		Instance target = instanceProvider.fetchInstance(instanceId, instanceType);
		populateParent(context, target);

		if (target != null) {
			actions = getActions(target, placeholder);
			// put action target instance in context for access inside an action button template
			actionContext.setActionTraget(target);
		} else {
			// TODO: if target is null, should refresh the page somehow
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading [{}] actions for instance type[{}] with id[{}] for user[{}] took {} s", actions.size(),
					instanceType, instanceId, securityContext.getAuthenticated().getDisplayName(),
					timeTracker.stopInSeconds());
		}

		if (actions.isEmpty()) {
			actions.add(new EmfAction(ActionTypeConstants.NO_PERMISSIONS));
			LOG.debug("User [{}] doesn't have permissions for instance type[{}] with id[{}]",
					securityContext.getAuthenticated().getDisplayName(), instanceType, instanceId);
		}

		return actions;
	}

	/**
	 * Populate parent. It is used DocumentContext getContextIntnace() and getRootInstance() to check if we need to
	 * initialize context for the instance. If either of both method returns not null value, the context for the
	 * instance will be initialize.
	 *
	 * @param context
	 *            the context
	 * @param instance
	 *            the instance
	 */
	private void populateParent(Instance context, Instance instance) {
		Instance contextInstance = documentContext.getContextInstance();
		Instance rootInstance = documentContext.getRootInstance();
		if (rootInstance != null || contextInstance != null) {
			instanceContextInitializer.restoreHierarchy(instance);
		}
	}

	/**
	 * Gets instance actions actions.
	 *
	 * @param instance
	 *            the instance
	 * @param placeholder
	 *            the placeholder
	 * @return the actions
	 */
	private List<Action> getActions(Instance instance, String placeholder) {
		Set<Action> allowedActions = authorityService.getAllowedActions(instance, placeholder);
		if (LOG.isTraceEnabled()) {
			LOG.debug("Found actions:{}\nfor instance: {}", allowedActions, instance);
		}
		return new ArrayList<>(allowedActions);
	}
}
