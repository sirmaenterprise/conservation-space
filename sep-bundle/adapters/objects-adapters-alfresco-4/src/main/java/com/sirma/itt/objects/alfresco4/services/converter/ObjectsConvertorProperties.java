package com.sirma.itt.objects.alfresco4.services.converter;

import java.util.Properties;

import javax.inject.Inject;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.ResourceLoadUtil;
import com.sirma.itt.objects.constants.ObjectsConfigProperties;

/**
 * Loads the converter properties for Objects module.
 *
 * @author bbanchev
 */
@Extension(target = ConverterProperties.TARGET_NAME, order = 20)
public class ObjectsConvertorProperties implements ConverterProperties {

	/** The convertor properties location. */
	@Inject
	@Config(name = ObjectsConfigProperties.CONFIG_OBJECTS_CONVERTER_LOCATION)
	private String convertorPropertiesLocation;

	@Override
	public Properties getInternalProperties() throws Exception {
		Properties internal = ResourceLoadUtil.loadProperties("objects_convertor.properties",
				ObjectsConvertorProperties.class);
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