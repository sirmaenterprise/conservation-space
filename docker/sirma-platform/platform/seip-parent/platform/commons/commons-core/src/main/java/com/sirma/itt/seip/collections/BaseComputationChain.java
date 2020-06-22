/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;

import com.sirma.itt.seip.Pair;

/**
 * The ComputationChain represents a store of chained predicates and values associated with them. The values could be a
 * single value mapping or suppliers or functions. The class is intended to replace huge blocks of if/else statements.
 *
 * @author BBonev
 * @param <T>
 *            the predicate argument type
 * @param <R>
 *            the predicate mapped value type
 */
public class BaseComputationChain<T, R> {
	/**
	 * Stores the chained operations
	 */
	private final Collection<Pair<T, R>> chain = new LinkedList<>();
	/** The default value. */
	private R defaultValue = null;

	/**
	 * Binds the given value to the given predicate. When the predicate returns true the given value will be returned.
	 *
	 * @param predicate
	 *            the predicate to bind to the value (could not be null).
	 * @param value
	 *            the value to return when the predicate returns <code>true</code>
	 */
	public void addStep(T predicate, R value) {
		Objects.requireNonNull(predicate, "Predicate could not be null");

		getChain().add(new Pair<>(predicate, value));
	}

	/**
	 * Adds the default value to be returned if non of the predicates returns <code>true</code>. If not set
	 * <code>null</code> will be returned upon execution of the chain.
	 *
	 * @param value
	 *            the value
	 */
	public void addDefault(R value) {
		defaultValue = value;
	}

	/**
	 * Execute the chain for the given argument. The argument will be passed to all predicate. The first that returns
	 * <code>true</code> will have it's value returned. If non of them return <code>true</code> then the default value
	 * will be returned.
	 *
	 * @param predicate
	 *            the test predicate that to be called in order to verify the predicate arguments.
	 * @return a value that it bound to a predicate that matches the argument or default value if non matched.
	 */
	public R executeWith(Predicate<T> predicate) {
		return getChain()
				.stream()
					.filter(pair -> predicate.test(pair.getFirst()))
					.findFirst()
					.orElseGet(this::getDefault)
					.getSecond();
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private Pair<T, R> getDefault() {
		return new Pair<>(null, defaultValue);
	}

	/**
	 * Gets the current chain.
	 *
	 * @return the chain
	 */
	protected Collection<Pair<T, R>> getChain() {
		return chain;
	}

}
