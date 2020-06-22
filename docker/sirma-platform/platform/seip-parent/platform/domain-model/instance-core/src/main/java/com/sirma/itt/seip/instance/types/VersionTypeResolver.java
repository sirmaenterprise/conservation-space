package com.sirma.itt.seip.instance.types;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.types.NoClassInstnaceTypeResolver.PluginConfiguration;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The extension is used to resolve the type for the version instances. This resolver will strip the version suffix from
 * the passed id and then use the result id to resolve the type through the normal flow for instance type resolving.
 * <p>
 * The resolver also prevents from OutOfMemory error (CMF-29716), where the system tries to load one version over and
 * over again, because the type for the version is resolved, when they are loaded.
 *
 * @author A. Kunchev
 */
@Extension(target = PluginConfiguration.NAME, order = PluginConfiguration.VERSION_RESOLVER_ORDER)
public class VersionTypeResolver implements NoClassInstnaceTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean canResolve(String id) {
		return InstanceVersionService.isVersion(id);
	}

	@Override
	public Optional<InstanceType> resolve(String id) {
		LOGGER.trace("Resolving instance type for version - {}", id);
		Serializable normalizedId = InstanceVersionService.getIdFromVersionId(id);
		return instanceTypeResolver.resolve(normalizedId);
	}
}