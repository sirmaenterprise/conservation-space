package com.sirmaenterprise.sep.eai.spreadsheet.model.transform;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants.LOCALE_BG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.inject.Singleton;

import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.eai.service.model.transform.impl.DefaultModelConverter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;

/**
 * Extension to register {@link SpreadsheetIntegrationConfigurationProvider#SYSTEM_ID} model converter and to
 * extend/override needed code
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIModelConverter.PLUGIN_ID, order = 5)
public class SpreadsheetModelConverter extends DefaultModelConverter {

	/**
	 * Construct a {@link SpreadsheetModelConverter} for {@link SpreadsheetIntegrationConfigurationProvider#SYSTEM_ID}
	 * system
	 */
	public SpreadsheetModelConverter() {
		super(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID);
	}

	@Override
	public Map<String, Serializable> convertExternaltoSEIPProperties(Map<String, Object> properties, Instance consumer)
			throws EAIModelException {
		return convertExternaltoSEIPProperties(properties, dictionaryService.getInstanceDefinition(consumer),
				new ErrorBuilderProvider());
	}

	@Override
	protected Serializable convertInternalToExternalValueByCodelist(Integer codelist, Serializable source) {
		if (source == null) {
			return null;
		}
		if (source instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Serializable> collection = (Collection<Serializable>) source;
			ArrayList<Serializable> converted = new ArrayList<>(collection.size());
			for (Serializable element : collection) {
				Serializable valueByCodeValue = convertInternalToExternalValueByCodelist(codelist,
						Objects.toString(element, null));
				if (valueByCodeValue == null) {
					// the whole batch fails when single entry is not resolved
					return null;
				}
				converted.add(valueByCodeValue);
			}
			return converted;
		}
		return convertInternalToExternalValueByCodelist(codelist, Objects.toString(source, null));
	}

	private Serializable convertInternalToExternalValueByCodelist(Integer codelist, String stringValue) {
		if (stringValue == null) {
			return null;
		}
		String trimmedValue = stringValue.trim();
		CodeValue codeValue = codelistService.getCodeValue(codelist, trimmedValue);
		if (codeValue != null) {
			return codelistService.getDescription(codeValue);
		}
		return null;
	}

	@Override
	protected Serializable convertExternalToInternalValueByCodelist(Integer codelist, Serializable source) {
		if (source == null) {
			return null;
		}
		Map<String, CodeValue> codeValues = codelistService.getCodeValues(codelist);
		if (source instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Serializable> collection = (Collection<Serializable>) source;
			ArrayList<Serializable> converted = new ArrayList<>(collection.size());
			for (Serializable element : collection) {
				Serializable valueByCodelist = convertExternalToInternalValueByCodelist(codeValues,
						Objects.toString(element, null));
				if (valueByCodelist == null) {
					// the whole batch fails when single entry is not resolved
					return null;
				}
				converted.add(valueByCodelist);
			}
			return converted;
		}
		return convertExternalToInternalValueByCodelist(codeValues, Objects.toString(source, null));
	}

	private static Serializable convertExternalToInternalValueByCodelist(Map<String, CodeValue> codeValues,
			String stringValue) {
		if (stringValue == null) {
			return null;
		}
		String trimmedValue = stringValue.trim();
		for (CodeValue codeValue : codeValues.values()) {
			// search in both locales
			if (EqualsHelper.nullSafeEquals(codeValue.getDescription(Locale.ENGLISH), trimmedValue, true)
					|| EqualsHelper.nullSafeEquals(codeValue.getDescription(LOCALE_BG), trimmedValue, true)) {
				return codeValue.getValue();
			}
		}
		return null;
	}

}