package com.sirma.itt.emf.concurrent;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Asynchronous observer that handles the execution of asynchronous actions.
 * 
 * @author BBonev
 */
@Stateless
public class AsyncCallableEventExecuter {

	/** The LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCallableEventExecuter.class);

	/**
	 * Execute action asynchronously in transaction.
	 * 
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncActionInNewTx(@Observes final TxAsyncCallableEvent event) {
		executeEvent(event);
	}

	/**
	 * Execute action asynchronously without transaction support.
	 * 
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeAsyncActionInNoTx(@Observes final NonTxAsyncCallableEvent event) {
		executeEvent(event);
	}

	/**
	 * Execute event.
	 * 
	 * @param event
	 *            the event
	 */
	private void executeEvent(final BaseCallableEvent event) {
		if (event.getCallable() == null) {
			// nothing to do
			return;
		}
		SecurityContext context = event.getSecurityContext();
		// if we have a security context we will execute the action in that context
		if (context != null) {
			SecurityContextManager.callAs(context.getAuthentication(),
					context.getEffectiveAuthentication(), event.getCallable());
		} else {
			try {
				event.getCallable().call();
			} catch (Exception e) {
				LOGGER.warn("Failed to execute asynchronous callable event due to ", e);
			}
		}
	}

}
