package com.sirma.itt.pm.web.project;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.emf.web.menu.main.event.MainMenuEvent;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * The Class ProjectActionTest.
 * 
 * @author svelikov
 */
@Test
public class ProjectActionTest extends PMTest {

	private final String currentPage = "dashboard";

	private final ProjectAction action;

	private ProjectInstance projectInstance;

	private DictionaryService dictionaryService;

	private ProjectDefinition projectDefinition;

	private ProjectService projectService;

	private InstanceService instanceService;

	protected boolean isImmediate;

	/**
	 * Instantiates a new project action test.
	 */
	public ProjectActionTest() {
		action = new ProjectAction() {
			private static final long serialVersionUID = 1L;
			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}

			@Override
			protected String getCurrentPageName() {
				return currentPage;
			}

			@Override
			public Set<String> getRequiredFieldsByDefinition(Instance instance, String operation) {
				Set<String> fields = new HashSet<>();
				return fields;
			}

			@Override
			protected boolean hasEmptyRequiredFields(ProjectInstance selectedInstance,
					Set<String> requiredFieldsByDefinition) {
				return isImmediate;
			}

		};

		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");
		projectDefinition = createProjectDefinition("dmsId");

		dictionaryService = Mockito.mock(DictionaryService.class);
		projectService = Mockito.mock(ProjectService.class);
		instanceService = Mockito.mock(InstanceService.class);

		ReflectionUtils.setField(action, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(action, "projectService", projectService);
		ReflectionUtils.setField(action, "instanceService", instanceService);
		ReflectionUtils.setField(action, "log", log);
	}

	/**
	 * Clear context before every test.
	 */
	@BeforeMethod
	public void clearContext() {
		action.getDocumentContext().clear();
		isImmediate = true;
	}

	/**
	 * Check create project conditions.
	 */
	private void checkCreateProjectConditions() {
		assertEquals(PmActionTypeConstants.CREATE_PROJECT, action.getDocumentContext()
				.getCurrentOperation(ProjectInstance.class.getSimpleName()));
	}

	/**
	 * Main menu create project test.
	 */
	public void mainMenuCreateProjectTest() {
		MainMenuEvent mainMenuEvent = new MainMenuEvent(PmActionTypeConstants.CREATE_PROJECT);
		action.mainMenuCreateProject(mainMenuEvent);

		checkCreateProjectConditions();
	}

	/**
	 * Action button create project test.
	 */
	public void actionButtonCreateProjectTest() {
		EMFActionEvent event = createEventObject("", projectInstance,
				PmActionTypeConstants.CREATE_PROJECT, new EmfAction(
						PmActionTypeConstants.CREATE_PROJECT));
		action.actionButtonCreateProject(event);

		checkCreateProjectConditions();
	}

	/**
	 * Navigation menu open project profile observer test.
	 */
	public void navigationMenuOpenProjectProfileObserverTest() {
		NavigationMenuEvent event = new NavigationMenuEvent("",
				PmActionTypeConstants.CREATE_PROJECT);
		action.navigationMenuOpenProjectProfileObserver(event);
	}

