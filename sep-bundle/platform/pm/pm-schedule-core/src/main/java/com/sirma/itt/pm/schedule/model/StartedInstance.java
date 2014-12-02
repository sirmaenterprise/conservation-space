package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * DAO object to initiate a start of an schedule entry. The object is used as a destination for
 * {@link com.sirma.itt.emf.converter.TypeConverter} call to execute the algorithm to start/commit a
 * {@link ScheduleEntity}.
 * 
 * @author BBonev
 */
public class StartedInstance {

	/** The target. */
	private Instance target;

	/**
	 * Instantiates a new started instance.
	 */
	public StartedInstance() {
	}

	/**
	 * Instantiates a new started schedule entry.
	 * 
	 * @param target
	 *            the target
	 */
	public StartedInstance(Instance target) {
		this.target = target;
	}

	/**
	 * Getter method for target.
	 * 
	 * @return the target
	 */
	public Instance getTarget() {
		return target;
	}

	/**
	 * Setter method for target.
	 * 
	 * @param target
	 *            the target to set
	 */
	public void setTarget(Instance target) {
		this.target = target;
	}

}
