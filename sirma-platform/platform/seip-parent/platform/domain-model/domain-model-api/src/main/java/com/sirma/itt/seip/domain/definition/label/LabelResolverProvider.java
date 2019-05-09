package com.sirma.itt.seip.domain.definition.label;

import java.util.Locale;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point for the {@link LabelProvider}. The extension is used to provide statically (resource bundles)
 * or dynamically (databases/caches) label resolver that can be used to load label information from different sources.
 *
 * @author BBonev
 */
public interface LabelResolverProvider extends Plugin {

	/**
	 * The plugin name.
	 */
	String TARGET_NAME = "resolverProvider"; // NOSONAR

	/**
	 * Gets a resolver instance that can fetch label information for the given locale.
	 *
	 * @param locale language for which the resolver should be fetched.
	 * @return the resolver instance
	 */
	LabelResolver getLabelResolver(Locale locale);
}
