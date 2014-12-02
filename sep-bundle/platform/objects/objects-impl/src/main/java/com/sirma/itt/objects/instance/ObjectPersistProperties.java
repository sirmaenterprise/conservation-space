package com.sirma.itt.objects.instance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PersistentPropertiesExtension;
import com.sirma.itt.objects.constants.ObjectProperties;

/**
 * Adds objects specific properties that need to be always persisted.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 50)
public class ObjectPersistProperties implements PersistentPropertiesExtension {

	/** The Constant PROPERTIES. */
	private static final Set<String> PROPERTIES = new HashSet<String>(Arrays.asList(ObjectProperties.DEFAULT_VIEW_LOCATION));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getPersistentProperties() {
		return PROPERTIES;
	}

}
