package com.sirma.itt.emf.semantic;

import java.lang.invoke.MethodHandles;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.ConnectionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.exception.TransactionNotActiveException;
import com.sirma.itt.seip.tx.util.TxUtils;

/**
 * Tracks the active transactions and the connections associated with them. Provides access to the connections for
 * given transaction.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2018
 */
class TransactionCoordinator {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final TransactionManager transactionManager;
	private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
	private final Supplier<RepositoryConnection> connectionBuilder;
	private final Map<Transaction, RepositoryConnection> managedConnections = new WeakHashMap<>(512);

	TransactionCoordinator(TransactionManager transactionManager,
			TransactionSynchronizationRegistry transactionSynchronizationRegistry, Supplier<RepositoryConnection> connectionBuilder) {
		this.transactionManager = transactionManager;
		this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
		this.connectionBuilder = connectionBuilder;
	}

	/**
	 * Commits a connection associated with the given transaction. The method is called in before transaction
	 * completion phase of the given transaction.
	 *
	 * @param transaction the transaction being committed
	 */
	void commitTransaction(Transaction transaction) {
		RepositoryConnection connection = managedConnections.get(transaction);
		if (connection != null) {
			if (!connection.isActive()) {
				LOGGER.warn("Tried to commit not active transaction for connection {}",
						System.identityHashCode(connection));
			} else {
				try {
					// this is temporary fix for the SocketTimeoutException until there is proof that someone changes
					// the timeout to value different than zero
					ConnectionChecker.ensureProperRequestTimeout(connection);

					connection.commit();
				} catch (RepositoryException e) {
					if (e.getCause() instanceof SocketTimeoutException) {
						// The data is persisted in the repository but sometimes fails with this and if not cough
						// causes transaction rollback
						LOGGER.trace("Remote commit timeout for connection {}", System.identityHashCode(connection));
						return;
					}
					throw e;
				}
			}
		} else {
			LOGGER.warn("No managed connection to commit for transaction");
		}
	}

	/**
	 * Rollback connection for the given transaction. It's called when the given transaction is beeing rollbacked
	 *
	 * @param transaction the transaction that is rolling back
	 */
	void rollbackTransaction(Transaction transaction) {
		RepositoryConnection connection = managedConnections.get(transaction);
		try {
			if (connection != null) {
				connection.rollback();
			}
		} catch (RepositoryException e) {
			Throwable cause = e.getCause();
			// if the commit fails with socket timeout error then we will have such exception here because the
			// connection is left active and the rollback (caused by some other transaction participant) will cause to
			// fail again as the transaction was already committed.
			if (transactionNotRegistered(cause)) {
				LOGGER.trace("Tried to rollback already committed connection {}", System.identityHashCode(connection));
				return;
			}
			throw new SemanticPersistenceException("Failed to rollback connection", e);
		}
	}

	/**
	 * Close connection associated with the given transaction. The method should be called in the after completion phase
	 * of the given transaction. After this method the coordinator no longer tracks the given transaction.
	 *
	 * @param transaction the transaction that was committed
	 */
	void closeConnection(Transaction transaction) {
		RepositoryConnection connection = managedConnections.remove(transaction);
		if (connection == null) {
			LOGGER.warn("No managed connection to close for transaction");
			return;
		}

		if (connection.isOpen()) {
			try {
				connection.close();
			} catch (RepositoryException e) {
				Throwable cause = e.getCause();
				// if the commit fails with socket timeout error then we will have such exception here because the
				// connection is left active and will issue a rollback on close that will cause to fail again as the
				// transaction was already committed
				if (transactionNotRegistered(cause)) {
					LOGGER.trace("Tried to close connection {} with failed commit operation due to socket timeout",
							System.identityHashCode(connection));
					return;
				}
				throw new SemanticPersistenceException("Failed to rollback connection", e);
			}
		}
	}

	private static boolean transactionNotRegistered(Throwable cause) {
		return cause instanceof RepositoryException && cause.getMessage() != null
				&& cause.getMessage().contains("transaction with id")
				&& cause.getMessage().contains("not registered");
	}

	/**
	 * Checks if there is a connection associated with the given transaction and if that connection is active.
	 *
	 * @param transaction the transaction to check for active connection
	 * @return true if there is associated connection and it's still active
	 */
	boolean isActive(Transaction transaction) {
		RepositoryConnection connection = managedConnections.get(transaction);
		return connection != null && connection.isActive();
	}

	/**
	 * Returns a connection for the current active transaction. If there is no associated connection yet it will be created if
	 * and only if the given transaction is in active state. If there is no active transaction then
	 * {@link TransactionNotActiveException} will be thrown
	 *
	 * @return a connection associated with the current transaction
	 * @throws IllegalStateException if the given transaction is null or not in active state
	 */
	RepositoryConnection getConnection() {
		Transaction transaction = getTx();
		if (transaction == null) {
			throw new TransactionNotActiveException(
					"No active transaction. Cannot produce managed connection outside transaction context");
		}
		return managedConnections.computeIfAbsent(transaction, tx -> {
			try {
				// we cannot allow creating new connection when the transaction is not in proper state
				validateTransaction(tx);
				RepositoryConnection connection = connectionBuilder.get();
				Synchronization synchronization = new RepositoryConnectionSynchronization(tx, this);
				// this synchronization will be called after all other user synchronizations
				// registered via Transaction.registerSynchronization(Synchronization)
				// this is needed in order all other synchronizations to run before flushing the connection
				transactionSynchronizationRegistry.registerInterposedSynchronization(synchronization);
				return connection;
			} catch (RepositoryException e) {
				throw new SemanticPersistenceException(e);
			}
		});
	}

	private static void validateTransaction(Transaction transaction) {
		try {
			int txStatus = transaction.getStatus();
			if (!TxUtils.isActive(txStatus)) {
				throw new TransactionNotActiveException(
						"Cannot produce managed connection for not active transaction in status "
								+ TxUtils.getStatusString(txStatus));
			}
		} catch (SystemException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	/**
	 * Helper method to gets the current active transaction
	 *
	 * @return the current active transaction if any
	 */
	Transaction getTx() {
		try {
			return transactionManager.getTransaction();
		} catch (SystemException e) {
			throw new SemanticPersistenceException("Cannot get the current transaction", e);
		}
	}
}
