package com.sirma.cmf.web.workflow;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.instance.landingpage.InstanceLandingPage;
import com.sirma.cmf.web.workflow.transition.TaskTransitionAction;
import com.sirma.cmf.web.workflow.transition.TransitionsBuilder;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.workflow.WorkflowOpenEvent;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Workflow landing page backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowLandingPage extends
		InstanceLandingPage<WorkflowInstanceContext, WorkflowDefinition> implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3303411441807949435L;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The operations builder. */
	@Inject
	private TransitionsBuilder operationsBuilder;

	/**
	 * Wrapper object for workflow tasks: active and completed. This is shared in workflow and task
	 * views
	 */
	@Inject
	private WorkflowTasksHolder workflowTasksHolder;

	/** Workflow diagram drawing bean. */
	@Inject
	private DrawBean drawBean;

	/** Key to process diagram buffered image in session map. */
	private String picKey;

	/** The workflow transition actions. */
	private List<TaskTransitionAction> workflowTransitions;

	/**
	 * Overrides the super method because we have different definition loading and there are fields
	 * to be initialized. {@inheritDoc}
	 * 
	 * @param instanceClass
	 *            the instance class
	 * @param instance
	 *            the instance
	 * @param panel
	 *            the panel
	 */
	@Override
	protected void initForEdit(Class<WorkflowInstanceContext> instanceClass,
			WorkflowInstanceContext instance, UIComponent panel) {
		Pair<List<TaskInstance>, List<TaskInstance>> taskLists = loadTaskLists(instance);

		// setActiveTasks(taskLists.getFirst());
		// setCompletedTasks(taskLists.getSecond());
		workflowTasksHolder.setActiveTasks(taskLists.getFirst());
		workflowTasksHolder.setCompletedTasks(taskLists.getSecond());

		// process diagram should be rendered only for active workflows
		if (instance.isActive()) {
			loadProcessDiagram(instance);
		}

		if (panel.getChildCount() == 0) {
			panel.getChildren().clear();

			String workflowDefinitionId = instance.getIdentifier();
			WorkflowDefinition workflowDefinition = dictionaryService.getDefinition(
					WorkflowDefinition.class, workflowDefinitionId, instance.getRevision());

			RegionDefinitionModel model = workflowDefinition;

			TaskDefinitionRef workflowPreviewTask = WorkflowHelper
					.getWorkflowPreviewTask(workflowDefinition);
			if (workflowPreviewTask != null) {
				model = workflowPreviewTask;
			}
			List<TaskTransitionAction> buildTransitionActions = buildTransitionActions((Transitional) model);
			setWorkflowTransitions(buildTransitionActions);

			// this is for backward compatibility ONLY
			// if should be removed, the workflow definition impl should be kept.
			if (workflowPreviewTask != null) {
				renderStartTaskForm((TaskDefinitionRef) model, instance, FormViewMode.PREVIEW,
						panel, null, TaskDefinitionRef.class);
			} else {
				renderStartTaskForm((WorkflowDefinition) model, instance, FormViewMode.PREVIEW,
						panel, null, WorkflowDefinition.class);
			}
		}
	}

	/**
	 * Pass the transitions to transitions builder to generate the operation buttons.
	 * 
	 * @param taskDefinition
	 *            the task definition
	 * @return Transition actions list
	 */
	private List<TaskTransitionAction> buildTransitionActions(Transitional taskDefinition) {
		List<TransitionDefinition> transitions = loadTransitions(taskDefinition);
		return operationsBuilder.build(transitions);
	}

	/**
	 * Load transitions.
	 * 
	 * @param taskDefinition
	 *            the task definition
	 * @return the list
	 */
	protected List<TransitionDefinition> loadTransitions(Transitional taskDefinition) {
		return DefinitionUtil.getDefaultTransitions(taskDefinition);
	}

	/**
	 * Render workflow form.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param definition
	 *            the task definition
	 * @param startTaskInstance
	 *            the start task instance (this is actually a WorkflowInstanceContext)
	 * @param formViewMode
	 *            the form view mode
	 * @param panel
	 *            the panel
	 * @param rootInstanceName
	 *            the root instance name
	 * @param type
	 *            the type
	 */
	private <D extends RegionDefinitionModel> void renderStartTaskForm(D definition,
			Instance startTaskInstance, FormViewMode formViewMode, UIComponent panel,
			String rootInstanceName, Class<D> type) {
		log.debug("CMFWeb: Executing WorkflowActionBase.renderWorkflowForm");
		// - pass the task definition to the definition reader to render the
		// form
		if (definition != null) {
			getDocumentContext().addDefinition(type, definition);
			mergeFields(definition);
			// - build the form
			// - for workflow details form we should pass the workflowInstance
			invokeReader(definition, startTaskInstance, panel, formViewMode, rootInstanceName);
		}
	}

	/**
	 * According to the selected workflow type a workflow instance is created and stored in context
	 * alongside with the workflow definition and the start task definition too. The actual
	 * rendering of the form is performed in init function executed on preRenderView event.
	 */
	@Override
	public void itemSelectedAction() {
		log.debug("CMFWeb: InstanceLandingPage.itemSelectedAction selected definition: ["
				+ getSelectedType() + "]");

		if (StringUtils.isNullOrEmpty(getSelectedType())) {
			return;
		}

		// finds the definition for the selected type
		WorkflowDefinition selectedDefinition = dictionaryService.getDefinition(
				WorkflowDefinition.class, getSelectedType());
		if (selectedDefinition == null) {
			log.error("CMFWeb: InstanceLandingPage.itemSelectedAction cann't create new instance with null definition");
			return;
		}
		getDocumentContext().addDefinition(WorkflowDefinition.class, selectedDefinition);

		// For every new instance except project we should have the context inside which it
		// should be created.
		Instance contextInstance = getDocumentContext().getContextInstance();

		// create a workflow instance for the selected type
		WorkflowInstanceContext workflowInstance = getNewInstance(selectedDefinition,
				contextInstance);
		getDocumentContext().addInstance(workflowInstance);

		// - Get the start task
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(selectedDefinition);
		getDocumentContext().addDefinition(TaskDefinitionRef.class, startTaskDefinition);

		Instance startTaskInstance = instanceService.createInstance(
				startTaskDefinition,
				workflowInstance,
				new Operation(getDocumentContext().getCurrentOperation(
						getDocumentContext().getCurrentInstance().getClass().getSimpleName())));
		getDocumentContext().addInstance(startTaskInstance);

		fireInstanceOpenEvent(startTaskInstance);

		// build the for for selected type
		UIComponent panel = getPanel(CMFConstants.INSTANCE_DATA_PANEL);
		if ((panel != null) && (panel.getChildCount() > 0)) {
			panel.getChildren().clear();
		}

		renderStartTaskForm(startTaskDefinition, startTaskInstance, FormViewMode.EDIT, panel, null,
				TaskDefinitionRef.class);
		setWorkflowTransitions(buildTransitionActions(startTaskDefinition));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<WorkflowDefinition> getInstanceDefinitionClass() {
		return WorkflowDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected WorkflowInstanceContext getNewInstance(WorkflowDefinition selectedDefinition,
			Instance context) {
		return workflowService.createInstance(selectedDefinition, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<WorkflowInstanceContext> getInstanceClass() {
		return WorkflowInstanceContext.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected InstanceReference getParentReference() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String saveInstance(WorkflowInstanceContext instance) {
		eventService.fire(new WorkflowOpenEvent(instance));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String cancelEditInstance(WorkflowInstanceContext instance) {
		String navigation = NavigationConstants.BACKWARD;
		return navigation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onExistingInstanceInitPage(WorkflowInstanceContext instance) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onNewInstanceInitPage(WorkflowInstanceContext instance) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getDefinitionId(WorkflowDefinition definition) {
		return WorkflowHelper.stripEngineId(definition.getIdentifier());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FormViewMode getFormViewModeExternal(WorkflowInstanceContext instance) {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getNavigationString() {

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getDefinitionFilterType() {
		return ObjectTypesCmf.WORKFLOW;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected InstanceService<WorkflowInstanceContext, WorkflowDefinition> getInstanceService() {
		return workflowService;
	}

	/**
	 * Load task lists.
	 * 
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the pair with the two task lists
	 */
	private Pair<List<TaskInstance>, List<TaskInstance>> loadTaskLists(
			WorkflowInstanceContext workflowInstance) {

		log.debug("CMFWeb: Executing WorkflowActionBase.loadTaskLists");

		List<TaskInstance> workflowTasks = workflowService.getWorkflowTasks(workflowInstance,
				TaskState.ALL);

		List<TaskInstance> activeTasksList = new ArrayList<TaskInstance>();
		List<TaskInstance> completedTasksList = new ArrayList<TaskInstance>();

		for (TaskInstance taskInstance : workflowTasks) {
			TaskState state = taskInstance.getState();
			if (TaskState.IN_PROGRESS == state) {
				activeTasksList.add(taskInstance);
			} else if (TaskState.COMPLETED == state) {
				completedTasksList.add(taskInstance);
			}
		}

		Pair<List<TaskInstance>, List<TaskInstance>> tasks = new Pair<List<TaskInstance>, List<TaskInstance>>(
				activeTasksList, completedTasksList);
		return tasks;
	}

	/**
	 * Renders current process diagram.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return string as value for a4j:mediaOutput UI component
	 */
	public String loadProcessDiagram(WorkflowInstanceContext workflowInstanceContext) {

		String stringKey = null;
		log.debug("CMFWeb: Executing WorkflowDetails.loadProcessDiagram");

		if (workflowInstanceContext != null) {
			BufferedImage bufferedImage = workflowService
					.getWorkflowProcessDiagram(workflowInstanceContext);

			stringKey = "tst" + getWorkflowIdentifier(workflowInstanceContext);
			setPicKey(stringKey);

			FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.put(getPicKey(), bufferedImage);

			if (bufferedImage == null) {
				drawBean.setMissingImage(true);
			} else {
				drawBean.setMissingImage(false);
			}
		}
		return stringKey;
	}

	/**
	 * Gets the workflow identifier.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return the workflow identifier
	 */
	public String getWorkflowIdentifier(WorkflowInstanceContext workflowInstanceContext) {
		return workflowInstanceContext.getId().toString().replace(':', '-');
	}

	/**
	 * Getter method for picKey.
	 * 
	 * @return the picKey
	 */
	public String getPicKey() {
		return picKey;
	}

	/**
	 * Setter method for picKey.
	 * 
	 * @param picKeyValue
	 *            the picKey to set
	 */
	public void setPicKey(String picKeyValue) {
		this.picKey = picKeyValue;
	}

	/**
	 * Getter method for workflowTransitions.
	 * 
	 * @return the workflowTransitions
	 */
	public List<TaskTransitionAction> getWorkflowTransitions() {
		return workflowTransitions;
	}

	/**
	 * Setter method for workflowTransitions.
	 * 
	 * @param workflowTransitionActions
	 *            the workflowTransitions to set
	 */
	public void setWorkflowTransitions(List<TaskTransitionAction> workflowTransitionActions) {
		this.workflowTransitions = workflowTransitionActions;
	}

	/**
	 * Getter method for workflowTasksHolder.
	 * 
	 * @return the workflowTasksHolder
	 */
	public WorkflowTasksHolder getWorkflowTasksHolder() {
		return workflowTasksHolder;
	}

	/**
	 * Setter method for workflowTasksHolder.
	 * 
	 * @param workflowTasksHolder
	 *            the workflowTasksHolder to set
	 */
	public void setWorkflowTasksHolder(WorkflowTasksHolder workflowTasksHolder) {
		this.workflowTasksHolder = workflowTasksHolder;
	}

}
