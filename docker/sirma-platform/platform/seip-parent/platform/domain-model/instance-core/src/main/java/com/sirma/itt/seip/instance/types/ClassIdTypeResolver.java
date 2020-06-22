package com.sirma.itt.seip.instance.types;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.types.NoClassInstnaceTypeResolver.PluginConfiguration;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The extensions is used to resolve the type for the ids that match specific pattern. The pattern will match ids of
 * class/libraries.
 *
 * @author A. Kunchev
 */
@Extension(target = PluginConfiguration.NAME, order = PluginConfiguration.CLASS_RESOLVER_ORDER)
public class ClassIdTypeResolver implements NoClassInstnaceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Matches matches identifiers, which does not contain '-'. Example emf:Case, emf:Task, emf:RecordSpace, etc.
	 */
	private static final Pattern IS_INSTANCE_TYPE = Pattern.compile("[\\w]+(?::|/|#)[a-zA-Z0-9]+$");

	@Override
	public boolean canResolve(String id) {
		return IS_INSTANCE_TYPE.matcher(id).find();
	}

	/**
	 * Prevent potential stack overflow, if the type cannot be resolved from the cache.
	 */
	@Override
	public Optional<InstanceType> resolve(String id) {
		LOGGER.warn("Could not resolve instance type for {}", id);
		return Optional.empty();
	}
}