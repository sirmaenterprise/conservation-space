package com.sirmaenterprise.sep.bpm.camunda.model;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * Common domain model constants used in the {@link Instance} model.
 *
 * @author bbanchev
 */
public class DomainProcessConstants extends ProcessConstants {
	/** Indicates a key for the transitions matrix of any activity instance. */
	public static final String TRANSITIONS = "transitionMatrix";
	/** Indicates a key for the non persistable transitions matrix of any activity instance. */
	public static final String TRANSITIONS_NONPERSISTED = "$transitionMatrix_nonPersistable$";
	/** Indicates a key for an instance currently processed by BPM integration. */
	public static final String ACTIVITY_IN_PROCESS = "$activityIsProcessed_nonPersistable$";
	/** Indicates property to set/get activity completion time. */
	public static final String COMPLETED_ON = "completedOn";
	/** Defines an instance specific subtype as property. */
	public static final String INSTANCE_SUB_TYPE = "subType";

	private DomainProcessConstants() {
		// constant class
	}
}
