package com.sirma.itt.emf.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.event.instance.EntityPersistedEvent;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Generate unique identifiers for persisting in databases.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SequenceEntityGenerator {

	/** The lock. REVIEW:BB probably could be implemented with RWLock. */
	private static final Lock LOCK = new ReentrantLock();

	/** The list of all generated IDs that are tracked for persistence. */
	private static Set<Serializable> generatedIds = new LinkedHashSet<>(1024);

	/** A place for ids that has been persisted before transaction completion. */
	private static Set<Serializable> toPersistIds = new LinkedHashSet<>(1024);

	/** The restore points. */
	private static Map<Serializable, IdState> restorePoints = Collections
			.synchronizedMap(new HashMap<Serializable, IdState>(64));

	/** The generator. */
	private static DbIdGenerator generator;

	/** The id generator. */
	@Inject
	private DbIdGenerator idGenerator;

	/**
	 * On application start.
	 * 
	 * @param event
	 *            the event
	 */
	public void onApplicationStart(@Observes ApplicationStartupEvent event) {
		generator = idGenerator;
	}

	/**
	 * Generate an id that is optionally tracked if persisted or not.
	 * 
	 * @param track
	 *            if the id should be tracked or not.
	 * @return the generated db id.
	 */
	public Serializable generate(boolean track) {
		return generateIdInternal(track);
	}

	/**
	 * Generate an id that is not tracked if persisted or not.
	 * 
	 * @return the generated db id.
	 */
	public static Serializable generateId() {
		return generateIdInternal(false);
	}

	/**
	 * Generate an id that is tracked or not if persisted or not.
	 * 
	 * @param track
	 *            if the id should be tracked or not.
	 * @return the generated db id.
	 */
	public static Serializable generateId(boolean track) {
		return generateIdInternal(track);
	}

	/**
	 * Generate an id for the given entity if an id has not been set already.
	 * 
	 * @param <I>
	 *            the generic ID type
	 * @param <E>
	 *            the concrete entity type
	 * @param entity
	 *            the entity to set the id
	 * @param toKeepTrack
	 *            if the instance should keep track of the generated id if persisted or not. If the
	 *            Id is generated without tracking the method {@link #isPersisted(Entity)} will
	 *            return <code>true</code> always no matter if the entity was persisted or not.
	 * @return the updated entity (same reference as the argument)
	 */
	@SuppressWarnings("unchecked")
	public static <I extends Serializable, E extends Entity<I>> E generateStringId(E entity,
			boolean toKeepTrack) {
		if ((entity != null) && (entity.getId() == null)) {
			entity.setId((I) generateIdInternal(toKeepTrack));
		}
		return entity;
	}

	/**
	 * Generate id internal.
	 *
	 * @param toKeepTrack
	 *            the to keep track
	 * @return the string
	 */
	private static Serializable generateIdInternal(boolean toKeepTrack) {
		LOCK.lock();
		try {
			Serializable id = generator.generateId().toString();
			if (toKeepTrack) {
				generatedIds.add(id);
			}
			return id;
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * Checks if the given ID is persisted. The method will return <code>true</code> for IDs that
	 * are tracked by the generator and is known to be persisted (after receiving the persist
	 * event). Passing a long value to the method will result in <code>true</code> as output. For
	 * any ID that is not tracked the by class a positive response will be returned.
	 * 
	 * @param id
	 *            the id to check
	 * @return true, if is persisted and <code>false</code> if tracked and is known for not
	 *         persisted or <code>null</code> value.
	 */
	public static boolean isIdPersisted(Serializable id) {
		if (id == null) {
			return false;
		}
		// all entities that have a long ID and that id is present are considered for persisted
		if (id instanceof Long) {
			return true;
		}
		LOCK.lock();
		try {
			boolean inPersisted = toPersistIds.contains(id);
			boolean notInGenerated = !generatedIds.contains(id);
			return inPersisted || notInGenerated;
		} finally {
			LOCK.unlock();
		}
	}


	/**
	 * Checks if the id is registered.
	 *
	 * @param id the id
	 * @return true, if id is registered
	 */
	public static boolean isIdRegistered(Serializable id) {
		if (id == null) {
			return false;
		}
		LOCK.lock();
		try {
			return generatedIds.contains(id) || toPersistIds.contains(id);
		} finally {
			LOCK.unlock();
		}
	}
	/**
	 * Checks if the given entity has been persisted.
	 *
	 * @param <S>
	 *            the ID type
	 * @param entity
	 *            the entity
	 * @return <code>true</code>, if is persisted and <code>false</code> if not yet.
	 */
	public static <S extends Serializable> boolean isPersisted(Entity<S> entity) {
		if ((entity == null) || (entity.getId() == null)) {
			return false;
		}
		return isIdPersisted(entity.getId());
	}

	/**
	 * Marks the given entity as persisted.
	 *
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @return <code>true</code>, if the entity successfully has been marked as persisted and
	 *         <code>false</code> if something got wrong ( the entity was invalid or the entity
	 *         already has been persisted.
	 */
	public static <S extends Serializable> boolean persisted(Entity<S> entity) {
		if ((entity == null) || (entity.getId() == null)) {
			return false;
		}
		LOCK.lock();
		try {
			if (generatedIds.remove(entity.getId())) {
				toPersistIds.add(entity.getId());
				return true;
			}
		} finally {
			LOCK.unlock();
		}
		return false;
	}

	/**
	 * Register the entity that is going to be persisted or the entity ID was generated externally.
	 * <b>NOTE: </b> After registration the methods {@link #isPersisted(Entity)} and
	 * {@link #isIdPersisted(Serializable)} will return <code>false</code> for the id that is set to
	 * the entity.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 */
	public static <S extends Serializable> void register(Entity<S> entity) {
		if ((entity != null) && (entity.getId() != null)) {
			LOCK.lock();
			try {
				generatedIds.add(entity.getId());
			} finally {
				LOCK.unlock();
			}
		}
	}

	/**
	 * Remove the given identifier from the internal registers. This is optional operation to
	 * unregister previously generated identifier if it's known never to be persisted.
	 * 
	 * @param <S>
	 *            the identifier type
	 * @param id
	 *            the id to unregister
	 * @return <code>true</code> if the given id was registered at all and <code>false</code> if
	 *         <code>null</code> or not registered.
	 */
	public static <S extends Serializable> boolean unregister(S id) {
		if (id != null) {
			LOCK.lock();
			try {
				boolean present = generatedIds.remove(id);
				present |= toPersistIds.remove(id);
				return present;
			} finally {
				LOCK.unlock();
			}
		}
		return false;
	}

	/**
	 * Register an ID that is going to be persisted or ID was generated externally or not tracked
	 * initially.<br>
	 * <b>NOTE: </b> After registration the methods {@link #isPersisted(Entity)} and
	 * {@link #isIdPersisted(Serializable)} will return <code>false</code> for the same id.
	 * 
	 * @param id
	 *            the entity
	 * @return <code>true</code> if the id was not registered before that and <code>false</code> if
	 *         the id was known before calling the method.
	 */
	public static boolean registerId(Serializable id) {
		if (id != null) {
			LOCK.lock();
			try {
				return generatedIds.add(id);
			} finally {
				LOCK.unlock();
			}
		}
		return false;
	}

	/**
	 * Creates the restore point for the given id. Calling any method that modifies the state for
	 * the id and then calling {@link #restoreSavedPoint(Serializable)} will return the internal
	 * state to the moment of calling the current method. Calling the method again before calling
	 * the restore method will override the previous state.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 * 
	 * @param id
	 *            the id
	 */
	public static void createRestorePoint(Serializable id) {
		if (id == null) {
			return;
		}
		LOCK.lock();
		IdState state;
		try {
			state = new IdState(id);
		} finally {
			LOCK.unlock();
		}
		restorePoints.put(id, state);
	}

	/**
	 * Checks if there is a restore point for the given id.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 * 
	 * @param id
	 *            the id
	 * @return <code>true</code>, if restore point has been found and <code>false</code> if id is
	 *         <code>null</code> not found
	 */
	public static boolean hasRestorePoint(Serializable id) {
		if (id == null) {
			return false;
		}
		return restorePoints.containsKey(id);
	}

	/**
	 * Restores any saved restore point. The method does nothing if there is no restore point
	 * created in advance.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 * 
	 * @param id
	 *            the id
	 * @return <code>true</code>, if any modifications to the internal state has been done and
	 *         <code>false</code> if there is no restore point or the restore does not modify the
	 *         internal state at all.
	 */
	public static boolean restoreSavedPoint(Serializable id) {
		if (!hasRestorePoint(id)) {
			return false;
		}
		IdState state = restorePoints.remove(id);
		LOCK.lock();
		try {
			return state.restore();
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * On entity persist failure.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <E extends Entity<S>, S extends Serializable> void onEntityPersistFailure(
			@Observes(during = TransactionPhase.AFTER_FAILURE) EntityPersistedEvent<E, S> event) {
		onTransactionFail(event.getInstance().getId());
	}

	/**
	 * On entity persist failure.
	 * 
	 * @param event
	 *            the event
	 */
	public void onInstancePersistFailure(
			@Observes(during = TransactionPhase.AFTER_FAILURE) EntityPersistedEvent<Instance, Serializable> event) {
		onTransactionFail(event.getInstance().getId());
	}

	/**
	 * On transaction fail.
	 * 
	 * @param id
	 *            the id
	 */
	private void onTransactionFail(Serializable id) {
		LOCK.lock();
		try {
			// do not add the id back if not in the first list at all
			if (toPersistIds.remove(id)) {
				generatedIds.add(id);
			}
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * On entity persist success.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	public <E extends Entity<S>, S extends Serializable> void onEntityPersistSuccess(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) EntityPersistedEvent<E, S> event) {
		onTransactionSuccess(event.getInstance().getId());
	}

	/**
	 * On entity persist success.
	 * 
	 * @param event
	 *            the event
	 */
	public void onInstancePersistSuccess(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) EntityPersistedEvent<Instance, Serializable> event) {
		onTransactionSuccess(event.getInstance().getId());
	}

	/**
	 * On transaction success.
	 * 
	 * @param id
	 *            the id
	 */
	private void onTransactionSuccess(Serializable id) {
		LOCK.lock();
		try {
			toPersistIds.remove(id);
			generatedIds.remove(id);
		} finally {
			LOCK.unlock();
		}
	}

	/**
	 * Class that represents a state of the a single id in the internal model. This should be used
	 * only when there are needed modifications to the model and need to be restored after that
	 * 
	 * @author BBonev
	 */
	private static class IdState {
		private final Serializable id;
		private final boolean generated;
		private final boolean toPersist;

		/**
		 * Instantiates a new id state.
		 * 
		 * @param id
		 *            the id
		 */
		private IdState(Serializable id) {
			this.id = id;
			generated = generatedIds.contains(id);
			toPersist = toPersistIds.contains(id);
		}

		/**
		 * Restores the state for the current id
		 * 
		 * @return true, if any modifications to the internal model has been done
		 */
		boolean restore() {
			boolean result = false;
			if (generated) {
				result |= generatedIds.add(id);
			}
			if (toPersist) {
				result |= toPersistIds.add(id);
			}
			return result;
		}
	}

}
