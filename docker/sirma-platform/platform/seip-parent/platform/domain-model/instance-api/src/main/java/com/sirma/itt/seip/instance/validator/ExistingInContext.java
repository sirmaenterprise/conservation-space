package com.sirma.itt.seip.instance.validator;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Defines all possible values of  {@link DefaultProperties#EXISTING_IN_CONTEXT} field.
 *
 * @author Boyan Tonchev.
 */
public enum ExistingInContext {
	/**
	 * Possible value of {@link DefaultProperties#EXISTING_IN_CONTEXT}. It means that instances with this value can
	 * exist in context and without context.
	 */
	BOTH,

	/**
	 * Possible value of {@link DefaultProperties#EXISTING_IN_CONTEXT}. It means that instances with this value can
	 * exist in context only.
	 */
	IN_CONTEXT,

	/**
	 * Possible value of {@link DefaultProperties#EXISTING_IN_CONTEXT}. It means that instances with this value can
	 * exist without context only.
	 */
	WITHOUT_CONTEXT;
}