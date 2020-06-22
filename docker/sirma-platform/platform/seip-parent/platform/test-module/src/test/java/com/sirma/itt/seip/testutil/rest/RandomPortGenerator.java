package com.sirma.itt.seip.testutil.rest;

import java.security.SecureRandom;

/**
 * Generate a random port based on given port range. This will allow parallel running of same tests on one machine.
 *
 * @author BBonev
 */
public class RandomPortGenerator {

	/**
	 * Generate random port in the given range
	 *
	 * @param low
	 *            low port bounds
	 * @param high
	 *            high port bounds
	 * @return a random port
	 */
	public static int generatePort(int low, int high) {
		if (low >= high) {
			throw new IllegalArgumentException("Invalid port range [" + low + ", " + high + "]");
		}
		SecureRandom random = new SecureRandom();
		random.nextInt();
		int generated;
		while ((generated = random.nextInt(high)) < low) {
			// nothing to do
		}
		return generated;
	}
}
