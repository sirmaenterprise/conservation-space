package com.sirma.itt.seip;

import java.util.Optional;

/**
 * Represents classes that can be cloned. Different from {@link Cloneable} in order to not be confused with it.
 *
 * @author Adrian Mitev
 */
public interface Copyable<T> {

	/**
	 * Creates a cloning of the instance.
	 *
	 * @return instance clone.
	 */
	T createCopy();

	/**
	 * Create a copy of the given object if supported. If the given object implements {@link Copyable} interface then
	 * it will be copied and returned otherwise empty Optional will be returned.
	 *
	 * @param toCopy the object to copy if supported
	 * @param <C> the input type and output type
	 * @return a copy of the argument produced by the {@link #createCopy()} method it applicable or {@link Optional#empty()}
	 */
	@SuppressWarnings("unchecked")
	static <C> Optional<C> copy(C toCopy) {
		if (toCopy instanceof Copyable<?>) {
			return Optional.of(((Copyable<C>) toCopy).createCopy());
		}
		return Optional.empty();
	}
}
