package com.sirma.itt.seip.domain.util;

import java.util.Collection;
import java.util.Iterator;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides means of resolving dependent elements based on an {@link Instance}.
 *
 * @author BBonev
 */
@Documentation("Provides means of resolving dependent elements based on an {@link Instance}.")
public interface DependencyResolver extends Plugin {

	String TARGET_NAME = "dependencyResolver";

	/**
	 * Checks if is lazy loading supported. If <code>true</code> the dependencies will be loaded using the
	 * {@link #resolveDependenciesLazily(Instance)} otherwise will be used {@link #resolveDependencies(Instance)}
	 * method.
	 *
	 * @return true, if is lazy loading supported
	 */
	boolean isLazyLoadingSupported();

	/**
	 * Count dependencies. This is optional operation. If counting is expensive the method could return
	 * <code>null</code> or -1 to indicate that no counting was performed. The method should return a integer value
	 * bigger or equal to zero if counting is supported.
	 * <p>
	 * <b>NOTE:</b> If the method returns 0 no calls to {@link #resolveDependencies(Instance)} or
	 * {@link #resolveDependenciesLazily(Instance)} will occur.
	 *
	 * @param instance
	 *            the instance
	 * @return <code>null</code> or negative number if counting is not supported and zero or positive number if counting
	 *         is possible and this is the calculated dependencies.
	 */
	Integer countDependencies(Instance instance);

	/**
	 * Resolve dependencies lazily. If implementation support lazy loading should return a non null iterator object that
	 * provides the instances.
	 *
	 * @param parent
	 *            the parent
	 * @return an iterator that will provide the dependencies.
	 */
	Iterator<Instance> resolveDependenciesLazily(Instance parent);

	/**
	 * If lazy loading is not supported the implementation is expected to provide all dependencies.
	 *
	 * @param parent
	 *            the parent
	 * @return the collection
	 */
	Collection<Instance> resolveDependencies(Instance parent);

	/**
	 * Returns the batch size used for loading instances lazily. If no lazy loading is supported the method should
	 * return value less than a 0. The default implementation just returns -1.
	 *
	 * @return the batch size.
	 */
	default int currentBatchSize() {
		return -1;
	}
}
