package com.sirma.itt.seip.synchronization;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Synchronization data provider used to provide data during synchronization process run via
 * {@link SynchronizationRunner}.
 *
 * @author BBonev
 * @param <I>
 *            the Identity type of the items being synchronized
 * @param <E>
 *            the type of the elements being synchronized
 */
public interface SynchronizationDataProvider<I, E> extends SynchronizationProvider<Collection<? extends E>> {

	/**
	 * Provide the data for synchronization. If the data cannot be provided at the moment the method should throw
	 * {@link SynchronizationException} to indicate that instead of empty collection.
	 *
	 * @return the data for synchronization.
	 * @throws SynchronizationException
	 *             if the data could not be provided and synchronization should be aborted
	 */
	@Override
	Collection<? extends E> provide() throws SynchronizationException;

	/**
	 * Gets the identity of a element return from the {@link #provide()} method. This method should provide unique
	 * identifier for each entity provided by the {@link #provide()} method.
	 *
	 * @param item
	 *            the item to get the identity from
	 * @return the identity of the element
	 */
	I getIdentity(E item);

	/**
	 * Creates a lazy initialized instance of the {@link SynchronizationDataProvider}. Note that both arguments are
	 * required.
	 *
	 * @param <I>
	 *            the Identity type of the items being synchronized
	 * @param <E>
	 *            the type of the elements being synchronized
	 * @param provider
	 *            the provider
	 * @param identityResolver
	 *            the identity resolver
	 * @return the synchronization source
	 */
	static <I, E> SynchronizationDataProvider<I, E> create(SynchronizationProvider<Collection<E>> provider,
			Function<E, I> identityResolver) {
		return new SynchronizationCallback<>(provider, identityResolver);
	}

	/**
	 * Simple implementation of {@link SynchronizationDataProvider} that uses functions to provide the implementation.
	 *
	 * @author BBonev
	 * @param <A>
	 *            the Identity type of the items being synchronized
	 * @param <T>
	 *            the type of the elements being synchronized
	 */
	class SynchronizationCallback<A, T> implements SynchronizationDataProvider<A, T> {

		private final SynchronizationProvider<Collection<T>> provider;
		private final Function<T, A> identityResolver;

		/**
		 * Instantiates a new synchronization callback.
		 *
		 * @param provider
		 *            the provider to use
		 * @param identityResolver
		 *            the identity resolver to use
		 */
		public SynchronizationCallback(SynchronizationProvider<Collection<T>> provider,
				Function<T, A> identityResolver) {
			this.provider = Objects.requireNonNull(provider);
			this.identityResolver = Objects.requireNonNull(identityResolver);
		}

		@Override
		public Collection<? extends T> provide() throws SynchronizationException {
			return provider.provide();
		}

		@Override
		public A getIdentity(T item) {
			return  identityResolver.apply(item);
		}
	}
}
