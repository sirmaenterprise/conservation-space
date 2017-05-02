package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.context.Config;
import com.sirma.itt.seip.context.RuntimeContext;
import com.sirma.itt.seip.db.DbIdGenerator;

/**
 * Holder class to store the current transaction id in a thread local store.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TransactionIdHolder {

	/** The transaction id configuration */
	private static final Config TRANSACTION_ID = RuntimeContext.createConfig("TRANSACTION_ID", true);

	private DbIdGenerator idGenerator;

	/**
	 * Instantiates a new transaction id holder.
	 */
	public TransactionIdHolder() {
		// default constructor
	}

	/**
	 * Instantiates a new transaction id holder.
	 *
	 * @param idGenerator
	 *            the id generator
	 */
	@Inject
	public TransactionIdHolder(DbIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	/**
	 * Creates new transaction id. The id is not set in the context! After this call the method
	 * {@link #isTransactionActive()} will still return <code>false</code> and the {@link #getTransactionId()} will
	 * return <code>null</code>.
	 *
	 * @return a transaction id
	 */
	public String createTransactionId() {
		return idGenerator.generateId().toString();
	}

	/**
	 * Checks if is transaction active. If this method returns <code>true</code> then calling the method
	 * {@link #getTransactionId()} will not return <code>null</code>.
	 *
	 * @return true, if is transaction active
	 */
	public boolean isTransactionActive() {
		return getTransactionId() != null;
	}

	/**
	 * Gets the transaction id.
	 *
	 * @return the transaction id
	 */
	public Serializable getTransactionId() {
		return TRANSACTION_ID.get();
	}

	/**
	 * Sets the transaction id.
	 *
	 * @param externalId
	 *            the new transaction id
	 */
	public void setTransactionId(String externalId) {
		TRANSACTION_ID.set(externalId);
	}

	/**
	 * Clear current id.
	 */
	public void clearCurrentId() {
		// drain all possible sets of the configuration until empty for the current thread
		do {
			TRANSACTION_ID.clear();
		} while (isTransactionActive());
	}

}
