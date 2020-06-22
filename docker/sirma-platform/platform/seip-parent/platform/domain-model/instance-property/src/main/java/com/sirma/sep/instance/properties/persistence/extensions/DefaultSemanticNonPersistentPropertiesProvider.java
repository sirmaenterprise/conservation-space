package com.sirma.sep.instance.properties.persistence.extensions;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.ENTITY_IDENTIFIER;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HAS_FAVOURITE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_TOOLTIP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY_MESSAGE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_INFO;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MARKED_FOR_DOWNLOAD;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_MODE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for the property keys that should be removed from an instance when persisting it.
 *
 * @author BBonev
 */
@Extension(target = SemanticNonPersistentPropertiesExtension.TARGET_NAME, order = 10)
public class DefaultSemanticNonPersistentPropertiesProvider implements SemanticNonPersistentPropertiesExtension {

	private static final Set<String> PROPERTIES = new HashSet<>(Arrays.asList(MARKED_FOR_DOWNLOAD, HAS_FAVOURITE,
			HEADER_BREADCRUMB, HEADER_COMPACT, HEADER_DEFAULT, HEADER_TOOLTIP, LOCKED_INFO, LOCKED_BY,
			LOCKED_BY_MESSAGE, VERSION_CREATED_ON, VERSION_MODE, ENTITY_IDENTIFIER, THUMBNAIL_IMAGE));

	@Override
	public Set<String> getNonPersistentProperties() {
		return PROPERTIES;
	}
}