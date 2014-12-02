package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class StartedStandaloneTaskInstance.
 *
 * @author BBonev
 */
public class StartedStandaloneTaskInstance extends StartedInstance {

	/**
	 * Instantiates a new started standalone task instance.
	 *
	 * @param target
	 *            the target
	 */
	public StartedStandaloneTaskInstance(Instance target) {
		super(target);
	}

	/**
	 * Instantiates a new started standalone task instance.
	 */
	public StartedStandaloneTaskInstance() {
		// default constructor
	}
}