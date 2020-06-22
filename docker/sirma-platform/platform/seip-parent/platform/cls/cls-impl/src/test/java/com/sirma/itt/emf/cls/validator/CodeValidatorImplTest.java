package com.sirma.itt.emf.cls.validator;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Tests server side validations for creating and updating {@link CodeList}.
 */
public class CodeValidatorImplTest {

	private CodeValidatorImpl codeListValidator;

	@Before
	public void beforeEach() {
		codeListValidator = new CodeValidatorImpl();
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_list_name_uniqueness() throws CodeValidatorException {

		CodeList first = getCodeList("1");
		CodeList second = getCodeList("2");

		first.setDescriptions(Arrays.asList(getDescription("EN", "Case")));
		second.setDescriptions(Arrays.asList(getDescription("EN", "Case")));

		codeListValidator.validateCodeLists(Arrays.asList(first, second));
	}

	@Test
	public void should_not_throw_validation_error_when_validating() throws CodeValidatorException {
		CodeList first = getCodeList("1");
		first.setDescriptions(Arrays.asList(getDescription("EN", "PR")));
		first.setValues(Arrays.asList(getCodeValue("PR01"), getCodeValue("PR02")));
		first.getValues().get(0).setDescriptions(Arrays.asList(getDescription("EN", "PR1")));
		first.getValues().get(1).setDescriptions(Arrays.asList(getDescription("EN", "PR2")));

		CodeList second = getCodeList("2");
		second.setDescriptions(Arrays.asList(getDescription("EN", "CS")));
		second.setValues(Arrays.asList(getCodeValue("CS01"), getCodeValue("CS02")));
		second.getValues().get(0).setDescriptions(Arrays.asList(getDescription("EN", "CS1")));
		second.getValues().get(1).setDescriptions(Arrays.asList(getDescription("EN", "CS2")));

		codeListValidator.validateCodeLists(Arrays.asList(first, second));
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating() throws CodeValidatorException {
		CodeList first = getCodeList("1");
		first.setDescriptions(Arrays.asList(getDescription("EN", "PR")));
		first.setValues(Arrays.asList(getCodeValue("PR02"), getCodeValue("PR02")));
		first.getValues().get(0).setDescriptions(Arrays.asList(getDescription("EN", "PR1")));
		first.getValues().get(1).setDescriptions(Arrays.asList(getDescription("EN", "PR2")));

		CodeList second = getCodeList("@##$#");
		second.setDescriptions(Arrays.asList(getDescription("EN", "CS")));
		second.setValues(Arrays.asList(getCodeValue("CS02"), getCodeValue("CS02")));
		second.getValues().get(0).setDescriptions(Arrays.asList(getDescription("EN", "CS1")));
		second.getValues().get(1).setDescriptions(Arrays.asList(getDescription("EN", "CS2")));

		codeListValidator.validateCodeLists(Arrays.asList(first, second));
	}

	@Test
	public void should_not_throw_validation_error_when_validating_code_value() throws CodeValidatorException {
		CodeValue value = getCodeValue("PR0001");
		value.setDescriptions(Arrays.asList(getDescription("EN", "Project"), getDescription("BG", "Проект")));
		codeListValidator.validateCodeValue(value);
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_value_pattern() throws CodeValidatorException {
		CodeValue value = getCodeValue("#$#%#@#");
		value.setDescriptions(Arrays.asList(getDescription("EN", "Project"), getDescription("BG", "Проект")));
		codeListValidator.validateCodeValue(value);
	}

	@Test
	public void should_not_throw_validation_error_when_validating_code_list_value() throws CodeValidatorException {
		CodeList value = getCodeList("1");
		value.setDescriptions(Arrays.asList(getDescription("EN", "Project"), getDescription("BG", "Проект")));
		codeListValidator.validateCodeList(value);
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_list_value_pattern() throws CodeValidatorException {

		CodeList value = getCodeList("$#$#$@@@");
		value.setDescriptions(Arrays.asList(getDescription("EN", "Project"), getDescription("BG", "Проект")));
		codeListValidator.validateCodeList(value);
	}

	@Test
	public void should_not_throw_validation_error_when_validating_code_values() throws CodeValidatorException {
		CodeValue first = getCodeValue("PR0001");
		CodeValue second = getCodeValue("PR0002");

		first.setDescriptions(Arrays.asList(getDescription("EN", "Project")));
		second.setDescriptions(Arrays.asList(getDescription("BG", "Проект")));

		codeListValidator.validateCodeValues(Arrays.asList(first, second));
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_values_value_uniqueness() throws CodeValidatorException {

		CodeValue first = getCodeValue("APPROVED");
		CodeValue second = getCodeValue("approved");

		codeListValidator.validateCodeValues(Arrays.asList(first, second));
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_value_name_uniqueness() throws CodeValidatorException {

		CodeValue first = getCodeValue("PR0001");
		CodeValue second = getCodeValue("PR0002");

		first.setDescriptions(Arrays.asList(getDescription("EN", "Project")));
		second.setDescriptions(Arrays.asList(getDescription("EN", "Project")));

		codeListValidator.validateCodeValues(Arrays.asList(first, second));
	}

	@Test
	public void should_not_throw_validation_error_when_validating_code_list() throws CodeValidatorException {
		CodeList first = getCodeList("1");
		CodeList second = getCodeList("2");

		first.setDescriptions(Arrays.asList(getDescription("EN", "Project")));
		second.setDescriptions(Arrays.asList(getDescription("BG", "Проект")));

		codeListValidator.validateCodeLists(Arrays.asList(first, second));
	}

	@Test(expected = CodeValidatorException.class)
	public void should_throw_validation_error_when_validating_code_list_value_uniqueness() throws CodeValidatorException {
		CodeList first = getCodeList("CaSe InSenSitivE");
		CodeList second = getCodeList("case insensitive");

		codeListValidator.validateCodeLists(Arrays.asList(first, second));
	}

	private static CodeList getCodeList(String value) {
		CodeList codeList = new CodeList();
		codeList.setValue(value);
		codeList.setExtra1("ListExtra 1");
		codeList.setExtra2("ListExtra 2");
		codeList.setExtra3("ListExtra 3");
		codeList.setValues(new ArrayList<>());
		return codeList;
	}

	private static CodeValue getCodeValue(String value) {
		CodeValue codeValue = new CodeValue();
		codeValue.setActive(true);
		codeValue.setValue(value);
		codeValue.setExtra1("ValueExtra 1");
		codeValue.setExtra2("ValueExtra 2");
		codeValue.setExtra3("ValueExtra 3");
		return codeValue;
	}

	private static CodeDescription getDescription(String language, String name) {
		CodeDescription descr = new CodeDescription();
		descr.setLanguage(language);
		descr.setName(name);
		return descr;
	}
}
