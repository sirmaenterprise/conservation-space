/**
 *
 */
package com.sirma.itt.seip.concurrent.locks;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Contextual {@link ReadWriteLock} implementation that uses {@link ReentrantReadWriteLock} be default.
 *
 * @author BBonev
 */
public interface ContextualReadWriteLock extends Contextual<ReadWriteLock>, ReadWriteLock {

	@Override
	default Lock readLock() {
		return getContextValue().readLock();
	}

	@Override
	default Lock writeLock() {
		return getContextValue().writeLock();
	}

	/**
	 * Lock for read.
	 */
	default void lockForRead() {
		readLock().lock();
	}

	/**
	 * Lock for write.
	 */
	default void lockForWrite() {
		writeLock().lock();
	}

	/**
	 * Unlock for read.
	 */
	default void unlockForRead() {
		readLock().unlock();
	}

	/**
	 * Unlock for write.
	 */
	default void unlockForWrite() {
		writeLock().unlock();
	}

	/**
	 * Creates new reentrant read write lock {@link ContextualReadWriteLock} instance with single context.
	 *
	 * @return the contextual lock
	 */
	static ContextualReadWriteLock create() {
		return new ContextualReferenceReadWriteLock(CONTEXT_ID_SUPPLIER, ReentrantReadWriteLock::new);
	}

	/**
	 * Creates new reentrant read write lock {@link ContextualReadWriteLock} instances using the given context id
	 * supplier.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual lock
	 */
	static ContextualReadWriteLock create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceReadWriteLock(contextIdSupplier, ReentrantReadWriteLock::new);
	}

	/**
	 * Creates {@link ContextualReadWriteLock} instance. The initial contextual value used will be created via the given
	 * initial value supplier. Note that the supplier should not return <code>null</code> or
	 * {@link NullPointerException} will be thrown.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual lock
	 */
	static ContextualReadWriteLock create(Supplier<String> contextIdSupplier, Supplier<ReadWriteLock> initialValue) {
		return new ContextualReferenceReadWriteLock(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualLock} implementation using a {@link ContextualReference}.
	 *
	 * @author BBonev
	 */
	class ContextualReferenceReadWriteLock extends ValidatingContextualReference<ReadWriteLock>
			implements ContextualReadWriteLock {

		/**
		 * Instantiates a new contextual reference read write lock.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceReadWriteLock(Supplier<String> contextIdSupplier,
				Supplier<ReadWriteLock> initialValue) {
			super(contextIdSupplier, initialValue,
					(l) -> Objects.requireNonNull(l, "Cannot work with null initial value!"));
		}
	}
}
