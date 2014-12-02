package com.sirma.itt.objects.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializer;
import com.sirma.cmf.web.entity.dispatcher.PageContextInitializerExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.notification.MessageLevel;
import com.sirma.itt.emf.web.notification.NotificationMessage;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.event.ObjectOpenEvent;
import com.sirma.itt.objects.web.constants.ObjectNavigationConstants;

/**
 * Initializes the page context for {@link ObjectInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 80)
public class ObjectInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<ObjectInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(ObjectInstance.class));

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
	public String initContextFor(ObjectInstance instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;
		if (instance == null) {
			log.warn("ObjectsWeb: Can not initialize context for objectInstance=null");
			return navigation;
		}

		if (entityPreviewAction.canOpenInstance(instance)) {
			getDocumentContext().populateContext(instance, ObjectDefinition.class,
					(ObjectDefinition) dictionaryService.getInstanceDefinition(instance));
			// this overrides the contextInstance applied in EntityOpenDispatcher
			initializeContextInstance(instance);
			initializeRoot(instance);
			eventService.fire(new ObjectOpenEvent(instance));
			return ObjectNavigationConstants.OBJECT;
		}

		StringBuilder message = new StringBuilder(labelProvider.getValue("warn.object.cantopen"));
		notificationSupport.addMessage(new NotificationMessage(message.toString(),
				MessageLevel.WARN));
		return ObjectNavigationConstants.NAVIGATE_HOME;
	}

}
