package com.sirma.itt.emf.audit.observer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.cmf.help.HelpRequestEvent;
import com.sirma.itt.emf.audit.processor.AuditProcessor;

/**
 * Logs action events in the audit log.
 *
 * @author nvelkov
 */
@Singleton
@Transactional(TxType.REQUIRED)
public class AuditActionObserver {

	@Inject
	private AuditProcessor auditProcessor;

	/**
	 * Observes help request events.
	 *
	 * @param event
	 *            - the help request event
	 */
	public void onHelpRequest(@Observes HelpRequestEvent event) {
		auditProcessor.process(null, event.getOperationId(), event);
	}

}
