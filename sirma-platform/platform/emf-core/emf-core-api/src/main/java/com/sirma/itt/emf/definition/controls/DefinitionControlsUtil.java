package com.sirma.itt.emf.definition.controls;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.definition.ControlParam;

/**
 * Utility functions to be used when work with control definitions.
 *
 * @author svelikov
 */
public final class DefinitionControlsUtil {

	/**
	 * Private constructor.
	 */
	private DefinitionControlsUtil() {
		// this is utility class
	}

	/**
	 * Gets the parameters by name.
	 *
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the parameters by name
	 */
	public static List<ControlParam> getParametersByName(String requestedParameterName,
			List<ControlParam> controlParams) {

		List<ControlParam> foundParameters = new ArrayList<>();

		if (controlParams != null && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {

				if (StringUtils.isNotNullOrEmpty(requestedParameterName)
						&& currentControlParam.getName().endsWith(requestedParameterName)) {

					foundParameters.add(currentControlParam);
				}
			}
		}
		return foundParameters;
	}

	/**
	 * Gets the first found control parameter from the list.
	 *
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the control parameter or null if no parameter is found
	 */
	public static ControlParam getControlParameter(String requestedParameterName, List<ControlParam> controlParams) {
		ControlParam controlParam = null;
		if (controlParams != null && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {
				if (currentControlParam.getName().endsWith(requestedParameterName)) {
					controlParam = currentControlParam;
					break;
				}
			}
		}
		return controlParam;
	}

	/**
	 * Gets the control parameter value.
	 *
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the control parameter value or null
	 */
	public static String getControlParameterValue(String requestedParameterName, List<ControlParam> controlParams) {
		return getControlParamValue(requestedParameterName, controlParams);
	}

	/**
	 * Gets the control parameter value if exists or return the default value untouched.
	 *
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @param defaultValue
	 *            the default value
	 * @return the control parameter value
	 */
	public static String getControlParameterValue(String requestedParameterName, List<ControlParam> controlParams,
			String defaultValue) {
		String controlParamValue = getControlParamValue(requestedParameterName, controlParams);
		if (controlParamValue == null) {
			return defaultValue;
		}
		return controlParamValue;

	}

	/**
	 * Gets the control param value.
	 *
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the control param value
	 */
	private static String getControlParamValue(String requestedParameterName, List<ControlParam> controlParams) {
		ControlParam controlParam = null;
		if (controlParams != null && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {
				String name = currentControlParam.getName();
				if (name != null && name.endsWith(requestedParameterName)) {
					controlParam = currentControlParam;
					break;
				}
			}
		}
		String value = null;
		if (controlParam != null && controlParam.getValue() != null) {
			value = controlParam.getValue();
		}
		return value;
	}

}
