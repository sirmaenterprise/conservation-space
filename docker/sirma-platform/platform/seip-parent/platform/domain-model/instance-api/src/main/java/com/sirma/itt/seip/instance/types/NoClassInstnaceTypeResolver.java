package com.sirma.itt.seip.instance.types;

import java.util.Optional;

import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides means to resolve instance type.
 *
 * @author A. Kunchev
 */
public interface NoClassInstnaceTypeResolver extends Plugin {

	/**
	 * Checks, if specific implementation can resolve the passed value.
	 *
	 * @param id to check
	 * @return {@code true} if the implementation can resolve the type, {@code false} otherwise
	 */
	boolean canResolve(String id);

	/**
	 * Performs the actual type resolving.
	 *
	 * @param id which type should be resolved
	 * @return {@link Optional} of resolved or {@link Optional#empty()} if the type cannot the resolved
	 */
	Optional<InstanceType> resolve(String id);

	/**
	 * Configuration for the extensions.
	 *
	 * @author A. Kunchev
	 */
	class PluginConfiguration {
		public static final String NAME = "noClassInstnaceTypeResolver";
		public static final double CLASS_RESOLVER_ORDER = 1.0;
		public static final double VERSION_RESOLVER_ORDER = 1000.0;
		public static final double OBJECT_INSTANCE_RESOLVER_ORDER = 9999999.9; // last in the chain of execution

		private PluginConfiguration() {}
	}
}