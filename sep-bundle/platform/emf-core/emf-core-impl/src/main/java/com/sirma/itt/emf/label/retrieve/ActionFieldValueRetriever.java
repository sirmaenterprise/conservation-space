package com.sirma.itt.emf.label.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Converts action id to action label using action codelist.
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 0)
public class ActionFieldValueRetriever extends PairFieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;

	static {
		SUPPORTED_FIELDS = new ArrayList<String>(1);
		SUPPORTED_FIELDS.add(FieldId.ACTIONID);
	}
	private static final int ACTION_CODELIST = 9;

	@Inject
	private CodelistService codelistService;

	@Override
	public String getLabel(String... value) {
		if (value != null && value.length > 0) {
			CodeValue codeValue = codelistService.getCodeValue(ACTION_CODELIST, value[0]);
			if (codeValue != null && codeValue.getProperties() != null) {
				Serializable label = codeValue.getProperties().get(getCurrentUserLanguage());
				if (label != null) {
					return label.toString();
				}
			}
		}
		return null;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		offset = offset != null ? offset : 0;
		long total = 0;
		List<Pair<String, String>> results = new ArrayList<>();

		Map<String, CodeValue> codeValues = codelistService.getCodeValues(ACTION_CODELIST, true);
		for (CodeValue codeValue : codeValues.values()) {

			Map<String, Serializable> properties = codeValue.getProperties();

			if (properties != null) {
				Serializable label = properties.get(getCurrentUserLanguage());
				if (label != null) {
					String codeValueValue = label.toString();
					if (StringUtils.isNullOrEmpty(codeValueValue)) {
						codeValueValue = codeValue.getValue();
					}

					if (StringUtils.isNullOrEmpty(filter)
							|| codeValueValue.toLowerCase().startsWith(filter.toLowerCase())) {
						validateAndAddPair(results, codeValue.getValue(), codeValueValue, filter,
								offset, limit, total);
						total++;
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
