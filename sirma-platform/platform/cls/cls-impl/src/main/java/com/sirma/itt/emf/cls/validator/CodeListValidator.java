package com.sirma.itt.emf.cls.validator;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sirma.itt.emf.cls.columns.CLColumn;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListDescription;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.entity.CodeValueDescription;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.util.ClsUtils;

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
	public void validateField(String content, String pattern, String fieldName, String codeListNumber)
			throws PersisterException {
		if (content != null && !content.matches(pattern)) {
			throw new PersisterException("Invalid " + fieldName + " for " + codeListNumber);
		}
	}

	/**
	 * Validate a list of code lists by iterating over it and matching the pattern of each column with the enum's
	 * pattern.
	 *
	 * @param codeLists
	 *            the code lists
	 * @throws PersisterException
	 *             the persister exception
	 */

	public void validateCodeLists(List<CodeList> codeLists) throws PersisterException {
		for (CodeList codeList : codeLists) {
			validateField(codeList.getValue(), CLColumn.CL_VALUE.getPattern(), "Code", codeList.getValue());
			// BG description is not mandatory. So if present, validate it
			boolean hasBgCodeListDescription = codeList.getDescriptions().size() > 1;
			if (hasBgCodeListDescription) {
				CodeListDescription bulgarianCLDescription = (CodeListDescription) ClsUtils
						.getDescriptionByLanguage(codeList.getDescriptions(), ClsUtils.LANGUAGE_BG);
				validateField(bulgarianCLDescription.getDescription(), CLColumn.DESCRBG.getPattern(), "BG description",
						codeList.getValue());

				validateField(bulgarianCLDescription.getComment(), CLColumn.COMMENTBG.getPattern(), "BG comment",
						codeList.getValue());
			}
			CodeListDescription englishCLDescription = (CodeListDescription) ClsUtils
					.getDescriptionByLanguage(codeList.getDescriptions(), ClsUtils.LANGUAGE_EN);
			validateField(englishCLDescription.getDescription(), CLColumn.DESCREN.getPattern(), "EN description",
					codeList.getValue());
			validateField(englishCLDescription.getComment(), CLColumn.COMMENTEN.getPattern(), "EN comment",
					codeList.getValue());
			validateField(codeList.getExtra1(), CLColumn.EXTRA1.getPattern(), "extra 1", codeList.getValue());
			validateField(codeList.getExtra2(), CLColumn.EXTRA2.getPattern(), "extra 2", codeList.getValue());
			validateField(codeList.getExtra3(), CLColumn.EXTRA3.getPattern(), "extra 3", codeList.getValue());
			validateField(codeList.getExtra4(), CLColumn.EXTRA4.getPattern(), "extra 4", codeList.getValue());
			validateField(codeList.getExtra5(), CLColumn.EXTRA5.getPattern(), "extra 5", codeList.getValue());
			validateDisplayType(codeList);
			if (codeList.getMasterValue() != null) {
				testCodeListMasterValue(codeList.getMasterValue(), codeLists);
			}
			validateCodeValues(codeList.getCodeValues(), codeLists, codeList);
		}
	}

	/**
	 * Validates that display type is provided and it matches its pattern in {@link CLColumn}.
	 *
	 * @param codeList
	 *            is the code list
	 * @throws PersisterException
	 *             if the display type validation fails
	 */
	private void validateDisplayType(CodeList codeList) throws PersisterException {
		if(codeList.getDisplayType() == null) {
			throw new PersisterException("Missing display type for codelist " + codeList.getValue());
		}
		validateField(codeList.getDisplayType().toString(), CLColumn.DISPLAY_TYPE.getPattern(), "display type",
				codeList.getValue());
	}

	/**
	 * Validates the list of {@link CodeValue}.
	 *
	 * @param codeValues
	 *            is the list of {@link CodeValue} to be validated
	 * @param codeLists
	 *            is the list of all {@link CodeList}. If needed, the master CL entity will be extracted from them
	 * @param codeList
	 *            is the associated {@link CodeList} for the passed code values
	 * @throws PersisterException
	 *             if one of the validations fail
	 */
	private void validateCodeValues(List<CodeValue> codeValues, List<CodeList> codeLists, CodeList codeList)
			throws PersisterException {
		if (codeValues == null) {
			return;
		}
		for (CodeValue codeValue : codeValues) {
			// Get all codeValues with the same code.
			List<CodeValue> duplicatedValues = codeValues
					.stream()
						.filter(value -> value.getValue().equals(codeValue.getValue()))
						.collect(Collectors.toList());
			if (duplicatedValues.size() > 1 && codeValuesOverlap(duplicatedValues)) {
				throw new PersisterException("Duplicated code value " + codeValue.getValue()
						+ " with overlapping date range in codelist " + codeList.getValue());
			}
			validateField(codeValue.getValue(), CLColumn.CV_VALUE.getPattern(), "value", codeValue.getValue());
			// BG description is not mandatory. So if present, validate it
			boolean hasBgCodeValueDescription = codeValue.getDescriptions().size() > 1;
			if (hasBgCodeValueDescription) {
				CodeValueDescription bulgarianCVDescription = (CodeValueDescription) ClsUtils
						.getDescriptionByLanguage(codeValue.getDescriptions(), ClsUtils.LANGUAGE_BG);
				validateField(bulgarianCVDescription.getDescription(), CLColumn.DESCRBG.getPattern(), "BG description",
						codeValue.getValue());
				validateField(bulgarianCVDescription.getComment(), CLColumn.COMMENTBG.getPattern(), "BG comment",
						codeValue.getValue());
			}
			CodeValueDescription englishCVDescription = (CodeValueDescription) ClsUtils
					.getDescriptionByLanguage(codeValue.getDescriptions(), ClsUtils.LANGUAGE_EN);
			validateField(englishCVDescription.getDescription(), CLColumn.DESCREN.getPattern(), "EN description",
					codeValue.getValue());
			validateField(englishCVDescription.getComment(), CLColumn.COMMENTEN.getPattern(), "EN comment",
					codeValue.getValue());
			validateField(codeValue.getExtra1(), CLColumn.EXTRA1.getPattern(), "extra1", codeValue.getValue());
			validateField(codeValue.getExtra2(), CLColumn.EXTRA2.getPattern(), "extra2", codeValue.getValue());
			validateField(codeValue.getExtra3(), CLColumn.EXTRA3.getPattern(), "extra3", codeValue.getValue());
			validateField(codeValue.getExtra4(), CLColumn.EXTRA4.getPattern(), "extra4", codeValue.getValue());
			validateField(codeValue.getExtra5(), CLColumn.EXTRA5.getPattern(), "extra1", codeValue.getValue());
			if (codeValue.getMasterValue() != null) {
				CodeList masterCodeList = codeLists
						.stream()
							.filter(currentCodeList -> currentCodeList.getValue().equals(codeList.getMasterValue()))
							.findFirst()
							.orElseThrow(() -> new PersisterException(
									"Master code list " + codeList.getMasterValue() + " does not exist"));
				testCodeValueMasterValue(masterCodeList, codeValue.getMasterValue());
			}
		}
	}

	/**
	 * Check if the code values' ranges overlap.
	 *
	 * @param codeValues
	 *            the codeValues to check
	 * @return true if any of their ranges overlap, false otherwise.
	 */
	public static boolean codeValuesOverlap(List<CodeValue> codeValues) {
		for (int i = 0; i < codeValues.size() - 1; i++) {
			for (int j = i + 1; j < codeValues.size(); j++) {
				CodeValue firstValue = codeValues.get(i);
				CodeValue secondValue = codeValues.get(j);

				if (firstValue.getValidFrom() == null && secondValue.getValidFrom() == null
						|| dateRangesOverlap(firstValue, secondValue)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Set the date to the max future date if it's not set to avoid most of the corner cases.
	 *
	 * @param date
	 *            the date
	 * @return the original date if set, otherwise the max future date
	 */
	private static Date getEndDate(Date date) {
		if (date == null) {
			return new Date(Long.MAX_VALUE);
		}
		return date;
	}

	/**
	 * Check if the date ranges overlap. This takes into consideration negative infinity and infinity.
	 *
	 * @param firstValue
	 *            the first codeValue
	 * @param secondValue
	 *            the second codeValue
	 * @return true if they overlap
	 */
	private static boolean dateRangesOverlap(CodeValue firstValue, CodeValue secondValue) {
		Date firstStartDate = firstValue.getValidFrom();
		Date secondStartDate = secondValue.getValidFrom();

		Date firstEndDate = getEndDate(firstValue.getValidTo());
		Date secondEndDate = getEndDate(secondValue.getValidTo());

		if (firstStartDate == null && secondStartDate.before(firstEndDate)) {
			return true;
		} else if (secondStartDate == null && secondEndDate.after(firstStartDate)) {
			return true;
		} else if (firstStartDate != null && secondStartDate != null && firstStartDate.before(secondEndDate)
				&& firstEndDate.after(secondStartDate)) {
			return true;
		}
		return false;
	}

	/**
	 * Test code list master value. The value is checked for in the whole list of code list entities. If there is such a
	 * value, nothing happens, otherwise an exception is thrown.
	 *
	 * @param masterCodeValue
	 *            the master code list
	 * @param codeLists
	 *            the code lists
	 * @throws PersisterException
	 *             if there is no matching master code list value
	 */
	private void testCodeListMasterValue(String masterCodeValue, List<CodeList> codeLists) throws PersisterException {
		for (CodeList codeList : codeLists) {
			if (codeList.getValue().equals(masterCodeValue)) {
				return;
			}
		}
		throw new PersisterException("Invalid code list master value " + masterCodeValue);
	}

	/**
	 * Test code value master value. The value is checked for in the list of code values for the master code list
	 * entity. If there is such a value, nothing happens, otherwise an exception is thrown.
	 *
	 * @param masterCodeList
	 *            is the entity representing the master code list
	 * @param masterCodeValueValue
	 *            is the value (ID) of the master code value
	 * @throws PersisterException
	 *             if the validation fails
	 */
	private void testCodeValueMasterValue(CodeList masterCodeList, String masterCodeValueValue)
			throws PersisterException {
		for (CodeValue codeValue : masterCodeList.getCodeValues()) {
			if (codeValue.getValue().equals(masterCodeValueValue)) {
				return;
			}
		}
		throw new PersisterException("Invalid code value master value " + masterCodeValueValue + " in code list "
				+ masterCodeList.getValue());
	}
}
