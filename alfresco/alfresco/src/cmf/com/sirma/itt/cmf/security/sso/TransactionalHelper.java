package com.sirma.itt.cmf.security.sso;

import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class TransactionalHelper.
 */
public class TransactionalHelper {

	/** The logger. */
	private static Log logger = LogFactory.getLog(TransactionalHelper.class);

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
		} catch (Throwable ex) {
			logger.error(ex);
			try {
				tx.rollback();
			} catch (Exception ex2) {
				logger.error("Failed to rollback transaction", ex2);
			}

			if ((ex instanceof RuntimeException)) {
				throw ((RuntimeException) ex);
			}
			throw new RuntimeException("Failed to execute transactional method", ex);
		}

		return result;
	}
}

/*
 * Location:
 * W:\CMF\test\WebContent\WEB-INF\lib\alfresco-opensso-webclient-0.8.jar
 * Qualified Name: com.sourcesense.alfresco.transaction.TransactionalHelper
 * JD-Core Version: 0.6.0
 */