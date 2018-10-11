package com.sirmaenterprise.sep.bpm.camunda.util;

import java.util.Objects;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Utility class to work with business objects identifiers based on current design implementation.
 *
 * @author bbanchev
 */
public class ActivityIdUtil {

	private ActivityIdUtil() {
		// utility class
	}

	/**
	 * Checks if business objects is skipped based on its id. All such objects ids should start with <code>skip_</code>
	 *
	 * @param bpmId
	 *            the business object id
	 * @return true if object is considered skipped for processing or false otherwise
	 */
	public static boolean isSkipped(String bpmId) {
		Objects.requireNonNull(bpmId, "Activity/Transition id is a required argument!");
		return bpmId.startsWith("skip_");
	}

	/**
	 * Generates and id that is considered skipped from processing. See {@link #isSkipped(String)}
	 * 
	 * @param bpmId
	 *            is the original id
	 * @return the new id considered as skipped from processing
	 */
	public static String markSkipped(String bpmId) {
		Objects.requireNonNull(bpmId, "Activity/Transition id is a required argument!");
		if (isSkipped(bpmId)) {
			return bpmId;
		}
		return "skip_" + bpmId;
	}

	/**
	 * Extract the business id from execution/task/transition element. Example of valid values are
	 * <code><ul><li>TASK100</li><li>TASK100_uid</li></ul></code>
	 *
	 * @param bpmId
	 *            the business object id
	 * @return the business id or throws exception on invalid id, never null
	 */
	public static String extractBusinessId(String bpmId) {
		Objects.requireNonNull(bpmId, "Activity/Transition id is a required argument to extract business id!");
		if (bpmId.contains("_")) {
			String[] typeAndUniqueId = bpmId.split("_");
			if (typeAndUniqueId.length < 1 || typeAndUniqueId.length > 2) {
				throw new CamundaIntegrationRuntimeException("Unsupported execution/task/transition id: " + bpmId
						+ ". Accepted format is DefinitionID or DefinitionID_UniqueID!");
			}
			return typeAndUniqueId[0];
		}
		return bpmId;
	}

	/**
	 * Extract the definition id and the subtype id from business id. Example of valid values are
	 * <code><ul><li>TASK100-subTaskId1</li><li>TASK100</li></ul></code>
	 *
	 * @param businessId
	 *            the business object id extracted using {@link #extractBusinessId(String)}
	 * @return the business id in [0] and optionally in [1] the subtype
	 */
	public static String[] getTypeAndSubtype(String businessId) {
		Objects.requireNonNull(businessId,
				"Valid business id is a required argument to find definiton and sub definition id!");
		return businessId.split("-");
	}
}
