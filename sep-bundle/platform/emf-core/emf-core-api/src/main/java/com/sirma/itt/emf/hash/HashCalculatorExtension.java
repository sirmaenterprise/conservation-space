package com.sirma.itt.emf.hash;

import com.sirma.itt.emf.plugin.SupportablePlugin;

/**
 * Extension for {@link HashCalculator} to provide concrete hash computations for objects
 * 
 * @author BBonev
 */
public interface HashCalculatorExtension extends SupportablePlugin {
	/** The target name. */
	String TARGET_NAME = "HashCalculator";

	/**
	 * Compute hash of the given object if supported. If the object is not supported then
	 * <code>null</code> can be returned
	 * 
	 * @param calculator
	 *            the calculator instance to use when calculate hashes so the extension can have
	 *            access to other types
	 * @param object
	 *            the object to compute hash of
	 * @return the integer representing the hash of the object or <code>null</code> if not supported
	 */
	public Integer computeHash(HashCalculator calculator, Object object);
}
