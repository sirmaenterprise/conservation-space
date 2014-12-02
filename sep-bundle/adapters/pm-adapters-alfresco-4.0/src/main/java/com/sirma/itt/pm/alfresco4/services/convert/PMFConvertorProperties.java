package com.sirma.itt.pm.alfresco4.services.convert;

import java.util.Properties;

import javax.inject.Inject;

import com.sirma.itt.cmf.alfresco4.services.convert.*;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.ResourceLoadUtil;
import com.sirma.itt.pm.constants.PMConfigProperties;

/**
 * Loads the converter properties for PM. Extension of base loader.
 *
 * @author bbanchev
 */
@Extension(target = ConverterProperties.TARGET_NAME, order = 10)
public class PMFConvertorProperties implements ConverterProperties {

	/** The convertor properties location. */
	@Inject
	@Config(name = PMConfigProperties.CONFIG_PM_CONVERTER_LOCATION)
	private String convertorPropertiesLocation;

	@Override
	public Properties getInternalProperties() throws Exception {
		Properties internal = ResourceLoadUtil.loadProperties("pmconvertor.properties",
				PMFConvertorProperties.class);
		return internal;
	}

	@Override
	public Properties getExternalProperties() throws Exception {
		if (StringUtils.isNotNullOrEmpty(convertorPropertiesLocation)) {
			// null as fallback
			Properties external = ResourceLoadUtil.loadProperties(convertorPropertiesLocation, null);
			if (external == null) {
				throw new EmfConfigurationException("External file " + convertorPropertiesLocation
						+ " is not found!");
			}
			return external;
		}
		return new Properties();
	}
}