	/**
	 * Creates the standalone task in project.
	 */
	public void createStandaloneTaskInProject() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.CREATE_TASK,
				new EmfAction(ActionTypeConstants.CREATE_TASK));
		action.createStandaloneTaskInProject(event);
		// no context instance should be populated in context if none is passed
		assertNull(action.getDocumentContext().getContextInstance());

		event = createEventObject("", projectInstance, ActionTypeConstants.CREATE_TASK,
				new EmfAction(ActionTypeConstants.CREATE_TASK));
		action.createStandaloneTaskInProject(event);
		// we should have context instance populated
		assertEquals(documentContext.getRootInstance(), projectInstance);

		assertEquals(event.getNavigation(), NavigationConstants.STANDALONE_TASK_DETAILS_PAGE);
		assertNull(documentContext.getInstance(StandaloneTaskInstance.class));
		assertNull(documentContext.getDefinition(TaskDefinition.class));
	}

	/**
	 * Edits the.
	 */
	public void edit() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.EDIT_DETAILS,
				new EmfAction(ActionTypeConstants.EDIT_DETAILS));
		action.edit(event);

		// if no instance is passed with the event we should stay on same page and context should be
		// clear
		assertNull(documentContext.getInstance(ProjectInstance.class));
		assertNull(documentContext.getDefinition(ProjectDefinition.class));
		assertEquals(documentContext.getFormMode(), FormViewMode.PREVIEW);
		assertNull(action.getSelectedType());
		assertEquals(event.getNavigation(), "");

		//
		event = createEventObject("", projectInstance, ActionTypeConstants.EDIT_DETAILS,
				new EmfAction(ActionTypeConstants.EDIT_DETAILS));
		Mockito.when(dictionaryService.getInstanceDefinition(projectInstance)).thenReturn(
				projectDefinition);
		action.edit(event);

		assertEquals(documentContext.getInstance(ProjectInstance.class), projectInstance);
		assertEquals(documentContext.getDefinition(ProjectDefinition.class), projectDefinition);
		assertEquals(documentContext.getFormMode(), FormViewMode.EDIT);
		assertEquals(action.getSelectedType(), projectInstance.getIdentifier());
		assertEquals(event.getNavigation(), PmNavigationConstants.PROJECT);
	}

	/**
	 * Check action.
	 * 
	 * @param documentContext
	 *            the document context
	 * @param event
	 *            the event
	 */
	private void checkAction(DocumentContext documentContext, EMFActionEvent event) {
		assertEquals(documentContext.getInstance(ProjectInstance.class), projectInstance);
		assertEquals(documentContext.getFormMode(), FormViewMode.EDIT);
	}

	/**
	 * Check action no instance.
	 * 
	 * @param documentContext
	 *            the document context
	 * @param event
	 *            the event
	 */
	private void checkActionNoInstance(DocumentContext documentContext, EMFActionEvent event) {
		assertNull(documentContext.getInstance(ProjectInstance.class));
		assertEquals(documentContext.getFormMode(), FormViewMode.PREVIEW);
		assertEquals(event.getNavigation(), "");
	}

	/**
	 * Approve.
	 */
	public void approve() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.APPROVE,
				new EmfAction(ActionTypeConstants.APPROVE));
		action.approve(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, ActionTypeConstants.APPROVE, new EmfAction(
				ActionTypeConstants.APPROVE));
		action.approve(event);
		checkAction(documentContext, event);
	}

	/**
	 * Complete.
	 */
	public void complete() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.COMPLETE,
				new EmfAction(ActionTypeConstants.COMPLETE));
		action.complete(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, ActionTypeConstants.COMPLETE, new EmfAction(
				ActionTypeConstants.COMPLETE));
		action.complete(event);
		checkAction(documentContext, event);
	}

	/**
	 * Delete.
	 */
	public void delete() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, ActionTypeConstants.DELETE,
				new EmfAction(ActionTypeConstants.DELETE));
		action.delete(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, ActionTypeConstants.DELETE, new EmfAction(
				ActionTypeConstants.DELETE));
		isImmediate = true;
		Mockito.when(
				instanceService.save(Mockito.any(ProjectInstance.class),
						Mockito.any(Operation.class))).thenReturn(projectInstance);
		isImmediate = false;
		action.delete(event);
		assertNull(documentContext.getInstance(ProjectInstance.class));
		assertEquals(documentContext.getFormMode(), FormViewMode.PREVIEW);
		assertEquals(event.getNavigation(), PmNavigationConstants.NAVIGATE_USER_DASHBOARD);
	}

	/**
	 * Manage relations.
	 */
	public void manageRelations() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.MANAGE_RELATIONS,
				new EmfAction(PmActionTypeConstants.MANAGE_RELATIONS));
		action.manageRelations(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.MANAGE_RELATIONS,
				new EmfAction(PmActionTypeConstants.MANAGE_RELATIONS));
		action.manageRelations(event);
		checkAction(documentContext, event);
	}

	/**
	 * Manage resources.
	 */
	public void manageResources() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.MANAGE_RESOURCES,
				new EmfAction(PmActionTypeConstants.MANAGE_RESOURCES));
		action.manageResources(event);

		assertNull(documentContext.getInstance(ProjectInstance.class));
		assertEquals(documentContext.getFormMode(), FormViewMode.PREVIEW);
		assertEquals(event.getNavigation(), PmNavigationConstants.MANAGE_RESOURCES);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.MANAGE_RESOURCES,
				new EmfAction(PmActionTypeConstants.MANAGE_RESOURCES));
		isImmediate = true;
		Mockito.when(
				instanceService.save(Mockito.any(ProjectInstance.class),
						Mockito.any(Operation.class))).thenReturn(projectInstance);
		action.manageResources(event);
		ProjectInstance instance = documentContext.getInstance(ProjectInstance.class);
		assertEquals(instance, projectInstance);
		assertEquals(documentContext.getFormMode(), FormViewMode.PREVIEW);
		assertEquals(event.getNavigation(), PmNavigationConstants.MANAGE_RESOURCES);
	}

	/**
	 * Restart.
	 */
	public void restart() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.RESTART,
				new EmfAction(PmActionTypeConstants.RESTART));
		action.restart(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.RESTART,
				new EmfAction(PmActionTypeConstants.RESTART));
		action.restart(event);
		checkAction(documentContext, event);
	}

	/**
	 * Start.
	 */
	public void start() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.START,
				new EmfAction(PmActionTypeConstants.START));
		action.start(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.START, new EmfAction(
				PmActionTypeConstants.START));
		action.start(event);
		checkAction(documentContext, event);
	}

	/**
	 * Stop.
	 */
	public void stop() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.STOP,
				new EmfAction(PmActionTypeConstants.STOP));
		action.stop(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.STOP, new EmfAction(
				PmActionTypeConstants.STOP));
		action.stop(event);
		checkAction(documentContext, event);
	}

	/**
	 * Suspend.
	 */
	public void suspend() {
		DocumentContext documentContext = action.getDocumentContext();
		EMFActionEvent event = createEventObject("", null, PmActionTypeConstants.SUSPEND,
				new EmfAction(PmActionTypeConstants.SUSPEND));
		action.suspend(event);
		checkActionNoInstance(documentContext, event);

		// if instance is passed with event
		event = createEventObject("", projectInstance, PmActionTypeConstants.SUSPEND,
				new EmfAction(PmActionTypeConstants.SUSPEND));
		Mockito.when(
				instanceService.save(Mockito.any(ProjectInstance.class),
						Mockito.any(Operation.class))).thenReturn(projectInstance);
		action.suspend(event);
		checkAction(documentContext, event);
	}
}
