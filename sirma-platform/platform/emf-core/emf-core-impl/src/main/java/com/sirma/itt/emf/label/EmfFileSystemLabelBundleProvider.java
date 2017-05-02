/**
 * Copyright (c) 2013 29.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.label;

import javax.inject.Inject;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.label.FileSystemLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * @author Adrian Mitev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 100)
public class EmfFileSystemLabelBundleProvider extends FileSystemLabelBundleProvider implements MutationObservable {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.label.externalPath", sensitive = true, label = "Used to configure external source of labels - properties file on the file system")
	private ConfigurationProperty<String> labelExternalPath;

	@Override
	public String getPath() {
		return labelExternalPath.get();
	}

	@Override
	public void addMutationObserver(Executable executable) {
		labelExternalPath.addConfigurationChangeListener(c -> executable.execute());
	}

}
