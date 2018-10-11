package com.sirma.itt.seip.instance.dao;

import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * InstanceLoadDecorator is called after loading of instance before being shown to the user. The
 * implementations may provide or load additional information and populate the passed instance or
 * instances.
 * <p>
 * The extension allows parallel processing for all extensions that return <code>true</code> from
 * {@link #allowParallelProcessing()}. The parallel extensions will be run before non parallel so
 * that the ordered synchronous extensions could work with data provided by the parallel. The
 * extension point is ordered and the one with higher order will be executed later. Note that the
 * order is relevant only for the synchronous extensions.
 *
 * @author A. Kunchev
 * @author BBonev
 */
public interface InstanceLoadDecorator extends Plugin {

	/** The plugin name. */
	String TARGET_NAME = "instanceLoadDecorator";
	double MAX_ORDER = 1_000_000.0;

	/**
	 * Can be used for some modification on single instance. The implementations of this interface provides the way of
	 * calling this method in ordered chain, which could be used to modify the instance in different ways.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instance
	 *            the instance which will be modified
	 */
	<I extends Instance> void decorateInstance(I instance);

	/**
	 * Can be used for modification on multiple instances. The implementations of this interface provides the way of
	 * calling this method in ordered chain, which could be used to modify the instances in different ways.
	 *
	 * @param <I>
	 *            the generic type
	 * @param collection
	 *            collection of instances
	 */
	<I extends Instance> void decorateResult(Collection<I> collection);

	/**
	 * Allow parallel processing of the given decorator. Default returned value is <code>true</code>.
	 *
	 * @return true, if processing could be done in parallel
	 */
	default boolean allowParallelProcessing() {
		return true;
	}
}
