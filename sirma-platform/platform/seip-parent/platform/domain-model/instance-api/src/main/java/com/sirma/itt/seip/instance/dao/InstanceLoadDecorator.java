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

	String INSTANCE_DECORATOR = "instanceLoadDecorator";
	String VERSION_INSTANCE_DECORATOR = "versionLoadDecorator";
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

	/**
	 * Optional operation. Mark the given instance as decorated to skip it's future decoration of the given instance in
	 * the current session. Node that the decoration status will be cleared if the method
	 * {@link #clearDecoratedStatus(Instance)} is called with the same instance or instance is refreshed or new instance
	 * copy is loaded.
	 *
	 * @param instance the instance to mark as decorated
	 */
	default void markAsDecorated(Instance instance) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Optional operation. Clear the decoration status of the instance.
	 *
	 * @param instance the instance to clear its status
	 */
	default void clearDecoratedStatus(Instance instance) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Optional operation. Check if the given instance is decorated or not
	 *
	 * @param instance the instance to check
	 * @return true if the instance have already been decorated and false if not.
	 */
	default boolean isDecorated(Instance instance) {
		throw new UnsupportedOperationException();
	}
}