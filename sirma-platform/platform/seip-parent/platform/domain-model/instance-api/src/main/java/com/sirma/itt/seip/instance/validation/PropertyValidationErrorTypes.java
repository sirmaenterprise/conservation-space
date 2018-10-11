package com.sirma.itt.seip.instance.validation;

/**
 * Contains the possible error types that can be returned from the validation service. Used in
 * {@link InstanceValidationResult}, for more information see there.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 16/07/2017
 */
public class PropertyValidationErrorTypes {
	public static final String INVALID_BOOLEAN = "INVALID_BOOLEAN";
	public static final String INVALID_CODELIST = "INVALID_CODELIST";
	public static final String INVALID_TEXT_FORMAT = "INVALID_TEXT_FORMAT";
	public static final String MISSING_MANDATORY_PROPERTY = "MISSING_MANDATORY_PROPERTY";
	public static final String INVALID_NUMBER = "INVALID_NUMBER";
	public static final String INVALID_URI = "INVALID_URI";
	public static final String INVALID_DATE = "INVALID_DATE";
	public static final String INVALID_SINGLE_VALUE = "INVALID_SINGLE_VALUE";

	private PropertyValidationErrorTypes() {
		// constants only file
	}
}
