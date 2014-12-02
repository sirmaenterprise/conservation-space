package com.sirma.itt.emf.resources;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Utility class for helper method for {@link Resource} such as building a display name.
 * 
 * @author BBonev
 */
public class EmfResourcesUtil {

	/**
	 * Instantiates a new emf resources util.
	 */
	private EmfResourcesUtil() {
		// utility class
	}

	/**
	 * Builds the display name from the given map using the keys from {@link ResourceProperties}
	 * 
	 * @param properties
	 *            the properties
	 * @return the string
	 */
	public static String buildDisplayName(Map<String, Serializable> properties) {
		String displayName;
		StringBuilder builder = new StringBuilder();
		String firstName = (String) properties.get(ResourceProperties.FIRST_NAME);
		String lastName = (String) properties.get(ResourceProperties.LAST_NAME);
		boolean hasLastName = StringUtils.isNotNull(lastName) && !"null".equals(lastName);
		if (StringUtils.isNotNull(firstName) && !"null".equals(firstName)) {
			builder.append(firstName);
			if (hasLastName) {
				builder.append(" ").append(lastName);
			}
		}
		if (builder.length() == 0) {
			// if we does not have a first name, but only last we will use it
			if (hasLastName) {
				displayName = lastName;
			} else {
				displayName = (String) properties.get(ResourceProperties.USER_ID);
			}
		} else {
			displayName = builder.toString();
		}
		return displayName;
	}
}
