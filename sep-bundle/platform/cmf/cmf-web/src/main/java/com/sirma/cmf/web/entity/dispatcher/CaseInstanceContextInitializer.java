package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.cmf.web.caseinstance.tab.CaseTabConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.event.cases.CaseOpenEvent;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.notification.MessageLevel;
import com.sirma.itt.emf.web.notification.NotificationMessage;

/**
 * Initializes the page context for {@link CaseInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 10)
public class CaseInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<CaseInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(CaseInstance.class));

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
	public String initContextFor(CaseInstance instance) {
		StringBuilder message = new StringBuilder(labelProvider.getValue("warn.case.noaccess"));
		String navigation = NavigationConstants.NAVIGATE_HOME;
		boolean canOpenCase = entityPreviewAction.canOpenInstance(instance);
		if (canOpenCase) {
			// if the case is in project we should add it to context too
			Instance owningInstance = instance.getOwningInstance();
			getDocumentContext().addInstance(owningInstance);

			initializeRoot(instance);

			getDocumentContext().populateContext(instance, CaseDefinition.class,
					(CaseDefinition) dictionaryService.getInstanceDefinition(instance));
			// populate document context with parent instance if exists

			eventService.fire(new CaseOpenEvent(instance));

			// if a tab name is not provided trough the link, we open the dashboard by default
			if (StringUtils.isNullOrEmpty(getDocumentContext().getSelectedTab())) {
				getDocumentContext().setSelectedTab(CaseTabConstants.CASE_DEFAULT_TAB);
			}

			navigation = NavigationConstants.NAVIGATE_TAB_CASE_DASHBOARD;
			return navigation;
		}

		notificationSupport.addMessage(new NotificationMessage(message.toString(),
				MessageLevel.WARN));
		return navigation;
	}

}
