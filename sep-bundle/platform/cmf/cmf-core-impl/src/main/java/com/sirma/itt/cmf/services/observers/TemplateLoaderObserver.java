package com.sirma.itt.cmf.services.observers;

import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.event.LoadTemplates;
import com.sirma.itt.cmf.services.DocumentTemplateService;
import com.sirma.itt.emf.security.Secure;

/**
 * Event observer for asynchronous template loading/reloading.
 * 
 * @author BBonev
 */
@Stateless
@Secure(runAsSystem = true)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TemplateLoaderObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateLoaderObserver.class);
	/** The template service. */
	@Inject
	private DocumentTemplateService templateService;

	/** Used to restrict multiple simultaneous template loadings. */
	private static final ReentrantLock LOCK = new ReentrantLock();

	/**
	 * Listens for {@link LoadTemplates} event to initiate a template loading.
	 * 
	 * @param event
	 *            the event
	 */
	@Asynchronous
	public void onApplicationLoaded(@Observes LoadTemplates event) {
		// here we check if someone called a synchronization during
		// asynchronous call
		if (!LOCK.tryLock()) {
			LOGGER.warn("Template loading in progress: ignoring event requiest LoadTemplates");
			return;
		}
		try {
			templateService.reload();
		} finally {
			LOCK.unlock();
		}
	}
}
