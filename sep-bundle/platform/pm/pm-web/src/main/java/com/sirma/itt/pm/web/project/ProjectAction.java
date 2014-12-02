package com.sirma.itt.pm.web.project;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.menu.NavigationMenu;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.NullInstance;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.emf.web.action.event.EmfImmediateActionEvent;
import com.sirma.itt.emf.web.menu.main.event.MainMenuEvent;
import com.sirma.itt.emf.web.menu.main.event.SelectedMainMenu;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * Project instance processing manager.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class ProjectAction extends ProjectLandingPage {

	private static final long serialVersionUID = 2135275835545918747L;

	private static final String EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION = "Executing observer for immediate operation ProjectAction.";

	@Inject
	private ProjectService projectService;

	@Inject
	private AuthorityService authorityService;

	/** The render members menu is cached flag whether to be rendered the menu or not. */
	private Boolean renderMembersMenu;

	/**
	 * Observer for create action.
	 * 
	 * @param event
	 *            The event payload object.
	 */
	public void mainMenuCreateProject(
			@Observes @SelectedMainMenu(PmActionTypeConstants.PROJECTINSTANCE) final MainMenuEvent event) {
		log.debug("Executing observer ProjectAction.mainMenuCreateProject");
		createProject(PmActionTypeConstants.CREATE_PROJECT);
	}

	/**
	 * Action button create project.
	 * 
	 * @param event
	 *            the event
	 */
	public void actionButtonCreateProject(
			@Observes @EMFAction(value = PmActionTypeConstants.CREATE_PROJECT, target = NullInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction.actionButtonCreateProject");
		createProject(event.getActionId());
	}

	/**
	 * This method will catch <b>create object</b> action, apply the needed data and navigate to
	 * object landing page, where we can create object under project level.
	 * 
	 * @param event
	 *            holds data for current operation
	 */
	public void createObjectForProject(
			@Observes @EMFAction(value = PmActionTypeConstants.CREATE_OBJECT, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction.createObjectForProject");
		Instance objectInstance = event.getInstance();
		if (objectInstance != null) {
			initContextForInstanceEdit(objectInstance);
		}
		event.setNavigation(PmNavigationConstants.NAVIGATE_OBJECT_PAGE);
	}

	/**
	 * This method will catch <b>attach object</b> action, will set needed data for the object
	 * picker.
	 * 
	 * @param event
	 *            holds data for current operation
	 */
	public void attachObjectToProject(
			@Observes @EMFAction(value = PmActionTypeConstants.ATTACH_OBJECT, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction.attachObjectToProject");
		getDocumentContext().addInstance(event.getInstance());
	}

	/**
	 * Open project profile navigation menu observer.
	 * 
	 * @param navigationEvent
	 *            the navigation event
	 */
	public void navigationMenuOpenProjectProfileObserver(
			@Observes @NavigationMenu(PmNavigationConstants.EDIT_PROJECT) NavigationMenuEvent navigationEvent) {
		log.debug("PMWeb: Executing observer ProjectAction.openProjectProfileNavigationMenuObserver");
	}

	/**
	 * Creates the project.
	 * 
	 * @param actionId
	 *            the action id
	 */
	private void createProject(String actionId) {
		getDocumentContext().setCurrentOperation(ProjectInstance.class.getSimpleName(), actionId);
	}

	/**
	 * Creates the standalone task in project.
	 * 
	 * @param event
	 *            the event
	 */
	public void createStandaloneTaskInProject(
			@Observes @EMFAction(value = ActionTypeConstants.CREATE_TASK, target = ProjectInstance.class) EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.CREATE_TASK);
		Instance instance = event.getInstance();
		initContextForInstanceEdit(instance);

		// clear task data from context if any
		getDocumentContext().put(StandaloneTaskInstance.class.getSimpleName(), null);
		getDocumentContext().put(TaskDefinition.class.getSimpleName(), null);

		event.setNavigation(NavigationConstants.STANDALONE_TASK_DETAILS_PAGE);
	}

	/**
	 * Edit project details action observer.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void edit(
			@Observes @EMFAction(value = PmActionTypeConstants.EDIT_DETAILS, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.EDIT_DETAILS);
		ProjectInstance selectedInstance = (ProjectInstance) event.getInstance();
		if (selectedInstance != null) {
			ProjectDefinition definition = (ProjectDefinition) dictionaryService
					.getInstanceDefinition(selectedInstance);
			getDocumentContext().populateContext(selectedInstance, ProjectDefinition.class,
					definition);
			getDocumentContext().setRootInstance(selectedInstance);
			setSelectedType(selectedInstance.getIdentifier());
			event.setNavigation(PmNavigationConstants.PROJECT);
			getDocumentContext().setFormMode(FormViewMode.EDIT);
		}
	}

	/**
	 * Approve project.
	 * 
	 * @param event
	 *            the event
	 */
	public void approve(
			@Observes @EMFAction(value = PmActionTypeConstants.APPROVE, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.APPROVE);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * (Immediate operation observer) Approve project.
	 * 
	 * @param event
	 *            the event
	 */
	public void approveImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.APPROVE, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.APPROVE);
		event.setHandled(true);
	}

	/**
	 * Complete project.
	 * 
	 * @param event
	 *            the event
	 */
	public void complete(
			@Observes @EMFAction(value = PmActionTypeConstants.COMPLETE, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.COMPLETE);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * (Immediate operation observer)Complete project.
	 * 
	 * @param event
	 *            the event
	 */
	public void completeImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.COMPLETE, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.COMPLETE);
		event.setHandled(true);
	}

	/**
	 * Delete project.
	 * 
	 * @param event
	 *            the event
	 */
	public void delete(
			@Observes @EMFAction(value = PmActionTypeConstants.DELETE, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.DELETE);
		projectService.delete((ProjectInstance) event.getInstance(), new Operation(
				ActionTypeConstants.DELETE), false);
		initInstanceOperation(event, PmNavigationConstants.NAVIGATE_USER_DASHBOARD, null, false);
		getDocumentContext().clear();
	}

	/**
	 * (Immediate operation observer)Delete project.
	 * 
	 * @param event
	 *            the event
	 */
	public void deleteImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.DELETE, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.DELETE);
		event.setHandled(true);
	}

	/**
	 * Manage relations.
	 * 
	 * @param event
	 *            the event
	 */
	public void manageRelations(
			@Observes @EMFAction(value = PmActionTypeConstants.MANAGE_RELATIONS, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.MANAGE_RELATIONS);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * Manage resources.
	 * 
	 * @param event
	 *            the event
	 */
	public void manageResources(
			@Observes @EMFAction(value = PmActionTypeConstants.MANAGE_RESOURCES, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.MANAGE_RESOURCES);
		getDocumentContext().setRootInstance(event.getInstance());
		getDocumentContext().addInstance(event.getInstance());
		event.setNavigation(PmNavigationConstants.MANAGE_RESOURCES);
		// initInstanceOperation(event, PmNavigationConstants.MANAGE_RESOURCES, null, true);
	}

	/**
	 * Restart project.
	 * 
	 * @param event
	 *            the event
	 */
	public void restart(
			@Observes @EMFAction(value = PmActionTypeConstants.RESTART, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.RESTART);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * (Immediate operation observer)Restart project.
	 * 
	 * @param event
	 *            the event
	 */
	public void restartImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.RESTART, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.RESTART);
		event.setHandled(true);
	}

	/**
	 * Start project.
	 * 
	 * @param event
	 *            the event
	 */
	public void start(
			@Observes @EMFAction(value = PmActionTypeConstants.START, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.START);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * (Immediate operation observer)Start project.
	 * 
	 * @param event
	 *            the event
	 */
	public void startImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.START, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.START);
		event.setHandled(true);
	}

	/**
	 * Stop project.
	 * 
	 * @param event
	 *            the event
	 */
	public void stop(
			@Observes @EMFAction(value = PmActionTypeConstants.STOP, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.STOP);
		projectService.cancel((ProjectInstance) event.getInstance());
		initInstanceOperation(event, null, null, false);
	}

	/**
	 * (Immediate operation observer) Stop project.
	 * 
	 * @param event
	 *            the event
	 */
	public void stopImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.STOP, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.STOP);
		event.setHandled(true);
	}

	/**
	 * Suspend project.
	 * 
	 * @param event
	 *            the event
	 */
	public void suspend(
			@Observes @EMFAction(value = PmActionTypeConstants.SUSPEND, target = ProjectInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer ProjectAction." + PmActionTypeConstants.SUSPEND);
		initInstanceOperation(event, null, null, true);
	}

	/**
	 * (Immediate operation observer)Suspend project.
	 * 
	 * @param event
	 *            the event
	 */
	public void suspendImmediate(
			@Observes @EMFAction(value = PmActionTypeConstants.SUSPEND, target = ProjectInstance.class) final EmfImmediateActionEvent event) {
		log.debug(EXECUTING_OBSERVER_FOR_IMMEDIATE_OPERATION_PROJECT_ACTION
				+ PmActionTypeConstants.SUSPEND);
		event.setHandled(true);
	}

	/**
	 * Render members menu.
	 * 
	 * @return true, if successful
	 */
	public Boolean renderMembersMenu() {
		if (!isNewInstance()) {
			renderMembersMenu = authorityService.isActionAllowed(
					getDocumentContext().getInstance(getInstanceClass()),
					PmActionTypeConstants.MANAGE_RESOURCES, "");
		} else {
			renderMembersMenu = Boolean.FALSE;
		}
		return renderMembersMenu;
	}

}
