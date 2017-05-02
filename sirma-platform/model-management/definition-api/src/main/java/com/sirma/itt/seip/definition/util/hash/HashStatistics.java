package com.sirma.itt.seip.definition.util.hash;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Hash statistics thread local cache.
 *
 * @author BBonev
 */
public class HashStatistics {

	/** The enable statistics. */
	private static final ThreadLocal<Boolean> ENABLE_STATISTICS = ThreadLocal.withInitial(() -> Boolean.FALSE);
	/** The statistics. */
	private static final ThreadLocal<List<String>> STATISTICS = ThreadLocal.withInitial(LinkedList::new);

	/**
	 * Instantiates a new hash statistics.
	 */
	private HashStatistics() {
		// utility class
	}

	/**
	 * Sets the statistics enabled.
	 *
	 * @param enabled
	 *            the new statistics enabled
	 */
	public static void setStatisticsEnabled(boolean enabled) {
		ENABLE_STATISTICS.set(Boolean.valueOf(enabled));
	}

	/**
	 * Sets the statistics enabled.
	 *
	 * @return true, if is statistics enabled
	 */
	public static boolean isStatisticsEnabled() {
		return ENABLE_STATISTICS.get().booleanValue();
	}

	/**
	 * Gets the statistics and reset them. Second call to this method will return empty list.
	 *
	 * @return the statistics
	 */
	public static List<String> getStatisticsAndReset() {
		try {
			// we clone the list if we are going to clean it after the method end
			return new ArrayList<>(STATISTICS.get());
		} finally {
			STATISTICS.remove();
		}
	}

	/**
	 * Gets the statistics.
	 *
	 * @return the statistics
	 */
	public static List<String> getStatistics() {
		return STATISTICS.get();
	}
}
