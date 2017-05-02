package com.sirmaenterprise.sep.bpm.model;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Common domain model constants used in the {@link Instance} model.
 *
 * @author bbanchev
 */
public class ProcessConstants {
	/** Indicates a key for the task instance id or process execution id. */
	public static final String ACTIVITY_ID = "activityId";
	/** Indicates a key for the transition id taken from activity. */
	public static final String OUTCOME = "outcome";

	/**
	 * Initialize as utility class
	 */
	protected ProcessConstants() {
		// constant class
	}
}
