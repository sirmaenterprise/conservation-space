package com.sirma.itt.seip.instance.types;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.types.NoClassInstnaceTypeResolver.PluginConfiguration;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The extension is used as last resolver in the chain. It will be executed, if there are no others implementation that
 * can resolve the type of the passed value. <br>
 * Note that this resolver will load instance for the given id.
 *
 * @author A. Kunchev
 */
@Extension(target = PluginConfiguration.NAME, order = PluginConfiguration.OBJECT_INSTANCE_RESOLVER_ORDER)
public class ObjectInstanceTypeResolver implements NoClassInstnaceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean canResolve(String id) {
		return true;
	}

	/**
	 * If any of the previous resolves can't resolve the type, try by loading the instance.
	 */
	@Override
	public Optional<InstanceType> resolve(String id) {
		LOGGER.trace("Resolving instance type via instance loading for id: {}", id);
		return instanceTypeResolver.resolve(id);
	}
}