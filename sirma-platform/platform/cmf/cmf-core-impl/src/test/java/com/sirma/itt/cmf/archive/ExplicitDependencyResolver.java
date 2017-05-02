package com.sirma.itt.cmf.archive;

import java.util.Collection;
import java.util.Iterator;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.DependencyResolver;

/**
 * {@link DependencyResolver} that is initialized with explicit data
 *
 * @author BBonev
 */
public class ExplicitDependencyResolver implements DependencyResolver {

	private Collection<Instance> data;
	private boolean lazy;

	/**
	 * Instantiates a new explicit dependency resolver.
	 *
	 * @param data
	 *            the data
	 */
	public <I extends Instance> ExplicitDependencyResolver(Collection<I> data) {
		this(data, false);
	}

	/**
	 * Instantiates a new explicit dependency resolver.
	 *
	 * @param data
	 *            the data
	 * @param lazy
	 *            the lazy
	 */
	@SuppressWarnings("unchecked")
	public <I extends Instance> ExplicitDependencyResolver(Collection<I> data, boolean lazy) {
		this.data = (Collection<Instance>) data;
		this.lazy = lazy;
	}

	@Override
	public boolean isLazyLoadingSupported() {
		return lazy;
	}

	@Override
	public Integer countDependencies(Instance instance) {
		return data.size();
	}

	@Override
	public Iterator<Instance> resolveDependenciesLazily(Instance parent) {
		return data.iterator();
	}

	@Override
	public Collection<Instance> resolveDependencies(Instance parent) {
		return data;
	}

}
