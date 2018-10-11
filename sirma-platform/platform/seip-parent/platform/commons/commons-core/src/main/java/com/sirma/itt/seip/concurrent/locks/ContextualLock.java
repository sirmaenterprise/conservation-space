/**
 *
 */
package com.sirma.itt.seip.concurrent.locks;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Contextual {@link Lock}.
 *
 * @author BBonev
 */
public interface ContextualLock extends Contextual<Lock>, Lock {

	@Override
	default void lock() {
		getContextValue().lock();
	}

	@Override
	default void lockInterruptibly() throws InterruptedException {
		getContextValue().lockInterruptibly();
	}

	@Override
	default boolean tryLock() {
		return getContextValue().tryLock();
	}

	@Override
	default boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return getContextValue().tryLock(time, unit);
	}

	@Override
	default void unlock() {
		getContextValue().unlock();
	}

	@Override
	default Condition newCondition() {
		return getContextValue().newCondition();
	}

	/**
	 * Creates new reentrant lock {@link ContextualLock} in fixed context.
	 *
	 * @return the contextual lock
	 */
	static ContextualLock create() {
		return new ContextualReferenceLock(CONTEXT_ID_SUPPLIER, ReentrantLock::new);
	}

	/**
	 * Creates new reentrant lock {@link ContextualLock} instances using the given context id supplier.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual lock
	 */
	static ContextualLock create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceLock(contextIdSupplier, ReentrantLock::new);
	}

	/**
	 * Creates {@link ContextualLock} instance. The initial contextual value used will be created via the given initial
	 * value supplier. Note that the supplier should not return <code>null</code> or {@link NullPointerException} will
	 * be thrown.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual lock
	 */
	static ContextualLock create(Supplier<String> contextIdSupplier, Supplier<Lock> initialValue) {
		return new ContextualReferenceLock(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualLock} implementation using a {@link ContextualReference}.
	 *
	 * @author BBonev
	 */
	class ContextualReferenceLock extends ValidatingContextualReference<Lock>implements ContextualLock {

		/**
		 * Instantiates a new contextual lock.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceLock(Supplier<String> contextIdSupplier, Supplier<Lock> initialValue) {
			super(contextIdSupplier, initialValue,
					(l) -> Objects.requireNonNull(l, "Cannot work with null initial value!"));
		}
	}
}
