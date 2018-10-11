package com.sirma.itt.emf.semantic;

import java.lang.invoke.MethodHandles;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.util.TxUtils;

/**
 * JTA synchronization implementation that commits or rollbacks a managed repository connection associated with a
 * transaction.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/03/2018
 */
class RepositoryConnectionSynchronization implements Synchronization {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The connection factory used for committing the connections.
	 */
	private final TransactionCoordinator transactionCoordinator;
	private final Transaction owningTx;
	private final TimeTracker timeTracker;

	private volatile long registrationThreadId;

	RepositoryConnectionSynchronization(Transaction transaction, TransactionCoordinator transactionCoordinator) {
		this.transactionCoordinator = transactionCoordinator;
		registrationThreadId = Thread.currentThread().getId();
		owningTx = transaction;
		timeTracker = TimeTracker.createAndStart();
	}

	@Override
	public void beforeCompletion() {
		LOGGER.trace("Transaction before completion callback for repository connection");

		boolean flush;
		try {
			int txStatus = owningTx.getStatus();
			flush = transactionCoordinator.isActive(owningTx) && !TxUtils.isRollback(txStatus);
		} catch (SystemException se) {
			setRollbackOnly();
			throw new SemanticPersistenceException(
					"Could not determine transaction status in beforeCompletion()", se);
		}

		try {
			if (flush) {
				LOGGER.trace("Automatically flushing session {} ms after creation", timeTracker.elapsedTime());

				transactionCoordinator.commitTransaction(owningTx);

				LOGGER.trace("Session was committed after {} ms after creation", timeTracker.stop());
			}
		} catch (RuntimeException re) {
			setRollbackOnly();
			throw new SemanticPersistenceException("Error during managed flush", re);
		}
	}

	private void setRollbackOnly() {
		try {
			owningTx.setRollbackOnly();
		} catch (SystemException e) {
			throw new SemanticPersistenceException("Could not mark transaction for rollback", e);
		}
	}

	@Override
	public void afterCompletion(int status) {
		LOGGER.trace("Transaction after completion callback for connection with status {}", TxUtils.getStatusString(status));

		// The whole concept of "tracking" comes down to this code block..
		// Essentially we need to see if we can process the callback immediately.  So here we check whether the
		// current call is happening on the same thread as the thread under which we registered the Synchronization.
		// As far as we know, this can only ever happen in the rollback case where the transaction had been rolled
		// back on a separate "reaper" thread.  Since we know the transaction status and that check is not as heavy
		// as accessing the current thread, we check that first
		if (TxUtils.isRollback(status)) {
			// we are processing a rollback, see if it is the same thread
			final long currentThreadId = Thread.currentThread().getId();
			final boolean isRegistrationThread = currentThreadId == registrationThreadId;
			if (!isRegistrationThread) {
				LOGGER.warn("Transaction for connection was rollbacked from other tread");
				// this will rollback in case not already rollbacked
				closeConnection();
				return;
			}
			try {
				transactionCoordinator.rollbackTransaction(owningTx);
			} finally {
				// in finally as in case of rollback error the close may not be called
				closeConnection();
			}
		} else {
			closeConnection();
		}
	}

	private void closeConnection() {
		transactionCoordinator.closeConnection(owningTx);
	}
}
