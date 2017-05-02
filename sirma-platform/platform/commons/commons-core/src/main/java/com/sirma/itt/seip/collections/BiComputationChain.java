/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.function.BiPredicate;

/**
 * The ComputationChain represents a store of chained predicates and values associated with them. The values could be a
 * single value mapping or suppliers or functions. The class is intended to replace huge blocks of if/else statements.
 *
 * @author BBonev
 * @param <T>
 *            the predicate argument type
 * @param <U>
 *            the predicate argument type
 * @param <R>
 *            the predicate mapped value type
 */
public class BiComputationChain<T, U, R> extends BaseComputationChain<BiPredicate<T, U>, R> {

	/**
	 * Execute the chain for the given argument. The argument will be passed to all predicate. The first that returns
	 * <code>true</code> will have it's value returned. If non of them return <code>true</code> then the default value
	 * will be returned.
	 *
	 * @param arg1
	 *            the argument 1 to pass to predicates
	 * @param arg2
	 *            the argument 2 to pass to predicates
	 * @return a value that it bound to a predicate that matches the argument or default value if non matched.
	 */
	public R execute(T arg1, U arg2) {
		return executeWith(p -> p.test(arg1, arg2));
	}
}
