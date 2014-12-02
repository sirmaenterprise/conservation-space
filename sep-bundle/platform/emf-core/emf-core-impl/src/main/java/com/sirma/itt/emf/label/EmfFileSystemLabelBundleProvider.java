/**
 * Copyright (c) 2013 29.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.label;

import javax.inject.Inject;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.plugin.Extension;

/**
 * @author Adrian Mitev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 100)
public class EmfFileSystemLabelBundleProvider extends FileSystemLabelBundleProvider {

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LABEL_EXTERNAL_PATH, defaultValue = "")
	private String labelExternalPath;

	@Override
	public String getPath() {
		return labelExternalPath;
	}

}
