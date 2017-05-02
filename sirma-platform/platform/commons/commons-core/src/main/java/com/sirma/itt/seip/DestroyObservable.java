package com.sirma.itt.seip;

import java.util.function.Consumer;

/**
 * DestroyObservable interface defines a method to register one or more observers to be notified when the value going to
 * be destroyed.
 *
 * @param <T>
 *            the generic type
 * @see Destroyable
 */
@FunctionalInterface
public interface DestroyObservable<T> {

	/**
	 * On destroy. to allow an object to be observed for disposition and if needed to be forced
	 *
	 * @param destroyConsumer
	 *            the destroy consumer
	 */
	void onDestroy(Consumer<T> destroyConsumer);

	/**
	 * Checks if the first argument implements the {@link DestroyObservable} interface and if so register the given
	 * consumer.
	 *
	 * @param observable
	 *            the observable target
	 * @param toBeNotified
	 *            the consumer to be notified
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void addObserver(Object observable, Consumer<?> toBeNotified) {
		if (observable instanceof DestroyObservable) {
			((DestroyObservable) observable).onDestroy(toBeNotified);
		}
	}
}
