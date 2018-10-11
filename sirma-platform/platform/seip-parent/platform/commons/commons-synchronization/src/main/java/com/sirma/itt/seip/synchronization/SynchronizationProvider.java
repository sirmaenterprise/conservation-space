package com.sirma.itt.seip.synchronization;

import java.util.function.Supplier;

/**
 * A functional interface that has a {@link Supplier} signature but also throws {@link SynchronizationException}
 *
 * @param <T>
 *            the provide type
 */
@FunctionalInterface
public interface SynchronizationProvider<T> {

	/**
	 * Provide the data or throw {@link SynchronizationException}
	 *
	 * @return provided data
	 * @throws SynchronizationException
	 *             the synchronization exception if cannot provide the data
	 */
	T provide() throws SynchronizationException;
}