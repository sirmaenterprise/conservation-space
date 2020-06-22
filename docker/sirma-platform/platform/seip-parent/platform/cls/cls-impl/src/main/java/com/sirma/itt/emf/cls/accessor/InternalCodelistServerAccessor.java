package com.sirma.itt.emf.cls.accessor;

import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.COMMENT;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA1;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA2;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA3;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.cls.util.ClsUtils;
import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.codelist.adapter.CodelistAdapter;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;

/**
 * Implementation of the {@link CodelistAdapter} that uses the services provided by the CLS functionality and
 * respectively, by {@link CodeListService}.
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class InternalCodelistServerAccessor implements CodelistAdapter {

	@Inject
	private CodeListService codeListService;

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist, String locale) {
		List<com.sirma.sep.cls.model.CodeValue> codeValues = codeListService.getCodeValues(codelist.toString());
		return convertValues(codeValues, locale);
	}

	@Override
	public void addMutationObserver(Executable executable) {
		// Nothing to do
	}

	@Override
	public void resetCodelist() {
		// Does not need implementation at this point (no caching used).
	}

	@Override
	public Map<BigInteger, String> getAllCodelists(String locale) {
		List<CodeList> codeLists = codeListService.getCodeLists();
		return convertCodeLists(locale, codeLists);
	}

	/**
	 * Converts the CLS-specific {@link com.sirma.sep.cls.model.CodeValue} instance to the SEIP's {@link CodeValue}
	 * .
	 *
	 * @param codeValue
	 *            is the {@link com.sirma.sep.cls.model.CodeValue} to convert
	 * @return the converted {@link CodeValue}
	 */
	private static CodeValue convertCodeValue(com.sirma.sep.cls.model.CodeValue codeValue, String locale) {
		CodeValue convertedCodeValue = new CodeValue();
		convertedCodeValue.setCodelist(Integer.valueOf(codeValue.getCodeListValue()));
		convertedCodeValue.setValue(codeValue.getValue());
		Map<String, Serializable> descriptions = codeValue.getDescriptions().stream()
				.filter(d -> d.getName() != null)
				.collect(Collectors.toMap(d -> new Locale(d.getLanguage()).getLanguage(), CodeDescription::getName));

		convertedCodeValue.addAllProperties(descriptions);

		CodeDescription description = ClsUtils.getDescriptionByLanguage(codeValue.getDescriptions(), locale);
		convertedCodeValue.addIfNotNullOrEmpty(COMMENT, description.getComment());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA1, codeValue.getExtra1());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA2, codeValue.getExtra2());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA3, codeValue.getExtra3());

		convertedCodeValue.preventModifications();
		return convertedCodeValue;
	}

	/**
	 * Converts the provided list of {@link com.sirma.sep.cls.model.CodeValue} to a map containing instances of {@link CodeValue}
	 *
	 * @param codeValues
	 *            list of values for conversion
	 * @return a map containing instances of {@link CodeValue}
	 */
	private static Map<String, CodeValue> convertValues(List<com.sirma.sep.cls.model.CodeValue> codeValues,
			String locale) {
		return codeValues.stream().filter(
				com.sirma.sep.cls.model.CodeValue::isActive).map(codeValue -> convertCodeValue(codeValue, locale)).collect(CollectionUtils
				.toIdentityMap(CodeValue::getValue, () -> CollectionUtils.createLinkedHashMap(codeValues.size())));
	}

	private static Map<BigInteger, String> convertCodeLists(String locale, List<CodeList> results) {
		return results
				.stream()
					.map(CodeList.class::cast)
					.collect(Collectors.toMap(cl -> new BigInteger(cl.getValue()),
						cl -> ClsUtils.getDescriptionByLanguageText(cl.getDescriptions(), locale),
						CollectionUtils.throwingMerger(), () -> CollectionUtils.createLinkedHashMap(results.size())));
	}
}
