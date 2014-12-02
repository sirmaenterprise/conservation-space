package com.sirma.itt.pm.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.entity.dispatcher.PageContextInitializer;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializerExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectOpenEvent;
import com.sirma.itt.pm.web.PMEntityPreviewAction;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;

/**
 * Initializes the page context for {@link ProjectInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 60)
public class ProjectInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<ProjectInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(ProjectInstance.class));

	/** The pm entity preview action. */
	@Inject
	private PMEntityPreviewAction pmEntityPreviewAction;

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
	public String initContextFor(ProjectInstance instance) {
		if (pmEntityPreviewAction.canOpenProject(instance)) {
			ProjectDefinition definition = (ProjectDefinition) dictionaryService
					.getInstanceDefinition(instance);
			getDocumentContext().populateContext(instance, ProjectDefinition.class, definition);
			getDocumentContext().addContextInstance(instance);

			initializeRoot(instance);

			eventService.fire(new ProjectOpenEvent(instance));
			return PmNavigationConstants.PROJECT_DASHBOARD;
		}
		return PmNavigationConstants.NAVIGATE_HOME;
	}

}
