package com.sirma.itt.cmf.services.observers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.event.document.AfterDocumentMoveEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * A synchronous update interface for receiving notifications about DocumentMove information as the
 * DocumentMove is constructed.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DocumentMoveObserver {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The logger. */
	@Inject
	private Logger logger;

	/**
	 * Called after the document was moved. The method removes the links associated with the given
	 * document instance for input and output documents from the tasks
	 * 
	 * @param event
	 *            the event
	 */
	public void onAfterMove(@Observes AfterDocumentMoveEvent event) {
		DocumentInstance documentInstance = event.getInstance();
		CaseInstance oldCase = InstanceUtil
				.getParent(CaseInstance.class, event.getSourceInstance());
		if ((oldCase != null)) {
			CaseInstance newCase = InstanceUtil.getParent(CaseInstance.class,
					event.getTargetInstance());
			if (newCase != null) {
				if (!EqualsHelper.entityEquals(oldCase, newCase)) {
					// the document was moved to other case .. we need to remove the links that
					// are associated with the old case tasks
					Set<String> types = new HashSet<String>(Arrays.asList(
							LinkConstants.INCOMING_DOCUMENTS_LINK_ID,
							LinkConstants.OUTGOING_DOCUMENTS_LINK_ID));
					boolean removed = linkService.removeLinksFor(documentInstance.toReference(),
							types);
					if (removed) {
						logger.debug("Removed links for document instance: "
								+ documentInstance.getId() + ":"
								+ documentInstance.getDmsId());
					} else {
						logger.debug("No links to remove for document instance: "
								+ documentInstance.getId() + ":"
								+ documentInstance.getDmsId());
					}
				} else {
					logger.debug("Document moved in the same case. Nothing to do.");
				}
			} else {
				logger.warn("Called method on event: AFTER document MOVE but the required data was not set!");
			}
		}
	}

}
