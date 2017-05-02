package com.sirma.itt.cmf.extensions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.seip.instance.properties.PersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides a list with persistent properties for cmf.
 *
 * @author svelikov
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 15)
public class CmfPersistentProperties implements PersistentPropertiesExtension {

	private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<>(
			Arrays.asList(TaskProperties.TASK_EXECUTORS, DocumentProperties.STRUCTURED));

	@Override
	public Set<String> getPersistentProperties() {
		return PERSISTENT_PROPERTIES;
	}

}
