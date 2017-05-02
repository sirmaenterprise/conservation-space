package com.sirma.itt.emf.cls.accessor;

import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.COMMENT;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA1;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA2;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.EXTRA3;
import static com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants.MASTER_VALUE;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.cls.entity.Code;
import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.Description;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.retriever.CodeValueSearchCriteria;
import com.sirma.itt.emf.cls.retriever.SearchResult;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.cls.util.ClsUtils;
import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.codelist.adapter.CodelistAdapter;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;

/**
 * Implementation of the {@link CodelistAdapter} that uses the services provided by the CLS functionality and
 * respectively, by {@link CodeListService}.
 *
 * @author Vilizar Tsonev
 */
@Singleton
public class InternalCodelistServerAccessor implements CodelistAdapter {
	private static final long serialVersionUID = 431403647335553806L;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "codelist.internalEnabled", defaultValue = "true", sensitive = true, type = Boolean.class, label = "Enable internal code list management")
	private ConfigurationProperty<Boolean> internalEnabled;

	@Inject
	private CodeListService codeListService;

	@Override
	public boolean isConfigured() {
		return internalEnabled.get().booleanValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist, String locale) {
		CodeValueSearchCriteria criteria = new CodeValueSearchCriteria();
		criteria.setCodeListId(codelist.toString());
		criteria.setOffset(0);
		criteria.setLimit(-1);
		criteria.setFromDate(new Date());
		criteria.setToDate(new Date());
		SearchResult codeValues = codeListService.getCodeValues(criteria);
		return convertValues((List<com.sirma.itt.emf.cls.entity.CodeValue>) codeValues.getResults(), locale);
	}

	@Override
	public void addMutationObserver(Executable executable) {
		internalEnabled.addConfigurationChangeListener((c) -> executable.execute());
	}

	@Override
	public void resetCodelist() {
		// Does not need implementation at this point (no caching used).
	}

	@Override
	public Map<BigInteger, String> getAllCodelists(String locale) {
		CodeListSearchCriteria criteria = new CodeListSearchCriteria();
		criteria.setOffset(0);
		criteria.setLimit(-1);
		criteria.setExcludeValues(true);
		SearchResult codeLists = codeListService.getCodeLists(criteria);

		List<? extends Code> results = codeLists.getResults();
		return results.stream().map(CodeList.class::cast).collect(Collectors.toMap(cl -> new BigInteger(cl.getValue()),
				cl -> ClsUtils.getDescriptionByLanguageText(cl.getDescriptions(), locale),
				CollectionUtils.throwingMerger(), () -> CollectionUtils.createLinkedHashMap(results.size())));
	}

	/**
	 * Converts the CLS-specific {@link com.sirma.itt.emf.cls.entity.CodeValue} instance to the SEIP's {@link CodeValue}
	 * .
	 *
	 * @param codeValue
	 *            is the {@link com.sirma.itt.emf.cls.entity.CodeValue} to convert
	 * @return the converted {@link CodeValue}
	 */
	private static CodeValue convertCodeValue(com.sirma.itt.emf.cls.entity.CodeValue codeValue, String locale) {
		CodeValue convertedCodeValue = new CodeValue();
		convertedCodeValue.setCodelist(Integer.valueOf(codeValue.getCodeListId()));
		convertedCodeValue.setValue(codeValue.getValue());
		Map<String, Serializable> descriptions = codeValue
				.getDescriptions()
					.stream()
					.filter(d -> d.getDescription() != null)
					.collect(Collectors.toMap(d -> new Locale(d.getLanguage()).getLanguage(), d -> d.getDescription()));

		convertedCodeValue.addAllProperties(descriptions);

		Description englishDescription = ClsUtils.getDescriptionByLanguage(codeValue.getDescriptions(), locale);
		convertedCodeValue.addIfNotNullOrEmpty(COMMENT, englishDescription.getComment());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA1, codeValue.getExtra1());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA2, codeValue.getExtra2());
		convertedCodeValue.addIfNotNullOrEmpty(EXTRA3, codeValue.getExtra3());
		convertedCodeValue.addIfNotNullOrEmpty(MASTER_VALUE, codeValue.getMasterValue());

		convertedCodeValue.preventModifications();
		return convertedCodeValue;
	}

	/**
	 * Converts the provided {@link SearchResult} to a map containing instances of {@link CodeValue}
	 *
	 * @param searchResult
	 *            is the search result
	 * @return a map containing instances of {@link CodeValue}
	 */
	private static Map<String, CodeValue> convertValues(List<com.sirma.itt.emf.cls.entity.CodeValue> searchResult, String locale) {
		return searchResult.stream().map(codeValue -> convertCodeValue(codeValue, locale)).collect(CollectionUtils
				.toIdentityMap(CodeValue::getValue, () -> CollectionUtils.createLinkedHashMap(searchResult.size())));
	}
}
