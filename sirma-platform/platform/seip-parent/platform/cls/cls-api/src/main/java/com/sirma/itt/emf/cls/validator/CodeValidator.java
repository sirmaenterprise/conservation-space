package com.sirma.itt.emf.cls.validator;

import java.util.List;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * CodeValidator for validating code lists or code values.
 * 
 * @author svetlozar.iliev
 */
public interface CodeValidator {

	/**
	 * Validates a single code list. Code list validation depends on the specific implementation. This interface does
	 * not in any way restrict the implementation of the validation
	 * 
	 * @param codeList
	 *            the code list to be validated
	 * @throws CodeValidatorException
	 *             thrown when the validation for the code list fails.
	 */
	void validateCodeList(CodeList codeList) throws CodeValidatorException;

	/**
	 * Validates a single code value. Code value validation depends on the specific implementation. This interface does
	 * not in any way restrict the implementation of the validation
	 * 
	 * @param codeValue
	 *            the code value to be validated
	 * @throws CodeValidatorException
	 *             thrown when the validation for the code value fails.
	 */
	void validateCodeValue(CodeValue codeValue) throws CodeValidatorException;

	/**
	 * Validates a collection of code lists. Code lists validation depends on the specific implementation. This
	 * interface does not in any way restrict the implementation of the validation
	 * 
	 * @param codeLists
	 *            the code lists to be validated
	 * @throws CodeValidatorException
	 *             thrown when the validation for the code lists fails.
	 */
	void validateCodeLists(List<CodeList> codeLists) throws CodeValidatorException;

	/**
	 * Validates a collection of code values. Code values validation depends on the specific implementation. This
	 * interface does not in any way restrict the implementation of the validation
	 * 
	 * @param codeValues
	 *            the code values to be validated
	 * @throws CodeValidatorException
	 *             thrown when the validation for the code values fails.
	 */
	void validateCodeValues(List<CodeValue> codeValues) throws CodeValidatorException;
}
