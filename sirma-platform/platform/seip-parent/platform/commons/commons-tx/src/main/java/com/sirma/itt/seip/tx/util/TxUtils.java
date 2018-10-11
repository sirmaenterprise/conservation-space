package com.sirma.itt.seip.tx.util;

import javax.transaction.Status;

/**
 * Utilities for working with JTA
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/03/2018
 */
public class TxUtils {
	private static final String[] TX_STATUS = new String[] { "ACTIVE", "MARKED_ROLLBACK", "PREPARED",
			"COMMITTED", "ROLLEDBACK", "UNKNOWN", "NO_TRANSACTION", "PREPARING", "COMMITTING", "ROLLING_BACK" };

	private TxUtils() {
		// utility class
	}

	/**
	 * Returns text representation of the transaction status
	 *
	 * @param txStatus the status code
	 * @return the status string
	 */
	public static String getStatusString(int txStatus) {
		return TX_STATUS[txStatus];
	}

	/**
	 * Checks if the transaction that has the given status is in active state where non transaction finalization
	 * operations are permitted like writing resources to persistent storage
	 *
	 * @param status the status to check
	 * @return true if the given status is considered active
	 */
	public static boolean isActive(int status) {
		return status == Status.STATUS_ACTIVE ||
				status == Status.STATUS_PREPARING;
	}

	/**
	 * Checks if the given status represents a rollback operation
	 *
	 * @param status is the status code to check
	 * @return true if is in rollback mode
	 */
	public static boolean isRollback(int status) {
		return status == Status.STATUS_MARKED_ROLLBACK ||
				status == Status.STATUS_ROLLING_BACK ||
				status == Status.STATUS_ROLLEDBACK;
	}
}
