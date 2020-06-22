package com.sirma.itt.seip.domain.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Provider for {@link DependencyResolver} implementations.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DependencyResolvers {

	private Iterable<DependencyResolver> resolvers;

	/**
	 *
	 */
	public DependencyResolvers() {
		// just default constructor. CDI requirement
	}

	/**
	 * Instantiates a new dependency resolvers.
	 *
	 * @param resolvers
	 *            the resolvers
	 */
	@Inject
	public DependencyResolvers(@ExtensionPoint(DependencyResolver.TARGET_NAME) Iterable<DependencyResolver> resolvers) {
		this.resolvers = resolvers;
	}

	/**
	 * Gets the default resolver.
	 *
	 * @return the default resolver
	 */
	public DependencyResolver getDefaultResolver() {
		return resolvers.iterator().next();
	}

	/**
	 * Gets the resolver.
	 *
	 * @param instance
	 *            the instance
	 * @return the resolver
	 */
	public DependencyResolver getResolver(Instance instance) {
		return getDefaultResolver();
	}

}
