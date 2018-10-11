package com.sirma.itt.emf.label.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Retrieves code list values. FieldValueRetrieverParameters.CODE_LIST_ID should be passed as additional parameter.
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 8)
public class CodeListFieldValueRetriever extends PairFieldValueRetriever {
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.CODE_LIST);
	}

	@Inject
	private CodelistService codelistService;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null && additionalParameters != null) {
			Integer codeListId = additionalParameters.getFirstInteger(FieldValueRetrieverParameters.CODE_LIST_ID);
			if (codeListId == null) {
				return value;
			}

			Map<String, CodeValue> codeValues = codelistService.getCodeValues(codeListId);
			for (CodeValue codeValue : codeValues.values()) {
				if (value.equalsIgnoreCase(codeValue.getValue())) {
					return codeValue.getProperties().get(getCurrentUserLanguage()).toString();
				}
			}
		}
		return value;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		int localOffset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();
		Set<String> codeValueIds = new HashSet<>();

		if (additionalParameters != null
				&& additionalParameters.get(FieldValueRetrieverParameters.CODE_LIST_ID) != null) {
			List<Integer> codeLists = additionalParameters.getIntegers(FieldValueRetrieverParameters.CODE_LIST_ID);
			for (Integer codeListId : codeLists) {
				Map<String, CodeValue> codeValues = codelistService.getCodeValues(codeListId);
				for (CodeValue codeValue : codeValues.values()) {
					Map<String, Serializable> properties = codeValue.getProperties();
					if (properties != null) {
						Serializable label = properties.get(getCurrentUserLanguage());
						if (label != null) {
							String codeValueValue = label.toString();
							if (StringUtils.isBlank(codeValueValue)) {
								codeValueValue = codeValue.getValue();
							}
							if (codeValueIds.add(codeValue.getValue()) && (StringUtils.isBlank(filter)
									|| codeValueValue.toLowerCase().startsWith(filter.toLowerCase()))) {
								validateAndAddPair(results, codeValue.getValue(), codeValueValue, filter, localOffset,
										limit, total);
								total++;
							}
						}
					}
				}
			}
		}
		return new RetrieveResponse(total, results);
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}

}
