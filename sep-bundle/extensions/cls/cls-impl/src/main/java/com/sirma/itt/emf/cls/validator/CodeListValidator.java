package com.sirma.itt.emf.cls.validator;

import java.util.List;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.persister.PersisterException;

/**
 * Class that validates code lists and values.
 * 
 * @author Nikolay Velkov
 */
public class CodeListValidator {

	/**
	 * Validate a field by checking if it's null and if it matches it's pattern.
	 * 
	 * @param content
	 *            the content the field to be validated
	 * @param pattern
	 *            the pattern that the field must confront to
	 * @param fieldName
	 *            the field name
	 * @param codeListNumber
	 *            the code list number
	 * @throws PersisterException
	 *             the persister exception
	 */
	public void validateField(String content, String pattern, String fieldName,
			String codeListNumber) throws PersisterException {
		if (content != null && !content.matches(pattern)) {
			throw new PersisterException("Invalid " + fieldName + " for "
					+ codeListNumber);
		}
	}

	/**
	 * Validate a list of code lists by iterating over it and matching the
	 * pattern of each column with the enum's pattern.
	 * 
	 * @param codeLists
	 *            the code lists
	 * @throws PersisterException
	 *             the persister exception
	 */

	public void validateCodeLists(List<CodeList> codeLists)
			throws PersisterException {
		for (CodeList codeList : codeLists) {
			validateField(codeList.getDescriptions().get(1).getDescription(),
					CLColumn.DESCREN.getPattern(), "EN description",
					codeList.getValue());

			validateField(codeList.getDescriptions().get(0).getDescription(),
					CLColumn.DESCRBG.getPattern(), "BG description",
					codeList.getValue());

			validateField(codeList.getDescriptions().get(0).getComment(),
					CLColumn.COMMENTBG.getPattern(), "BG comment",
					codeList.getValue());

			validateField(codeList.getDescriptions().get(1).getComment(),
					CLColumn.COMMENTEN.getPattern(), "EN comment",
					codeList.getValue());
			validateField(codeList.getExtra1(), CLColumn.EXTRA1.getPattern(),
					"extra 1", codeList.getValue());
			validateField(codeList.getExtra2(), CLColumn.EXTRA2.getPattern(),
					"extra 2", codeList.getValue());
			validateField(codeList.getExtra3(), CLColumn.EXTRA3.getPattern(),
					"extra 3", codeList.getValue());
			validateField(codeList.getExtra4(), CLColumn.EXTRA4.getPattern(),
					"extra 4", codeList.getValue());
			validateField(codeList.getExtra5(), CLColumn.EXTRA5.getPattern(),
					"extra 5", codeList.getValue());
			if (codeList.getMasterValue() != null) {
				testCodeListMasterValue(codeList.getMasterValue(), codeLists);
			}
			if (codeList.getCodeValues() != null) {
				for (CodeValue codeValue : codeList.getCodeValues()) {
					validateField(codeValue.getDescriptions().get(1)
							.getDescription(), CLColumn.DESCREN.getPattern(),
							"EN description", codeValue.getValue());
					validateField(codeValue.getDescriptions().get(0)
							.getDescription(), CLColumn.DESCRBG.getPattern(),
							"BG description", codeValue.getValue());
					validateField(codeValue.getDescriptions().get(0)
							.getComment(), CLColumn.COMMENTBG.getPattern(),
							"BG comment", codeValue.getValue());
					validateField(codeValue.getDescriptions().get(1)
							.getComment(), CLColumn.COMMENTEN.getPattern(),
							"EN comment", codeValue.getValue());
					validateField(codeValue.getValue(),
							CLColumn.VALUE.getPattern(), "value",
							codeValue.getValue());
					validateField(codeValue.getExtra1(),
							CLColumn.EXTRA1.getPattern(), "extra1",
							codeValue.getValue());
					validateField(codeValue.getExtra2(),
							CLColumn.EXTRA2.getPattern(), "extra2",
							codeValue.getValue());
					validateField(codeValue.getExtra3(),
							CLColumn.EXTRA3.getPattern(), "extra3",
							codeValue.getValue());
					validateField(codeValue.getExtra4(),
							CLColumn.EXTRA4.getPattern(), "extra4",
							codeValue.getValue());
					validateField(codeValue.getExtra5(),
							CLColumn.EXTRA5.getPattern(), "extra1",
							codeValue.getValue());
					if (codeValue.getMasterValue() != null) {
						testCodeValueMasterValue(codeValue.getMasterValue(),
								codeList);
					}

				}
			}
		}
	}

	/**
	 * Test code list master value. The value is checked for in the whole list
	 * of code list entities. If there is such a value, nothing happens,
	 * otherwise an exception is thrown.
	 * 
	 * @param masterCodeValue
	 *            the master code list
	 * @param codeLists
	 *            the code lists
	 * @throws PersisterException
	 *             if there is no matching master code list value
	 */
	private void testCodeListMasterValue(String masterCodeValue,
			List<CodeList> codeLists) throws PersisterException {
		for (CodeList codeList : codeLists) {
			if (codeList.getValue().equals(masterCodeValue)) {
				return;
			}
		}
		throw new PersisterException("Invalid code list master value "
				+ masterCodeValue);
	}

	/**
	 * Test code value master value. The value is checked for in the list of
	 * code values for the current code list entity. If there is such a value,
	 * nothing happens, otherwise an exception is thrown.
	 * 
	 * @param masterCodeValueValue
	 *            the master code value
	 * @param codeList
	 *            the code list
	 * @throws PersisterException
	 *             the persister exception
	 */
	private void testCodeValueMasterValue(String masterCodeValueValue,
			CodeList codeList) throws PersisterException {
		for (CodeValue codeValue : codeList.getCodeValues()) {
			if (codeValue.getValue().equals(masterCodeValueValue)) {
				return;
			}
		}
		throw new PersisterException("Invalid code value master value "
				+ masterCodeValueValue + " in code list " + codeList.getValue());
	}
}
