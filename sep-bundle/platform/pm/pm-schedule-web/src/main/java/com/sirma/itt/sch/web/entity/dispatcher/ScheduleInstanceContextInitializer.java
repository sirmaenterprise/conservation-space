package com.sirma.itt.sch.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializer;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializerExtension;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;
import com.sirma.itt.sch.web.entity.event.ProjectScheduleOpenEvent;

/**
 * Initializes the page context for {@link ScheduleInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 70)
public class ScheduleInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<ScheduleInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(ScheduleInstance.class));

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

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
	public String initContextFor(ScheduleInstance instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;
		InstanceReference owningReference = instance.getOwningReference();
		Instance owningInstance = typeConverter.convert(Instance.class, owningReference);
		if (entityPreviewAction.canOpenInstance(owningInstance)) {
			getDocumentContext().addInstance(owningInstance);
			ProjectDefinition definition = (ProjectDefinition) dictionaryService
					.getInstanceDefinition(owningInstance);
			getDocumentContext().addDefinition(ProjectDefinition.class, definition);

			ScheduleInstance scheduleInstance = scheduleService.getOrCreateSchedule(owningInstance);
			getDocumentContext().put("scheduleInstance", scheduleInstance);
			// fire schedule open event
			eventService.fire(new ProjectScheduleOpenEvent(scheduleInstance));
			navigation = PmNavigationConstants.PROJECT_SCHEDULE;
		}
		return navigation;
	}

}
