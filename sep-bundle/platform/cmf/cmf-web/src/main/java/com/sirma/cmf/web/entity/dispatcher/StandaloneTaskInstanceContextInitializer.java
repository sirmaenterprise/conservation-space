package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.standaloneTask.StandaloneTaskAction;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOpenEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Initializes the page context for {@link StandaloneTaskInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 50)
public class StandaloneTaskInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<StandaloneTaskInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(StandaloneTaskInstance.class));

	/** The standalone task action. */
	@Inject
	private StandaloneTaskAction standaloneTaskAction;

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
	public String initContextFor(StandaloneTaskInstance instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;

		Instance owningInstance = instance.getOwningInstance();
		if (entityPreviewAction.canOpenInstance(owningInstance)) {
			getDocumentContext().addInstance(owningInstance);
			initializeRoot(instance);
			navigation = standaloneTaskAction.open(instance);
			// fire an event that a task is to be opened
			// this event may be caught in third party application
			eventService.fire(new StandaloneTaskOpenEvent(instance));
		}

		return navigation;
	}

}
