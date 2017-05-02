package com.sirma.cmf.web.actionsmanager;

import java.io.Serializable;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.security.action.ActionTypeBinding;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.NullInstance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * All statefull instance actions goes trough this manager.
 *
 * @author svelikov
 */
@Named
@SessionScoped
public class ActionsManager extends com.sirma.cmf.web.Action implements Serializable {

	private static final long serialVersionUID = -4100299582266298139L;

	@Inject
	private Event<EMFActionEvent> allowedActionEvent;

	@Inject
	private StateTransitionManager transitionManager;

	@Inject
	private StateService stateService;

	@Inject
	protected InstanceContextInitializer instanceContextInitializer;

	/**
	 * Context actions executor. Allowed action name which is provided is used to build and fire cdi event with dynamic
	 * selection of the event qualifier.
	 * <p>
	 * Use the method with context instance.
	 * </p>
	 *
	 * @param name
	 *            The action name.
	 * @param initiatedOn
	 *            Navigation string of the page where this action was initiated. This is needed when is needed to
	 *            redirect to the same page later.
	 * @return Navigation string.
	 */
	public String executeContextAction(final String name, final String initiatedOn) {
		return executeContextAction(name, initiatedOn, null);
	}

	/**
	 * Execute context action passing current context instance with the event. <br />
	 * REVIEW: This is used only for create case with no context (project) and for create project operations and should
	 * be removed when we have possibility to have these operations from evaluator.
	 *
	 * @param actionId
	 *            the action
	 * @param initiatedOn
	 *            the initiated on
	 * @param context
	 *            the context
	 * @return navigation string.
	 */
	public String executeContextAction(final String actionId, final String initiatedOn, Instance context) {
		TimeTracker timer = TimeTracker.createAndStart();
		log.debug("Started operation [" + actionId + "] initiated on [" + initiatedOn + "] context [" + context + "]");
		getDocumentContext().addContextInstance(context);
		// we don't have action for these dummy action buttons like 'create case' and 'create
		// project'
		getDocumentContext().clearSelectedAction();
		EMFActionEvent event = fireActionEvent(null, actionId, context, initiatedOn);
		setCurrentOperation(context, actionId);
		log.debug("Operation [ " + actionId + "] took " + timer.stopInSeconds() + " s");
		return event.getNavigation();
	}

	/**
	 * Execute allowed action.
	 *
	 * @param action
	 *            the action
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	public String executeAllowedAction(Action action, Instance instance) {
		if (instance != null && action != null) {
			TimeTracker timer = TimeTracker.createAndStart();
			log.debug("Started operation [" + action + "] for instance type[" + instance.getClass().getSimpleName()
					+ "] and id[" + instance.getId() + "]");

			Instance freshInstance = fetchInstance(instance.getId(), instance.getClass().getSimpleName().toLowerCase());

			String actionId = action.getActionId();
			setCurrentOperation(freshInstance, actionId);
			getDocumentContext().setSelectedAction(action);
			instanceContextInitializer.restoreHierarchy(freshInstance);
			EMFActionEvent event = fireActionEvent(action, actionId, freshInstance, null);
			log.debug("Operation [" + action + "] took " + timer.stopInSeconds() + " s");

			// Update instance in context if already there. On dashboards for example we may not have the instance for
			// which this action is executed in document context. For deleted instances, we don't want to re-apply them
			// in the context again. When the current instance is same as fresh there is no need to init the context
			if (action.isImmediateAction() && !ActionTypeConstants.DELETE.equals(actionId)
					&& freshInstance.equals(getDocumentContext().getCurrentInstance())) {
				initContextForImmediateAction(freshInstance, event.getInstance());
			}

			return event.getNavigation();
		}
		log.warn("Can not execute operation [" + action + "] for null instance!");
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Inits the context for immediate action.
	 *
	 * @param instance
	 *            the instance
	 * @param updatedInstance
	 *            the instance
	 */
	private void initContextForImmediateAction(Instance instance, Instance updatedInstance) {
		if (instance != null && updatedInstance != null) {
			// add updated current instance in context
			getDocumentContext().updateCurrentInstance(updatedInstance);
			// add context instance needed for the the new instance to be initialized
			getDocumentContext().updateContextInstance(updatedInstance);
		}
	}

	/**
	 * Calculate form view mode according to whether there are uncompleted required instance properties.
	 *
	 * @param instance
	 *            the instance
	 * @param actionId
	 *            the action id
	 */
	protected void calculateFormViewMode(Instance instance, String actionId) {
		Set<String> requiredFields = transitionManager.getRequiredFields(instance,
				stateService.getPrimaryState(instance), actionId);
		FormViewMode effectiveFormMode = FormViewMode.PREVIEW;
		boolean containsAll = instance.getProperties().keySet().containsAll(requiredFields);
		if (!containsAll) {
			effectiveFormMode = FormViewMode.EDIT;
		}
		getDocumentContext().setFormMode(effectiveFormMode);
	}

	/**
	 * Fire action event.
	 *
	 * @param action
	 *            the action
	 * @param actionId
	 *            the action id
	 * @param instance
	 *            the instance
	 * @param navigation
	 *            the navigation
	 * @return the cMF action event
	 */
	protected EMFActionEvent fireActionEvent(Action action, String actionId, Instance instance, String navigation) {
		EMFActionEvent event = new EMFActionEvent(instance, navigation, actionId, action);

		Class<?> target = NullInstance.class;
		if (instance != null) {
			target = instance.getClass();
		}

		ActionTypeBinding binding = new ActionTypeBinding(actionId, target);
		allowedActionEvent.select(binding).fire(event);
		return event;
	}

	/**
	 * Calculate onclick attribute for action buttons. If onclick attribute is set, then its value is used. Otherwise if
	 * confirmation attribute is set, then it will be applied.
	 *
	 * @param action
	 *            the action
	 * @return the onclick attribute as string
	 */
	public String calculateOnclickAttribute(Action action) {
		StringBuilder onclick = new StringBuilder();
		if (StringUtils.isNotNullOrEmpty(action.getOnclick())) {
			onclick.append(action.getOnclick());
		} else {
			if (StringUtils.isNotNullOrEmpty(action.getConfirmationMessage())) {
				onclick
						.append("return CMF.utilityFunctions.riseConfirmation('")
							.append(action.getConfirmationMessage())
							.append("', ")
							.append(RichFunction.component("confirmationPopup"))
							.append(")");
			}
		}
		return onclick.toString();
	}

	/**
	 * Gets the action style class for the action buttons in ui.
	 *
	 * @param buttonClass
	 *            the button class
	 * @param currentInstance
	 *            the current instance
	 * @param action
	 *            the action
	 * @param compactMode
	 *            the compact mode
	 * @return the action style class
	 */
	public String getActionStyleClass(String buttonClass, Instance currentInstance, Action action, String compactMode) {
		StringBuilder builder = new StringBuilder();
		if (currentInstance != null) {
			builder.append(currentInstance.getClass().getSimpleName().toLowerCase());
		}
		if (StringUtils.isNotNullOrEmpty(buttonClass)) {
			builder.append(" ").append(buttonClass);
		} else {
			builder.append(" allowed-action-button");
		}
		if (action != null && action.getActionId() != null) {
			builder.append(" ").append(action.getActionId());
		}
		if (StringUtils.isNotNullOrEmpty(compactMode)) {
			builder.append(" has-tooltip");
		}
		return builder.toString().trim();
	}
}
