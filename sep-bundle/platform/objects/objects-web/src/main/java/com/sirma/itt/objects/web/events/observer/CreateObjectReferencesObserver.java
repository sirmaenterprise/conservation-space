package com.sirma.itt.objects.web.events.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.idoc.web.events.observer.AbstractDocumentLinkHandler;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Listens for object created/persisted events and creates 'references' relationships from the
 * object to the objects configured in for display in widgets.
 * 
 * @author yasko
 */
@ApplicationScoped
public class CreateObjectReferencesObserver extends AbstractDocumentLinkHandler {

	/**
	 * Listens for the {@link InstancePersistedEvent} and scans for widgets, so that references
	 * could be created.
	 * 
	 * @param event
	 *            Event payload.
	 */
	public void handleDocumentPersistedEvent(
			@Observes InstancePersistedEvent<? extends Instance> event) {
		if (!isOperationChanging(event) || !(event.getInstance() instanceof ObjectInstance)) {
			return;
		}
		handle(event.getInstance());
	}

	/**
	 * Initiates view content scan for widgets that should create relationships.
	 * 
	 * @param instance
	 *            to link from.
	 */
	private void handle(Instance instance) {
		DocumentInstance documentInstance = (DocumentInstance) instance.getProperties().get(
				ObjectProperties.DEFAULT_VIEW);

		if (documentInstance != null) {
			// FIXME view creation should set this
			documentInstance.setPurpose("iDoc");
			handle(documentInstance, instance);
		}
	}
}