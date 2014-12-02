package com.sirma.itt.emf.hash;

import java.util.List;

import com.sirma.itt.emf.domain.Pair;

/**
 * Class for calculation of hash codes of different complex objects. When the hash code is not just
 * a sum of all fields. The service is extendible via extension {@link HashCalculatorExtension}
 * 
 * @author BBonev
 */
public interface HashCalculator {

	/**
	 * Compute hash of the given object
	 * 
	 * @param object
	 *            the object
	 * @return the integer
	 */
	Integer computeHash(Object object);

	/**
	 * Equals by hash.
	 * 
	 * @param object1
	 *            the object1
	 * @param object2
	 *            the object2
	 * @return true, if successful
	 */
	boolean equalsByHash(Object object1, Object object2);

	/**
	 * Sets the statistics enabled.
	 * 
	 * @param enabled
	 *            the new statistics enabled
	 */
	void setStatisticsEnabled(boolean enabled);

	/**
	 * Gets the statistics.
	 * 
	 * @param reset
	 *            the reset
	 * @return the statistics
	 */
	List<Pair<String, String>> getStatistics(boolean reset);
}
