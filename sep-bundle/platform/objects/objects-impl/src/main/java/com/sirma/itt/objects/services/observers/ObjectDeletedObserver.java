package com.sirma.itt.objects.services.observers;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.instance.dao.InstanceServiceProvider;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.objects.event.BeforeObjectDeleteEvent;

/**
 * Observer for object deletion
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ObjectDeletedObserver {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceServiceProvider instanceService;

	@Inject
	private Logger logger;

	/**
	 * Observer method that listens for object deleted event to delete the object child documents if
	 * any
	 * 
	 * @param event
	 *            the event
	 */
	public void onObjectDeleted(@Observes BeforeObjectDeleteEvent event) {
		List<LinkReference> links = linkService.getLinks(event.getInstance().toReference(),
				LinkConstants.PARENT_TO_CHILD);
		List<LinkInstance> list = linkService.convertToLinkInstance(links, true);
		Operation operation = new Operation(ActionTypeConstants.DELETE);
		for (LinkInstance linkInstance : list) {
			// check if the document was not deleted or something else happen
			// also check if it's document, not to delete something unexpected
			if (linkInstance.getTo() instanceof DocumentInstance) {
				instanceService.delete(linkInstance.getTo(), operation, false);
			} else {
				if (linkInstance.getTo() != null) {
					logger.warn("! Skipping deletion of non document ("
							+ linkInstance.getTo().getClass().getSimpleName() + " with id="
							+ linkInstance.getTo().getId() + ") child of an object with ID/URI="
							+ event.getInstance().getId());
				}
			}
		}
	}

}
