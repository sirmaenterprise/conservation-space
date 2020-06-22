package com.sirma.itt.seip.configuration.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Provides helper methods for configuration building
 *
 * @author BBonev
 */
public class ReflectionHelper {

	/**
	 * Instantiates a new reflection helper.
	 */
	private ReflectionHelper() {
		// utility class
	}

	/**
	 * Determine configuration name for the given member.
	 *
	 * @param annotationNameValue
	 *            the annotation name value
	 * @param definedOn
	 *            the defined on
	 * @return the string
	 */
	public static String determineConfigName(String annotationNameValue, Member definedOn) {
		if (definedOn instanceof Method) {
			return determineConfigName(annotationNameValue, (Method) definedOn);
		} else if (definedOn instanceof Field) {
			return determineConfigName(annotationNameValue, (Field) definedOn);
		}
		throw new IllegalArgumentException("Unsupported member type " + definedOn);
	}

	/**
	 * Determine configuration name.
	 *
	 * @param annotationNameValue
	 *            the annotation name value
	 * @param definedOn
	 *            the defined on
	 * @return the string
	 */
	public static String determineConfigName(String annotationNameValue, Field definedOn) {
		if ("".equals(annotationNameValue)) {
			if (Modifier.isStatic(definedOn.getModifiers())) {
				Object fieldvalue;
				try {
					definedOn.setAccessible(true);
					fieldvalue = definedOn.get(definedOn.getDeclaringClass());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalStateException("Failed to access field " + definedOn.getDeclaringClass().getName()
							+ "." + definedOn.getName(), e);
				}

				if (fieldvalue instanceof String && !fieldvalue.toString().isEmpty()) {
					return fieldvalue.toString();
				}
				throw new IllegalStateException(
						"Configuration is defined on a field that does not not have a value or is not a non empty string!");
			}
			throw new IllegalStateException(
					"Configuration is defined on a non static field and there is no configuration name!");
		}
		return annotationNameValue;
	}

	/**
	 * Determine configuration name.
	 *
	 * @param annotationNameValue
	 *            the annotation name value
	 * @param definedOn
	 *            the defined on
	 * @return the string
	 */
	public static String determineConfigName(String annotationNameValue, Method definedOn) {
		if ("".equals(annotationNameValue)) {
			throw new IllegalStateException("Configuration is defined on a method and there is no configuration name!");
		}
		return annotationNameValue;
	}

}
