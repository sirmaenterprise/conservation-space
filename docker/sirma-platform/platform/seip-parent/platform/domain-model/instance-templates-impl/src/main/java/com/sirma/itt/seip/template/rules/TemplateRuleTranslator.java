package com.sirma.itt.seip.template.rules;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Translates template rules to a human-readable form, swapping the codes with their labels from the definition and
 * codelists.
 *
 * @author Vilizar Tsonev
 */
public class TemplateRuleTranslator {

	@Inject
	private DefinitionService definitionService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private SystemConfiguration systemConfiguration;

	/**
	 * Translates the rule to a human-readable form, swapping the codes with their labels from the definition and
	 * codelists.
	 *
	 * @param rule is the template rule
	 * @param forType is the forType of the template (definition ID)
	 * @return the translated rule
	 */
	public String translate(String rule, String forType) {
		if (StringUtils.isBlank(rule) || StringUtils.isBlank(forType)) {
			return rule;
		}

		DefinitionModel definition = definitionService.find(forType);

		Map<String, Serializable> parsed = TemplateRuleUtils.parseRule(rule);

		StringBuilder translated = new StringBuilder();
		for (Map.Entry<String, Serializable> entry : parsed.entrySet()) {
			if (translated.length() > 0) {
				translated.append(" AND ");
			}

			String criteriaString = ruleCriteriaToString(entry.getKey(), entry.getValue(), definition);
			translated.append(criteriaString);
		}

		return translated.toString();
	}

	private String ruleCriteriaToString(String field, Serializable value, DefinitionModel definition) {
		Optional<PropertyDefinition> propertyDefinition = definition.getField(field);
		if (!propertyDefinition.isPresent()) {
			throw new EmfApplicationException(
					"Template rule field [" + field + "] was not found in definition " + definition.getIdentifier());
		}
		PropertyDefinition definitionField = propertyDefinition.get();
		String keyTranslated = definitionField.getLabel();
		String valueTranslated;

		if (isCodeListField(definitionField)) {
			valueTranslated = retrieveFromCodeList(definitionField.getCodelist(), value);
		} else {
			valueTranslated = String.valueOf(value);
		}

		return keyTranslated + ": " + valueTranslated;
	}

	private String retrieveFromCodeList(Integer codeList, Serializable value) {
		if (value instanceof List<?>) {
			List<String> values = (List<String>) value;
			if (values.size() == 1) {
				return codelistService.getCodeValue(codeList, values.get(0))
						.getDescription(new Locale(systemConfiguration.getSystemLanguage()));
			}
			StringBuilder result = new StringBuilder("(");
			values.stream().forEach(val -> {
				if (result.length() > 1) {
					result.append(", ");
				}
				String codeValueLabel = codelistService.getCodeValue(codeList, val)
						.getDescription(new Locale(systemConfiguration.getSystemLanguage()));
				result.append(codeValueLabel);
			});
			result.append(")");
			return result.toString();
		}
		return codelistService.getCodeValue(codeList, (String) value)
				.getDescription(new Locale(systemConfiguration.getSystemLanguage()));
	}

	private static boolean isCodeListField(PropertyDefinition definitionField) {
		return definitionField.getCodelist() != null;
	}
}
