package com.sirma.itt.emf.cls.validator;

import java.io.InputStream;

import jxl.Sheet;

/**
 * Interface for validating excel files that contains code lists and values.
 *
 * @author Mihail Radkov
 */
public interface XLSValidator {

	/**
	 * Validates provided excel file via an input stream, extracts the code list sheet and validates it.
	 *
	 * @param inputStream
	 *            the excel file as input stream
	 * @return the extracted sheet or null if does not exist
	 * @throws XLSValidatorException
	 *             if the validating encounters a problem
	 */
	Sheet getValidatedCodeListSheet(InputStream inputStream) throws XLSValidatorException;

}
