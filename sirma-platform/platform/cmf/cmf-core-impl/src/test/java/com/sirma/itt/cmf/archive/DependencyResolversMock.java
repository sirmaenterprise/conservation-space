package com.sirma.itt.cmf.archive;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.domain.util.DependencyResolvers;

/**
 * Mock dependency resolver that can have a resolver set to be used
 *
 * @author BBonev
 */
@ApplicationScoped
@Specializes
public class DependencyResolversMock extends DependencyResolvers {

	private DependencyResolver resolver;

	@Override
	public DependencyResolver getResolver(Instance instance) {
		if (resolver == null) {
			return super.getResolver(instance);
		}
		return resolver;
	}

	/**
	 * Use resolver.
	 *
	 * @param resolverToUse
	 *            the resolver to use
	 */
	public void useResolver(DependencyResolver resolverToUse) {
		resolver = resolverToUse;
	}

}
