package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.Properties;

import javax.inject.Inject;

import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.ResourceLoadUtil;

/**
 * Loads the converter properties for CMF. This is the base extension.
 *
 * @author bbanchev
 */
@Extension(target = ConverterProperties.TARGET_NAME, order = 0)
public class CMFConverterProperties implements ConverterProperties {

	/** The convertor properties location. */
	@Inject
	@Config(name = CmfConfigurationProperties.CONFIG_CONVERTER_LOCATION)
	private String convertorPropertiesLocation;

	@Override
	public Properties getInternalProperties() throws Exception {
		Properties internal = ResourceLoadUtil.loadProperties("convertor.properties",
				CMFConverterProperties.class);
		return internal;
	}

	@Override
	public Properties getExternalProperties() throws Exception {
		if (StringUtils.isNotNullOrEmpty(convertorPropertiesLocation)) {
			Properties external = ResourceLoadUtil
					.loadProperties(convertorPropertiesLocation, null);
			if (external == null) {
				throw new EmfConfigurationException("External file " + convertorPropertiesLocation
						+ " is not found!");
			}
			return external;
		}
		return new Properties();
	}

}