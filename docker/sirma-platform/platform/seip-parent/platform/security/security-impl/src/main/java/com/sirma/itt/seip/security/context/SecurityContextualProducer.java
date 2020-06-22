/**
 *
 */
package com.sirma.itt.seip.security.context;

import java.util.function.Supplier;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.ContextualBlockingDeque;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.collections.ContextualDeque;
import com.sirma.itt.seip.collections.ContextualList;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.concurrent.locks.ContextualSync;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;

/**
 * Produces {@link Contextual} objects backed by the {@link SecurityContext#getCurrentTenantId()}. Effectively the
 * produces objects are multitenant aware.
 *
 * @author BBonev
 */
@Singleton
public class SecurityContextualProducer {

	@Inject
	private SecurityContext securityContext;

	/**
	 * Produce reference.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the contextual
	 */
	@Produces
	public <T> Contextual<T> produceReference() {
		return new ContextualReference<>(getTenantIdSupplier());
	}

	/**
	 * Produce map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the contextual map
	 */
	@Produces
	public <K, V> ContextualMap<K, V> produceMap() {
		return ContextualMap.create(getTenantIdSupplier());
	}

	/**
	 * Produce concurrent map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the contextual map
	 */
	@Produces
	public <K, V> ContextualConcurrentMap<K, V> produceConcurrentMap() {
		return ContextualConcurrentMap.create(getTenantIdSupplier());
	}

	/**
	 * Produce set.
	 *
	 * @param <V>
	 *            the value type
	 * @return the contextual set
	 */
	@Produces
	public <V> ContextualSet<V> produceSet() {
		return ContextualSet.create(getTenantIdSupplier());
	}

	/**
	 * Produce lock.
	 *
	 * @return the lock
	 */
	@Produces
	public ContextualLock produceLock() {
		return ContextualLock.create(getTenantIdSupplier());
	}

	/**
	 * Produce read write lock.
	 *
	 * @return the read write lock
	 */
	@Produces
	public ContextualReadWriteLock produceReadWriteLock() {
		return ContextualReadWriteLock.create(getTenantIdSupplier());
	}

	/**
	 * Produce contextual sync.
	 *
	 * @return the contextual sync
	 */
	@Produces
	public ContextualSync produceContextualSync() {
		return ContextualSync.create(getTenantIdSupplier());
	}

	/**
	 * Produce contextual list.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the contextual list
	 */
	@Produces
	public <T> ContextualList<T> produceContextualList() {
		return ContextualList.create(getTenantIdSupplier());
	}

	/**
	 * Produce contextual deque.
	 *
	 * @param <E>
	 *            the element type
	 * @return the contextual deque
	 */
	@Produces
	public <E> ContextualDeque<E> produceContextualDeque() {
		return ContextualDeque.create(getTenantIdSupplier());
	}

	/**
	 * Produce contextual blocking deque.
	 *
	 * @param <E>
	 *            the element type
	 * @return the contextual blocking deque
	 */
	@Produces
	public <E> ContextualBlockingDeque<E> produceContextualBlockingDeque() {
		return ContextualBlockingDeque.create(getTenantIdSupplier());
	}

	private Supplier<String> getTenantIdSupplier() {
		// remove the reference to the current class
		SecurityContext context = securityContext;
		return () -> context.getCurrentTenantId();
	}
}
