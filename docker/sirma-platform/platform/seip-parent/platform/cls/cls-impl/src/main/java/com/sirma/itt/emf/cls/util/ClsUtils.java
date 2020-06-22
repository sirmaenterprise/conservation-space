package com.sirma.itt.emf.cls.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;

/**
 * Provides common utility methods and constants for working with code lists in CLS.
 *
 * @author Vilizar Tsonev
 * @author svetlozar.iliev
 */
public final class ClsUtils {

	/**
	 * Not instantiated utility class.
	 */
	private ClsUtils() {
		// nothing to do
	}

	/**
	 * Gets the description by the given language.
	 *
	 * @param descriptions
	 *            is the {@link List} of descriptions
	 * @param language
	 *            is the language code
	 * @return the {@link CodeDescription} in the given language
	 */
	public static CodeDescription getDescriptionByLanguage(List<CodeDescription> descriptions, String language) {
		return descriptions.stream().filter(description -> description.getLanguage().equalsIgnoreCase(language)).findFirst().orElse(null);
	}

	/**
	 * Gets the description by language text.
	 *
	 * @param descriptions
	 *            the descriptions
	 * @param language
	 *            the language
	 * @return the description by language text
	 */
	public static String getDescriptionByLanguageText(List<CodeDescription> descriptions, String language) {
		CodeDescription description = getDescriptionByLanguage(descriptions, language);
		if (description != null) {
			return description.getName();
		}
		return null;
	}

	/**
	 * Normalizes the code lists by trimming the unnecessary white spaces at the start and the end of the descriptions
	 * and extras.
	 * 
	 * @param codeLists
	 *            a collection of code lists to be formatted
	 */
	public static void formatCodeAttributesAndDescriptions(List<CodeList> codeLists) {
		codeLists.forEach(codeList -> {
			formatCodeAttributesAndDescriptions(codeList);
			codeList.getValues().forEach(ClsUtils::formatCodeAttributesAndDescriptions);
		});
	}

	/**
	 * Formats both code attributes, fields and their descriptions for each language. For detailed description of the
	 * implementation refer to {@link ClsUtils#formatCodeAttributes(Code)} and {@link ClsUtils#formatDescriptions}
	 * 
	 * @param code
	 *            the code fields and descriptions to be formatted
	 */
	public static void formatCodeAttributesAndDescriptions(Code code) {
		formatCodeAttributes(code);
		formatDescriptions(code);
	}

	/**
	 * Trims all white spaces from the beginning/end of the code list/value's attributes and if any of them is an empty
	 * string, makes it null to avoid the single quotes in the DB.
	 *
	 * @param code
	 *            is the {@link Code} to format
	 */
	private static void formatCodeAttributes(Code code) {
		code.setValue(StringUtils.stripToNull(code.getValue()));
		code.setExtra1(StringUtils.stripToNull(code.getExtra1()));
		code.setExtra2(StringUtils.stripToNull(code.getExtra2()));
		code.setExtra3(StringUtils.stripToNull(code.getExtra3()));
	}

	/**
	 * Trims the descriptions of the provided code. If a description or comment is empty, it will be made null.
	 *
	 * @param code
	 *            - the provided code list or value to format
	 */
	private static void formatDescriptions(Code code) {
		code.getDescriptions().forEach(ClsUtils::formatDescription);
	}

	private static void formatDescription(CodeDescription description) {
		description.setLanguage(StringUtils.stripToNull(description.getLanguage()));
		description.setName(StringUtils.stripToNull(description.getName()));
		description.setComment(StringUtils.stripToNull(description.getComment()));
	}
}
