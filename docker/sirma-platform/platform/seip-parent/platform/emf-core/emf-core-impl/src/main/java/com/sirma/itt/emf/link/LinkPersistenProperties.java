package com.sirma.itt.emf.link;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.instance.properties.PersistentPropertiesExtension;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The list of persistent link properties.
 *
 * @author BBonev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 11)
public class LinkPersistenProperties implements PersistentPropertiesExtension {

	/** The allowed no definition fields. */
	private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<>(
			Arrays.asList(LinkConstants.LINK_DESCRIPTION));

	@Override
	public Set<String> getPersistentProperties() {
		return PERSISTENT_PROPERTIES;
	}
}
