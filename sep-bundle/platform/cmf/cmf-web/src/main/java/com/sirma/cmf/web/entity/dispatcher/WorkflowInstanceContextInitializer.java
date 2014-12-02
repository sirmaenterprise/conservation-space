package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.event.workflow.WorkflowOpenEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Initializes the page context for {@link WorkflowInstanceContext}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 30)
public class WorkflowInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<WorkflowInstanceContext> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(WorkflowInstanceContext.class));

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
	public String initContextFor(WorkflowInstanceContext instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;
		Instance owningInstance = instance.getOwningInstance();
		if (entityPreviewAction.canOpenInstance(owningInstance)) {
			// initialize the context with the workflow data
			WorkflowDefinition workflowDefinition = (WorkflowDefinition) dictionaryService
					.getInstanceDefinition(instance);
			getDocumentContext().populateContext(instance, WorkflowDefinition.class,
					workflowDefinition);

			// initialize the root instance if any
			initializeRoot(instance);

			// event is fired to allow someone to deal with some initialization tasks
			eventService.fire(new WorkflowOpenEvent(instance));

			navigation = NavigationConstants.WORKFLOW_LANDING_PAGE;
		}
		return navigation;
	}

}
