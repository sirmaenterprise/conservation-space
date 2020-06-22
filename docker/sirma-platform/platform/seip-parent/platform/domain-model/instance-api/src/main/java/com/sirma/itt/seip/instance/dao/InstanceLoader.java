package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Generic facility for loading instances. The loader provides means to load instance by primary and secondary id where
 * the secondary is optional and may not be supported by all instances. The loader implementation may use
 * {@link InstancePersistCallback} to retrieve any custom information about the loaded instance. <br>
 * All loader methods that return instances should return fully loaded instance if not specified otherwise. <br>
 *
 * @author BBonev
 * @see InstancePersistCallback
 */
public interface InstanceLoader {

	/**
	 * Finds instance by it's primary id. If the method returns <code>null</code> it means that the instance is not
	 * persisted or it's deleted.
	 *
	 * @param <I>
	 *            the instance type
	 * @param id
	 *            the id
	 * @return the loaded instance of <code>null</code>
	 */
	<I extends Instance> I find(Serializable id);

	/**
	 * Finds an instance by it's secondary id. If the instance does not supported secondary id a <code>null</code> will
	 * be returned and not error should be issued.
	 *
	 * @param <I>
	 *            the instance type
	 * @param secondaryId
	 *            the instance secondary id to search
	 * @return the loaded instance of <code>null</code>
	 */
	<I extends Instance> I findBySecondaryId(Serializable secondaryId);

	/**
	 * Batch loads instances by the provided primary ids. Note that the method may return less results then requested if
	 * the any or all of the requested instances were not found.<br>
	 * The returned instances should be the same order as the requested ids.
	 *
	 * @param <I>
	 *            the Instance type
	 * @param ids
	 *            the instance ids to load.
	 * @return the collection of found instance
	 */
	<I extends Instance> Collection<I> load(Collection<? extends Serializable> ids);

	/**
	 * Batch loads instances by the provided secondary ids. Note that the method may return less results then requested
	 * if the any or all of the requested instances were not found or the instance does not support secondary ids.<br>
	 * The returned instances should be the same order as the requested ids.
	 *
	 * @param <I>
	 *            the instance type
	 * @param ids
	 *            the secondary ids to search for
	 * @return the collection of found instances
	 */
	<I extends Instance> Collection<I> loadBySecondaryId(Collection<? extends Serializable> ids);

	/**
	 * Gets the persist callback used to loading instances by the current instance loader.
	 *
	 * @return the persist callback or <code>null</code> if no callback is used for loading
	 */
	InstancePersistCallback getPersistCallback();

}