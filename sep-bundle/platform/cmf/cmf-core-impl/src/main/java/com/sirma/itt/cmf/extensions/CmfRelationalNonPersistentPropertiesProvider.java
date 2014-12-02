package com.sirma.itt.cmf.extensions;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.cmf.constants.NonPersistentProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.dao.RelationalNonPersistentPropertiesExtension;

/**
 * Provider for the property keys that should be removed from an instance when persisting it to
 * relational db
 * 
 * @author BBonev
 */
@Extension(target = RelationalNonPersistentPropertiesExtension.TARGET_NAME, order = 25)
public class CmfRelationalNonPersistentPropertiesProvider implements RelationalNonPersistentPropertiesExtension {

	private static final Set<String> PROPERTIES = new HashSet<>();

	static {
		PROPERTIES.add(DefaultProperties.CONTENT);
		PROPERTIES.add(NonPersistentProperties.LOAD_VIEW_VERSION);
		PROPERTIES.add(DefaultProperties.THUMBNAIL_IMAGE);
		PROPERTIES.add(TaskProperties.NEXT_STATE_PROP_MAP);
		PROPERTIES.add(TaskProperties.CURRENT_STATE_PROP_MAP);
	}

	@Override
	public Set<String> getNonPersistentProperties() {
		return CmfRelationalNonPersistentPropertiesProvider.PROPERTIES;
	}

}
