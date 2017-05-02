package com.sirma.itt.seip.concurrent.event;

import java.lang.invoke.MethodHandles;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.security.annotation.SecureObserver;

/**
 * Asynchronous observer that handles the execution of asynchronous actions.
 *
 * @author BBonev
 */
@Stateless
public class AsyncCallableEventExecuter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Execute action asynchronously in transaction.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SecureObserver
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncActionInNewTx(@Observes final TxAsyncCallableEvent event) {
		try {
			event.call();
		} catch (RuntimeException e) {
			// log error and throw application exception to force transaction rollback
			LOGGER.error("Failed transactional async operation with {}", e.getMessage(), e);
			throw new RollbackedRuntimeException("Failed transactional async operation. Rolling back tranaction", e);
		}
	}

	/**
	 * Execute action asynchronously without transaction support.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SecureObserver
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeAsyncActionInNoTx(@Observes final NonTxAsyncCallableEvent event) {
		try {
			event.call();
		} catch (RuntimeException e) {
			LOGGER.error("Failed non transactional async operation with {}", e.getMessage(), e);
			throw e;
		}
	}

}
