package com.sirma.itt.cmf.security.sso;

import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;

/**
 * The Class TransactionalHelper.
 */
public class TransactionalHelper {

	/** The logger. */
	private static final Log LOGGER = LogFactory.getLog(TransactionalHelper.class);

	/** The transaction service. */
	private TransactionService transactionService;

	/**
	 * Instantiates a new transactional helper.
	 * 
	 * @param txService
	 *            the tx service
	 */
	public TransactionalHelper(TransactionService txService) {
		this.transactionService = txService;
	}

	/**
	 * Do in transaction.
	 * 
	 * @param callback
	 *            the callback
	 * @return the object
	 */
	public Object doInTransaction(Transactionable callback) {
		UserTransaction tx = this.transactionService.getUserTransaction();
		Object result;
		try {
			tx.begin();
			result = callback.execute();
			tx.commit();
		} catch (Exception ex) {
			LOGGER.error(ex);
			try {
				tx.rollback();
			} catch (Exception ex2) {
				LOGGER.error("Failed to rollback transaction", ex2);
			}
			throw new SEIPRuntimeException("Failed to execute transactional method", ex);
		}

		return result;
	}
}
