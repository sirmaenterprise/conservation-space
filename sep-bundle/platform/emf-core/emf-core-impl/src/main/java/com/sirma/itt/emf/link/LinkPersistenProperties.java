package com.sirma.itt.emf.link;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PersistentPropertiesExtension;

/**
 * The list of persistent link properties.
 * 
 * @author BBonev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 11)
public class LinkPersistenProperties implements PersistentPropertiesExtension {

	/** The allowed no definition fields. */
	private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<String>(
			Arrays.asList(LinkConstants.LINK_DESCRIPTION));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getPersistentProperties() {
		return PERSISTENT_PROPERTIES;
	}
}
