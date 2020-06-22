package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.Properties;

import javax.inject.Inject;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Loads the converter properties for CMF. This is the base extension.
 *
 * @author bbanchev
 */
@Extension(target = ConverterProperties.TARGET_NAME, order = 0)
public class CMFConverterProperties implements ConverterProperties, MutationObservable {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "convertor.dms.location", sensitive = true, label = "Path to the DMS converter properties")
	private ConfigurationProperty<String> convertorPropertiesLocation;

	@Override
	public Properties getInternalProperties() {
		return ResourceLoadUtil.loadProperties("convertor.properties", CMFConverterProperties.class);
	}

	@Override
	public Properties getExternalProperties() {
		if (convertorPropertiesLocation.isSet()) {
			Properties external = ResourceLoadUtil.loadProperties(convertorPropertiesLocation.get(), null);
			if (external == null) {
				throw new EmfConfigurationException("External file " + convertorPropertiesLocation + " is not found!");
			}
			return external;
		}
		return new Properties();
	}

	@Override
	public void addMutationObserver(Executable executable) {
		convertorPropertiesLocation.addConfigurationChangeListener(c -> executable.execute());
	}

}