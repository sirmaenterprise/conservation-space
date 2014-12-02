package com.sirma.itt.emf.hash;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.domain.Pair;

/**
 * The HashStatistics.
 * 
 * @author BBonev
 */
public class HashStatistics {

	/** The enable statistics. */
	protected static ThreadLocalBoolean enableStatistics = new ThreadLocalBoolean();
	/** The statistics. */
	protected static ThreadLocalList statistics = new ThreadLocalList();

	/**
	 * Sets the statistics enabled.
	 * 
	 * @param enabled
	 *            the new statistics enabled
	 */
	public static void setStatisticsEnabled(boolean enabled) {
		enableStatistics.set(enabled);
	}

	/**
	 * Sets the statistics enabled.
	 * 
	 * @return true, if is statistics enabled
	 */
	public static boolean isStatisticsEnabled() {
		return (enableStatistics.get() != null) || enableStatistics.get();
	}

	/**
	 * Gets the statistics.
	 * 
	 * @param reset
	 *            the reset
	 * @return the statistics
	 */
	public static List<Pair<String, String>> getStatistics(boolean reset) {
		try {
			List<Pair<String, String>> list = statistics.get();
			return list;
		} finally {
			if (reset) {
				statistics.remove();
			}
		}
	}

	/**
	 * The Class ThreadLocalBoolean.
	 */
	static class ThreadLocalBoolean extends ThreadLocal<Boolean> {

		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	}

	/**
	 * The Class ThreadLocalList.
	 */
	static class ThreadLocalList extends ThreadLocal<List<Pair<String, String>>> {

		@Override
		protected List<Pair<String, String>> initialValue() {
			return new ArrayList<>(500);
		}
	}
}
