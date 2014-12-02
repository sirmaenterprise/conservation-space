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

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.web.config.InstanceProvider;

/**
 * Common logic for actions loading.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ActionsProvider {

	protected Logger log;
	protected boolean trace;
	protected boolean debug;

	@Inject
	private ActionContext actionContext;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private InstanceProvider instanceProvider;

	private TimeTracker timeTracker;

	// @Inject
	// private InstanceService<Instance, DefinitionModel> instanceService;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		timeTracker = new TimeTracker();
		log = LoggerFactory.getLogger(this.getClass());
		trace = log.isTraceEnabled();
		debug = log.isDebugEnabled();
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
	public List<Action> evaluateActionsByInstance(Instance instance, String placeholder,
			Instance context) {
		if ((instance == null) || StringUtils.isNullOrEmpty(placeholder)) {
			log.warn("Can't evaluate actions for null instance ot missing placeholder!");
			return CollectionUtils.emptyList();
		}
		return evaluateActions(instance.getId(), instance.getClass().getSimpleName().toLowerCase(),
				placeholder, context);
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
	private List<Action> evaluateActions(Serializable instanceId, String instanceType,
			String placeholder, Instance context) {
		if ((instanceId == null) || StringUtils.isNullOrEmpty(instanceType)
				|| StringUtils.isNullOrEmpty(placeholder)) {
			log.warn("Can't evaluate actions for null instanceId or instanceType!");
			return CollectionUtils.emptyList();
		}

		timeTracker.begin();

		Instance target = null;
		List<Action> actions = new LinkedList<Action>();
		// If the context instance is a caseinstance and target instanceType is documentinstance or
		// objectinstance, we try to find the instance inside the context case instance.
		// If target instanceType is not documentinstance or objectinstance or the context is not a
		// caseinstance or the target is not found in the case context, we are going to fetch the
		// target instance from the database.

		// instanceService.refresh(context);
		target = findTargetInContext(context, (String) instanceId, instanceType);

		if (target == null) {
			target = instanceProvider.fetchInstance(instanceId, instanceType);
			// FIXME: context should be resolved truly
			populateParent(context, target);
		}
		
		if (target != null) {
			actions = getActions(target, placeholder);
			// put action target instance in context for access inside an action button template
			actionContext.setActionTraget(target);
		} else {
			// TODO: if target is null, should refresh the page somehow
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Loading actions for instance type[" + instanceType + "] with id["
					+ instanceId + "] took " + timeTracker.stopInSeconds() + " s");
		}
		
		if(actions.isEmpty()){
			actions.add(new EmfAction(ActionTypeConstants.NO_PERMISSIONS));
		}
		
		return actions;
	}

	/**
	 * Populate parent.
	 * 
	 * @param context
	 *            the context
	 * @param instance
	 *            the instance
	 */
	private void populateParent(Instance context, Instance instance) {
		if ((context == null) || (instance == null)) {
			return;
		}
		if ((instance instanceof OwnedModel)
				&& (((OwnedModel) instance).getOwningInstance() == null)) {
			((OwnedModel) instance).setOwningInstance(context);
		}
	}

	/**
	 * Find target in case.
	 * 
	 * @param context
	 *            the context
	 * @param id
	 *            the id
	 * @param instanceType
	 *            the instance type
	 * @return the instance
	 */
	private Instance findTargetInContext(Instance context, String id, String instanceType) {
		if (!"documentinstance".equals(instanceType) && !"objectinstance".equals(instanceType)) {
			return null;
		}
		if (context instanceof CaseInstance) {
			List<SectionInstance> sections = ((CaseInstance) context).getSections();
			for (SectionInstance sectionInstance : sections) {
				if (id.equals(sectionInstance.getId())) {
					return sectionInstance;
				}
				Instance found = findTargetInSection(id, sectionInstance);
				if (found != null) {
					return found;
				}
			}
		} else if (context instanceof SectionInstance) {
			return findTargetInSection(id, (SectionInstance) context);
		}
		return null;
	}

	/**
	 * Find target in section.
	 * 
	 * @param id
	 *            the id
	 * @param sectionInstance
	 *            the section instance
	 * @return the instance or null if not found
	 */
	private Instance findTargetInSection(String id, SectionInstance sectionInstance) {
		for (Instance instance : sectionInstance.getContent()) {
			if (id.equals(instance.getId())) {
				return instance;
			}
		}
		return null;
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
		if (instance instanceof DocumentInstance) {
			if (!((DocumentInstance) instance).hasDocument()) {
				return CollectionUtils.emptyList();
			}
		}
		Set<Action> allowedActions = authorityService.getAllowedActions(instance, placeholder);
		if (log.isTraceEnabled()) {
			log.debug("Found actions:" + allowedActions + "\nfor instance:" + instance);
		}
		return new ArrayList<Action>(allowedActions);
	}

	/**
	 * Load dashlet toolbar actions.
	 * 
	 * @param panel
	 *            the panel
	 * @return the list
	 */
	public List<Action> loadDashletActions(DashboardPanelActionBase panel) {
		List<Action> actions = new ArrayList<Action>();
		if (panel == null) {
			log.warn("Can't evaluate actions for null dashlet!");
			return actions;
		}

		timeTracker.begin();
		Instance targetInstance = panel.dashletActionsTarget();
		String placeholder = panel.targetDashletName();
		if (targetInstance == null) {
			actions.add(new EmfAction(ActionTypeConstants.NO_PERMISSIONS));
			return actions;
		}
		if (panel.getFilterActions()) {
			Set<String> dashletActionIds = panel.dashletActionIds();
			if ((dashletActionIds != null) && !dashletActionIds.isEmpty()) {
				for (String actionId : dashletActionIds) {
					Set<Action> filteredAllowedActions = authorityService.filterAllowedActions(
							targetInstance, placeholder, actionId);
					actions.addAll(filteredAllowedActions);
				}
			}
		} else {
			Set<Action> allowedActions = authorityService.getAllowedActions(targetInstance,
					placeholder);
			actions.addAll(allowedActions);
		}
		if (log.isDebugEnabled()) {
			log.debug("Loading actions for instance type["
					+ targetInstance.getClass().getSimpleName() + "] with id["
					+ targetInstance.getId() + "] took " + timeTracker.stopInSeconds() + " s");
		}
		if(actions.isEmpty()){
			actions.add(new EmfAction(ActionTypeConstants.NO_PERMISSIONS));
		}
		return actions;
	}

}
