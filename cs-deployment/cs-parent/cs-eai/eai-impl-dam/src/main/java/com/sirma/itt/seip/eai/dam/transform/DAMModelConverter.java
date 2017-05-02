package com.sirma.itt.seip.eai.dam.transform;

import java.io.Serializable;

import javax.inject.Singleton;

import com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.eai.dam.configuration.DAMIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.eai.service.model.transform.impl.DefaultModelConverter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension to register DAM model converter and to extend/override needed code
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIModelConverter.PLUGIN_ID, order = 11)
public class DAMModelConverter extends DefaultModelConverter {

	/**
	 * Construct a {@link DefaultModelConverter} for {@link DAMIntegrationConfigurationProvider#SYSTEM_ID} system
	 */
	public DAMModelConverter() {
		super(DAMIntegrationConfigurationProvider.SYSTEM_ID);
	}

	@Override
	protected Serializable convertInternalToExternalValueByCodelist(Integer codelist, Serializable source) {
		if (source == null) {
			return null;
		}
		String stringValue = String.valueOf(source);
		CodeValue codeValue = codelistService.getCodeValue(codelist, stringValue);
		if (codeValue != null) {
			return codeValue.getProperties().get(CodelistPropertiesConstants.EXTRA3);
		}
		return null;
	}

	@Override
	protected Serializable convertExternalToInternalValueByCodelist(Integer codelist, Serializable source) {
		if (source == null) {
			return null;
		}
		String stringValue = String.valueOf(source);
		for (CodeValue codeValue : codelistService.getCodeValues(codelist).values()) {
			Serializable extra = codeValue.getProperties().get(CodelistPropertiesConstants.EXTRA3);
			if (extra != null && (String.valueOf(extra).equalsIgnoreCase(stringValue))) {
				return codeValue.getValue();
			}
		}
		return null;
	}
}