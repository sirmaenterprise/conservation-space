package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class StartedWorkflowInstance.
 *
 * @author BBonev
 */
public class StartedWorkflowInstance extends StartedInstance {

	/**
	 * Instantiates a new started workflow instance.
	 *
	 * @param target
	 *            the target
	 */
	public StartedWorkflowInstance(Instance target) {
		super(target);
	}

	/**
	 * Instantiates a new started workflow instance.
	 */
	public StartedWorkflowInstance() {
		// default constructor
	}
}