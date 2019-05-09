package com.sirma.itt.emf.cls.validator;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Class implementation of {@link CodeValidator}. This specific validation implementation validates the code values or
 * lists by their value or name uniqueness and pattern matching for each field of the code value or code list
 *
 * @author svetlozar.iliev
 */
public class CodeValidatorImpl implements CodeValidator {

	private static final String ITEM_DELIMITER = ",\u0020";

	@Override
	public void validateCodeList(CodeList codeList) {
		validateCodeLists(Collections.singletonList(codeList));
	}

	@Override
	public void validateCodeValue(CodeValue codeValue) {
		validateCodeValues(Collections.singletonList(codeValue));
	}

	@Override
	public void validateCodeLists(List<CodeList> codeLists) {
		Objects.requireNonNull(codeLists);

		List<String> errors = validateCodes(codeLists);
		codeLists.forEach(l -> errors.addAll(validateCodes(l.getValues())));
		if (!errors.isEmpty()) {
			throw new CodeValidatorException("Code list validation error", errors);
		}
	}

	@Override
	public void validateCodeValues(List<CodeValue> codeValues) {
		Objects.requireNonNull(codeValues);

		List<String> errors = validateCodes(codeValues);
		if (!errors.isEmpty()) {
			throw new CodeValidatorException("Code value validation error", errors);
		}
	}

	/**
	 * Validates a collection of codes. A set off error messages might be returned if there are any validation errors
	 * When no validation errors are present the error set is empty
	 * 
	 * @param codes
	 *            the collection of codes
	 * @return set of possible error messages occurred during the validation
	 */
	private static List<String> validateCodes(List<? extends Code> codes) {
		List<String> errors = new ArrayList<>();

		if (codes != null && !codes.isEmpty()) {
			for (Code codeList : codes) {
				errors.addAll(validateCodeFields(codeList));
			}

			if (codes.size() > 1) {
				errors.addAll(validateCodeUniqueness(codes));
			}
		}
		return errors;
	}

	/**
	 * Validates a single code and it's fields. A set off error messages might be returned if there are any validation
	 * errors When no validation errors are present the error set is empty
	 * 
	 * @param code
	 *            the code to be validated
	 * @return set of possible error messages occurred during the validation
	 */
	private static <T extends Code> List<String> validateCodeFields(T code) {
		List<String> errors = new ArrayList<>();
		CLColumn valueColumn = isCodeList(code) ? CLColumn.CL_VALUE : CLColumn.CV_VALUE;

		addNonNullValue(errors, validateField(code.getValue(), valueColumn, code.getValue()));
		addNonNullValue(errors, validateField(code.getExtra1(), CLColumn.EXTRA1, code.getValue()));
		addNonNullValue(errors, validateField(code.getExtra2(), CLColumn.EXTRA2, code.getValue()));
		addNonNullValue(errors, validateField(code.getExtra3(), CLColumn.EXTRA3, code.getValue()));

		for (CodeDescription description : code.getDescriptions()) {
			addNonNullValue(errors, validateField(description.getName(), CLColumn.DESCR, code.getValue()));
			addNonNullValue(errors, validateField(description.getComment(), CLColumn.COMMENT, code.getValue()));
			addNonNullValue(errors, validateField(description.getLanguage(), CLColumn.LANGUAGE, code.getValue()));
		}
		return errors;
	}

	/**
	 * Validates the uniqueness of a code collection. A set off error messages might be returned if there are any
	 * validation errors When no validation errors are present the error set is empty
	 * 
	 * @param codes
	 *            the code collection to be validated
	 * @return set of possible error messages occurred during the validation
	 */
	private static List<String> validateCodeUniqueness(List<? extends Code> codes) {
		List<String> errors = new ArrayList<>();
		Map<String, List<Code>> valuesMap = new CaseInsensitiveMap<>();
		Map<CodeDescription, List<Code>> namesMap = new HashMap<>();

		for (Code code : codes) {
			String valueKey = createValueKey(code);
			valuesMap.computeIfAbsent(valueKey, key -> new ArrayList<>()).add(code);

			for (CodeDescription descr : code.getDescriptions()) {
				if (StringUtils.isBlank(descr.getName())) {
					continue;
				}
				CodeDescription descrKey = createDescriptionKey(descr);
				namesMap.computeIfAbsent(descrKey, key -> new ArrayList<>()).add(code);
			}
		}
		removeSingleValuedEntries(namesMap);
		removeSingleValuedEntries(valuesMap);

		valuesMap.forEach((k, v) -> errors.add("Duplicate identifier " + k + " found for following codes: " + stringify(v)));
		namesMap.forEach((k, v) -> errors.add(
				"Duplicate name " + k.getName() + " found for language " + k.getLanguage() + " in the following codes: " + stringify(v)));
		return errors;
	}

	/**
	 * Validates a single field's content based on a specific {@link CLColumn} restrictions
	 * 
	 * @param content
	 *            the content to be validated
	 * @param field
	 *            the field / column restrictions
	 * @param code
	 *            the code identifier to which this field belongs
	 * @return error message or null if no error has occurred during validation
	 */
	private static String validateField(String content, CLColumn field, String code) {
		if ((content == null || content.isEmpty()) && field.isMandatory()) {
			return "Content for " + field + " field is mandatory for code with identifier: " + code;
		} else if (content != null && !content.matches(field.getPattern())) {
			return "Content in " + field + " field does not match pattern for code with identifier: " + code;
		}
		return null;
	}

	/**
	 * Converts a collection of codes to a delimited string of their code values.
	 * 
	 * @param codes
	 *            collection of valid codes
	 * @return the delimited string of the codes' values
	 */
	private static String stringify(List<Code> codes) {
		return codes.stream().map(Code::getValue).collect(Collectors.joining(ITEM_DELIMITER));
	}

	/**
	 * Creates a description as a key from a given description, consisting of the name and language only
	 * 
	 * @param createFrom
	 *            the description for which to build the description name and language key
	 * @return the key
	 */
	private static CodeDescription createDescriptionKey(CodeDescription createFrom) {
		CodeDescription description = new CodeDescription();
		description.setName(createFrom.getName());
		description.setLanguage(createFrom.getLanguage().toUpperCase());
		return description;
	}

	/**
	 * Creates a string key from the value identifier of a give code
	 * 
	 * @param code
	 *            the code for which to build the identifier key
	 * @return the key
	 */
	private static String createValueKey(Code code) {
		return code.getValue().toUpperCase();
	}

	/**
	 * Removes those key and value pairs from a given mapping for which the values have single entries
	 * 
	 * @param mapping
	 *            the key value pairs mapping
	 */
	private static void removeSingleValuedEntries(Map<?, List<Code>> mapping) {
		mapping.entrySet().removeIf(v -> v.getValue().size() <= 1);
	}

	/**
	 * Checks if the given code is an instance of {@link CodeList}
	 * 
	 * @param code
	 *            the code to be checked
	 * @return true if instance of {@link CodeList}, false otherwise
	 */
	private static <T extends Code> boolean isCodeList(T code) {
		return code instanceof CodeList;
	}
}
