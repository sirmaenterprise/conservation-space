/**
 *
 */
package com.sirma.itt.seip;

import java.util.function.Supplier;

/**
 * Supplier that acts as a proxy of other supplier and caches the produces instance
 *
 * @author BBonev
 * @param <T>
 *            the supplied type
 */
public class CachingSupplier<T> implements Supplier<T>, Resettable {

	private volatile T cachedValue;
	private final Supplier<T> source;

	/**
	 * Create a caching supplier that wraps the given supplier.
	 *
	 * @param <X>
	 *            the generic type
	 * @param source
	 *            the source
	 * @return the supplier
	 */
	public static <X> Supplier<X> of(Supplier<X> source) {
		return new CachingSupplier<>(source);
	}

	/**
	 * Instantiates a new caching supplier.
	 *
	 * @param source
	 *            the source
	 */
	public CachingSupplier(Supplier<T> source) {
		this.source = source;
	}

	@Override
	public T get() {
		if (cachedValue == null) {
			cachedValue = source.get();
		}
		return cachedValue;
	}

	/**
	 * Clears the cached value
	 */
	@Override
	public void reset() {
		cachedValue = null;
	}
}
