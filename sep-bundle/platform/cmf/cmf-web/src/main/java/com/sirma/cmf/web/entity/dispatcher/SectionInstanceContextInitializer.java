package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.cmf.web.caseinstance.tab.CaseTabConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.event.cases.CaseOpenEvent;
import com.sirma.itt.cmf.event.section.SectionOpenEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.notification.MessageLevel;
import com.sirma.itt.emf.web.notification.NotificationMessage;

/**
 * Initializes the page context for {@link SectionInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 12, priority = 0)
public class SectionInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<SectionInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(SectionInstance.class));

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
	public String initContextFor(SectionInstance sectionInstance) {
		// TODO: folders should not be opened until landing page is implemented
		if (SectionProperties.PURPOSE_FOLDER.equals(sectionInstance.getPurpose())) {
			return null;
		}

		StringBuilder message = new StringBuilder(labelProvider.getValue("warn.section.cantopen"));
		String navigation = NavigationConstants.NAVIGATE_HOME;
		Instance caseInstance = sectionInstance.getOwningInstance();
		if (caseInstance != null) {
			if (entityPreviewAction.canOpenInstance(sectionInstance)) {
				getDocumentContext().addInstance(caseInstance);
				getDocumentContext().populateContext(caseInstance, CaseDefinition.class,
						(CaseDefinition) dictionaryService.getInstanceDefinition(caseInstance));
				initializeRoot(caseInstance);

				eventService.fire(new CaseOpenEvent((CaseInstance) caseInstance));
				eventService.fire(new SectionOpenEvent(sectionInstance));

				navigation = NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS;
				getDocumentContext().setSelectedTab(CaseTabConstants.DOCUMENTS);

				return navigation;
			}

			message.append(labelProvider.getValue("warn.section.noaccess"));

		} else {
			message.append(labelProvider.getValue("warn.section.casenotfound"));
		}

		notificationSupport.addMessage(new NotificationMessage(message.toString(),
				MessageLevel.WARN));

		return navigation;
	}

}
