package com.sirma.itt.emf.cls.validator;

import java.io.InputStream;

import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;

import jxl.Sheet;

/**
 * SheetValidator validating files presented as an input stream, which contain a collection of code lists
 *
 * @author Mihail Radkov
 * @author svetlozar.iliev
 */
public interface SheetValidator {

	/**
	 * Validates provided file via an input stream, extracts all of the code lists and validates them.
	 *
	 * @param inputStream
	 *            the file as input stream
	 * @return the extracted sheet or null if does not exist
	 * @throws SheetValidatorException
	 *             if the validating encounters a problem
	 */
	Sheet getValidatedCodeListSheet(InputStream inputStream) throws SheetValidatorException;

}
