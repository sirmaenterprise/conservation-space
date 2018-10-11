package com.sirmaenterprise.sep.bpm.camunda.util;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.ACTIVITY_IN_PROCESS;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.COMPLETED_ON;

import java.util.Objects;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * BPM activity util used by Camunda integration for various utility methods related to activity instance operations and
 * checks.
 * 
 * @author bbanchev
 */
public class BPMActivityUtil {

	private BPMActivityUtil() {
		// utility class
	}

	/**
	 * Check if instance is considered active.
	 * 
	 * @param activity
	 *            the instance to check
	 * @return true if activity is completed
	 */
	public static boolean isActivityInProgress(Instance activity) {
		Objects.requireNonNull(activity, "Instance is a required to check if activity is in progress!");
		if (activity.get(ACTIVITY_IN_PROCESS) != null) {
			return true;
		}
		return !isActivityCompleted(activity);
	}

	/**
	 * Check if instance is completed.
	 * 
	 * @param activity
	 *            the instance to check
	 * @return true if activity is completed
	 */
	public static boolean isActivityCompleted(Instance activity) {
		Objects.requireNonNull(activity, "Instance is a required to check if activity is completed!");
		return activity.get(COMPLETED_ON) != null;
	}
}
