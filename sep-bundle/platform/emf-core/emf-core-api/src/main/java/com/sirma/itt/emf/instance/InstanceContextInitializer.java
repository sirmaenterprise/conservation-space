package com.sirma.itt.emf.instance;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * Responsible for restoring of context hierarchy of a given instance.
 * 
 * @author svelikov
 */
public interface InstanceContextInitializer {

	/**
	 * Restore hierarchy.
	 * 
	 * @param selectedInstance
	 *            the selected trough web link instance
	 * @param context
	 *            the context if is known in advance
	 */
	void restoreHierarchy(Instance selectedInstance, Instance context);
}
