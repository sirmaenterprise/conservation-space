package com.sirma.cmf.web.entity.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.event.document.DocumentOpenEvent;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.notification.MessageLevel;
import com.sirma.itt.emf.web.notification.NotificationMessage;

/**
 * Initializes the page context for {@link DocumentInstance}.
 * 
 * @author svelikov
 */
@Extension(target = PageContextInitializerExtension.TARGET_NAME, order = 20)
public class DocumentInstanceContextInitializer extends PageContextInitializer implements
		PageContextInitializerExtension<DocumentInstance> {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(
			Arrays.asList(DocumentInstance.class));

	/** The documents table action. */
	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

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
	public String initContextFor(DocumentInstance instance) {
		String navigation = NavigationConstants.NAVIGATE_HOME;
		if (instance == null) {
			log.warn("CMFWeb: Can not initialize context for document instance=null");
			return navigation;
		}

		if (entityPreviewAction.canOpenInstance(instance)) {
			getDocumentContext().populateContext(instance, DocumentDefinitionRef.class,
					(DocumentDefinitionRef) dictionaryService.getInstanceDefinition(instance));
			initializeContextInstance(instance);
			initializeRoot(instance);
			eventService.fire(new DocumentOpenEvent(instance));
			caseDocumentsTableAction.open(instance);
			return NavigationConstants.NAVIGATE_DOCUMENT_DETAILS;
		}

		StringBuilder message = new StringBuilder();
		message.append(labelProvider.getValue("warn.document.noaccess"));
		notificationSupport.addMessage(new NotificationMessage(message.toString(),
				MessageLevel.WARN));
		return NavigationConstants.NAVIGATE_HOME;
	}

}
