package com.sirma.itt.emf.security.evaluator;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HAS_FAVOURITE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_TOOLTIP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY_MESSAGE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_INFO;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MARKED_FOR_DOWNLOAD;
import static com.sirma.itt.seip.instance.version.VersionProperties.DYNAMIC_QUERIES;
import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;

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
public class EmfSemanticNonPersistentPropertiesProvider implements SemanticNonPersistentPropertiesExtension {

	private static final Set<String> PROPERTIES = new HashSet<>(
			Arrays.asList(MARKED_FOR_DOWNLOAD, HAS_FAVOURITE, HEADER_BREADCRUMB, HEADER_COMPACT, HEADER_DEFAULT,
					HEADER_TOOLTIP, LOCKED_INFO, LOCKED_BY, LOCKED_BY_MESSAGE, DYNAMIC_QUERIES, VERSION_CREATED_ON));

	@Override
	public Set<String> getNonPersistentProperties() {
		return PROPERTIES;
	}

}
