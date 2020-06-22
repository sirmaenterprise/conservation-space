package com.sirma.itt.emf.semantic.exception;

/**
 * Exception thrown when managed connection is requested but there is no active transaction or the transaction is
 * in inactive state.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/03/2018
 */
public class TransactionNotActiveException extends SemanticPersistenceException { // NOSONAR

	/**
	 * Instantiate new exception instance
	 *
	 * @param message exception message
	 */
	public TransactionNotActiveException(String message) {
		super(message);
	}
}
