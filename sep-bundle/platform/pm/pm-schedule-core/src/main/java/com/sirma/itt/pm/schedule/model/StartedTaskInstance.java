package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class StartedTaskInstance.
 *
 * @author BBonev
 */
public class StartedTaskInstance extends StartedInstance {

	/**
	 * Instantiates a new started task instance.
	 *
	 * @param target
	 *            the target
	 */
	public StartedTaskInstance(Instance target) {
		super(target);
	}

	/**
	 * Instantiates a new started task instance.
	 */
	public StartedTaskInstance() {
		// default constructor
	}
}