/**
 *
 */
package com.sirma.itt.seip.concurrent.locks;

import java.text.MessageFormat;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;

/**
 * ContextualSync combines the functionality provided by {@link Contextual} and {@link Sync} interfaces to provide
 * locking/synchronization for particular context.
 * <p>
 * This class is useful in cases when synchronization should be performed during processing contextual data. The data is
 * separated for the contexts so the locking and synchronization may also be contextual and not global. This will act as
 * throughput optimization so that the processed data could be partially locked (based on the context).
 *
 * @author BBonev
 * @see ContextualLock
 * @see ContextualReadWriteLock
 */
public interface ContextualSync extends Contextual<Sync>, Sync {

	@Override
	default void await() throws InterruptedException {
		getContextValue().await();
	}

	@Override
	default void await(long timeout) throws InterruptedException {
		getContextValue().await(timeout);
	}

	@Override
	default void await(long timeout, int nanos) throws InterruptedException {
		getContextValue().await(timeout, nanos);
	}

	@Override
	default void signal() {
		getContextValue().signal();
	}

	@Override
	default void signalAll() {
		getContextValue().signalAll();
	}

	/**
	 * Creates contextual sync object with single context. The implementation synchronizes over plain Java
	 * {@link Object}.
	 *
	 * @return the contextual sync
	 */
	static ContextualSync create() {
		return new ContextualReferenceSync(CONTEXT_ID_SUPPLIER, SimpleSync::new);
	}

	/**
	 * Creates contextual sync object using the given context suppler. The implementation synchronizes over plain Java
	 * {@link Object}.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual sync
	 */
	static ContextualSync create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceSync(contextIdSupplier, SimpleSync::new);
	}

	/**
	 * Creates contextual sync object using the given context suppler. The implementation synchronizes over plain Java
	 * {@link Object}.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param syncSupplier
	 *            the sync supplier
	 * @return the contextual sync
	 */
	static ContextualSync create(Supplier<String> contextIdSupplier, Supplier<Sync> syncSupplier) {
		return new ContextualReferenceSync(contextIdSupplier, syncSupplier);
	}

	/**
	 * Simple implementation of {@link ContextualSync} based on the {@link ContextualReference}.
	 *
	 * @author BBonev
	 */
	class ContextualReferenceSync extends ContextualReference<Sync>implements ContextualSync {

		private static final String THIS_OPERATION_IS_FORBIDDEN = "{0} operation is forbidden!";

		/**
		 * Instantiates a new contextual reference sync.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param syncSupplier
		 *            the sync supplier
		 */
		public ContextualReferenceSync(Supplier<String> contextIdSupplier, Supplier<Sync> syncSupplier) {
			super(contextIdSupplier);
			super.initializeWith(syncSupplier);
		}

		@Override
		public Sync clearContextValue() {
			// disallow context clear in order to minimize the programming errors of misuse.
			// this prevents errors when a locking is performed, someone calls this method and then
			// notification is called. in this case the synchronization will never happen.
			throw new IllegalStateException(MessageFormat.format(THIS_OPERATION_IS_FORBIDDEN, "clearContextValue"));
		}

		@Override
		public Sync replaceContextValue(Sync newValue) {
			// disallow context value change in order to minimize the programming errors of misuse.
			// this prevents errors when a locking is performed, someone calls this method with new
			// instance and then notification is called. in this case the synchronization will never
			// happen.
			throw new IllegalStateException(MessageFormat.format(THIS_OPERATION_IS_FORBIDDEN, "replaceContextValue"));
		}

		@Override
		public void initializeWith(Supplier<Sync> initialValue) {
			// disallow context initialization change in order to minimize the programming errors of
			// misuse. this prevents errors when a locking is performed, someone calls this method
			// with new instance and then notification is called. in this case the synchronization
			// will never happen.
			throw new IllegalStateException(MessageFormat.format(THIS_OPERATION_IS_FORBIDDEN, "initializeWith"));
		}

		@Override
		public void reset() {
			// disallow reset in order to minimize the programming errors of misuse.
			throw new IllegalStateException(MessageFormat.format(THIS_OPERATION_IS_FORBIDDEN, "reset"));
		}

	}
}
