package com.sirma.itt.pm.services.impl.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.AfterProjectPersistEvent;
import com.sirma.itt.pm.services.adapter.CMFProjectInstanceAdapterService;

/**
 * Observer for failed transaction events for PM objects. The observer tries to delete from DMS the
 * successfully created instances if the overall transaction for creation failed.
 * 
 * @author BBonev
 */
@Secure
@ApplicationScoped
public class PmInstanceCreateRollbackObserver {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PmInstanceCreateRollbackObserver.class);
	/** The adapter service. */
	@Inject
	private CMFProjectInstanceAdapterService adapterService;

	/**
	 * Deletes project on transaction fail.
	 * 
	 * @param event
	 *            the event
	 */
	public void onProjectRollback(
			@Observes(during = TransactionPhase.AFTER_FAILURE) AfterProjectPersistEvent event) {
		LOGGER.debug("Trying to delete project from DMS on failed transaction for creation");
		ProjectInstance instance = event.getInstance();
		if (StringUtils.isNotNullOrEmpty(instance.getDmsId())
				&& !Boolean.TRUE
						.equals(instance.getProperties().get(DefaultProperties.IS_IMPORTED))) {
			try {
				adapterService.deleteProjectInstance(instance, true);
			} catch (DMSException e) {
				LOGGER.warn("Failed to delete project from DMS on failed transaction", e);
			}
		}
	}
}
