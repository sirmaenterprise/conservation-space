package com.sirma.cmf.web.standaloneTask;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.cmf.web.workflow.transition.TaskTransitionAction;
import com.sirma.cmf.web.workflow.transition.TransitionsBuilder;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOpenEvent;
import com.sirma.itt.cmf.security.evaluator.StandaloneTaskRoleEvaluator;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Landing page implementation for standalone tasks.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class StandaloneTaskLandingPage extends
		InstanceLandingPage<StandaloneTaskInstance, TaskDefinition> implements Serializable {

	private static final long serialVersionUID = 1027132744951014621L;

	@Inject
	protected StandaloneTaskService standaloneTaskService;

	private List<TaskTransitionAction> transitionActions;

	@Inject
	private TransitionsBuilder operationsBuilder;

	@Inject
	private AuthorityService authorityService;

	@Override
	public Class<TaskDefinition> getInstanceDefinitionClass() {
		return TaskDefinition.class;
	}

	@Override
	public StandaloneTaskInstance getNewInstance(TaskDefinition selectedDefinition, Instance context) {
		return standaloneTaskService.createInstance(selectedDefinition, context);
	}

	@Override
	public Class<StandaloneTaskInstance> getInstanceClass() {
		return StandaloneTaskInstance.class;
	}

	@Override
	public InstanceReference getParentReference() {
		StandaloneTaskInstance instance = getDocumentContext().getInstance(getInstanceClass());
		return instance.getOwningReference();
	}

	@Override
	public String cancelEditInstance(StandaloneTaskInstance taskInstance) {
		return updateContextInstance(taskInstance);
	}

	@Override
	public String saveInstance(StandaloneTaskInstance instance) {
		if (!SequenceEntityGenerator.isPersisted(instance)) {
			standaloneTaskService.start(instance, new Operation(ActionTypeConstants.CREATE_TASK));
		} else {
			standaloneTaskService.save(instance, createOperation());
		}

		String navigation = NavigationConstants.RELOAD_PAGE;

		eventService.fire(new StandaloneTaskOpenEvent(instance));

		return navigation;
	}

	/**
	 * Update context and determine navigation rule. TODO: review the code after navigation
	 * re-factoring.
	 * 
	 * @param instance
	 *            current instance
	 * @return navigation rule
	 */
	private String updateContextInstance(StandaloneTaskInstance instance) {
		String navigation = NavigationConstants.BACKWARD;
		return navigation;
	}

	@Override
	public void onExistingInstanceInitPage(StandaloneTaskInstance instance) {

		safeClearTransitions();

		// render transition buttons only if wf is active
		if (instance.isEditable() && (getFormViewMode(instance) == FormViewMode.EDIT)) {

			// - get the transitions from the task
			TaskDefinition definition = getDocumentContext().getDefinition(
					getInstanceDefinitionClass());
			List<TransitionDefinition> transitions = loadTransitions(definition);

			// - pass the transitions to transitions builder to generate
			// the operation actions to be used in command button in the
			// page
			transitionActions = operationsBuilder.build(transitions);
			
			getDocumentContext().setCurrentOperation(StandaloneTaskInstance.class.getSimpleName(), ActionTypeConstants.EDIT_DETAILS);
		}
	}

	/**
	 * Load transitions for the task.
	 * 
	 * @param definition
	 *            the definition
	 * @return Found transitions.
	 */
	protected List<TransitionDefinition> loadTransitions(TaskDefinition definition) {
		return DefinitionUtil.getDefaultTransitions(definition);
	}

	@Override
	public void onNewInstanceInitPage(StandaloneTaskInstance instance) {
		safeClearTransitions();
	}

	@Override
	public FormViewMode getFormViewModeExternal(StandaloneTaskInstance instance) {
		Set<Action> allowedActions = authorityService.getAllowedActions(instance, "");
		if (allowedActions.contains(StandaloneTaskRoleEvaluator.EDIT)) {
			return FormViewMode.EDIT;
		}
		return FormViewMode.PREVIEW;
	}

	/**
	 * Help method for clearing the transitions, prevent NPE Exception.
	 */
	public void safeClearTransitions() {
		// check for if there are transitions
		if (transitionActions != null) {
			transitionActions.clear();
		}
	}

	/**
	 * Removes the task data from context.
	 */
	protected void removeFromContext() {
		// clear old StandAlone task from context if any
		getDocumentContext().put(getInstanceClass().getSimpleName(), null);
		getDocumentContext().put(getInstanceDefinitionClass().getSimpleName(), null);
	}

	@Override
	public String getNavigationString() {
		return NavigationConstants.STANDALONE_TASK_DETAILS_PAGE;
	}

	/**
	 * Getter method for transitionActions.
	 * 
	 * @return the transitionActions
	 */
	public List<TaskTransitionAction> getTransitionActions() {
		return transitionActions;
	}

	/**
	 * Setter method for transitionActions.
	 * 
	 * @param transitionActions
	 *            the transitionActions to set
	 */
	public void setTransitionActions(List<TaskTransitionAction> transitionActions) {
		this.transitionActions = transitionActions;
	}

	@Override
	protected String getDefinitionFilterType() {
		return ObjectTypesCmf.STANDALONE_TASK;
	}

	@Override
	protected InstanceService<StandaloneTaskInstance, TaskDefinition> getInstanceService() {
		return standaloneTaskService;
	}

}
