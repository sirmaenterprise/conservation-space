package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.workflow.task.TaskListTableAction;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.event.task.workflow.TaskOpenEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Initializes the page context for {@link TaskInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 40)
public class WorkflowTaskInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<TaskInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(TaskInstance.class));

	/** The task list table action. */
	@Inject
	private TaskListTableAction taskListTableAction;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String initContextFor(TaskInstance instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;

		WorkflowInstanceContext workflowInstance = instance.getContext();
		Instance owningInstance = workflowInstance.getOwningInstance();
		if (entityPreviewAction.canOpenInstance(owningInstance)) {
			initializeRoot(instance);
			navigation = taskListTableAction.open(instance);
			// fire an event that a task is to be opened
			// this event may be caught in third party application
			eventService.fire(new TaskOpenEvent(instance));
		}
		return navigation;
	}

}
