package com.sirma.itt.seip.db;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;

/**
 * Generate unique identifiers for persisting in databases.
 *
 * @author BBonev
 */
public interface DatabaseIdManager extends Serializable {

	/**
	 * Generate an id that is optionally tracked if persisted or not.
	 *
	 * @param track
	 *            if the id should be tracked or not.
	 * @return the generated db id.
	 */
	Serializable generate(boolean track);

	/**
	 * Generate an id that is not tracked if persisted or not.
	 *
	 * @return the generated db id.
	 */
	default Serializable generateId() {
		return generate(false);
	}

	/**
	 * Checks if the given id is valid and if not modifies it and returns the valid identifier. If the given id is valid
	 * the same id will be returned.
	 *
	 * @param id
	 *            the id
	 * @return the valid id
	 */
	default Serializable getValidId(Serializable id) {
		return getGenerator().getValidId(id);
	}

	/**
	 * Gets the generator.
	 *
	 * @return the generator
	 */
	DbIdGenerator getGenerator();

	/**
	 * Generate revision id based on the given source id and revision number. The implementation could use the source
	 * information to form new id or to generate something completely new.
	 *
	 * @param src
	 *            the src
	 * @param revision
	 *            the revision
	 * @return the revision id
	 */
	default Serializable getRevisionId(Serializable src, String revision) {
		return getGenerator().generateRevisionId(src, revision);
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
	 *            if the instance should keep track of the generated id if persisted or not. If the Id is generated
	 *            without tracking the method {@link #isPersisted(Entity)} will return <code>true</code> always no
	 *            matter if the entity was persisted or not.
	 * @return the updated entity (same reference as the argument)
	 */
	@SuppressWarnings("unchecked")
	default <I extends Serializable, E extends Entity<I>> E generateStringId(E entity, boolean toKeepTrack) {
		if (entity != null && entity.getId() == null) {
			entity.setId((I) generate(toKeepTrack));
		}
		return entity;
	}

	/**
	 * Checks if the given ID is persisted. The method will return <code>true</code> for IDs that are tracked by the
	 * generator and is known to be persisted (after receiving the persist event). Passing a long value to the method
	 * will result in <code>true</code> as output. For any ID that is not tracked the by class a positive response will
	 * be returned.
	 *
	 * @param id
	 *            the id to check
	 * @return true, if is persisted and <code>false</code> if tracked and is known for not persisted or
	 *         <code>null</code> value.
	 */
	boolean isIdPersisted(Serializable id);

	/**
	 * Checks if the id is registered.
	 *
	 * @param id
	 *            the id
	 * @return true, if id is registered
	 */
	boolean isIdRegistered(Serializable id);

	/**
	 * Checks if the given entity has been persisted.
	 *
	 * @param entity
	 *            the entity
	 * @return <code>true</code>, if is persisted and <code>false</code> if not yet.
	 */
	boolean isPersisted(Entity<? extends Serializable> entity);

	/**
	 * Marks the given entity as persisted.
	 *
	 * @param entity
	 *            the entity
	 * @return <code>true</code>, if the entity successfully has been marked as persisted and <code>false</code> if
	 *         something got wrong ( the entity was invalid or the entity already has been persisted.
	 */
	boolean persisted(Entity<? extends Serializable> entity);

	/**
	 * Register the entity that is going to be persisted or the entity ID was generated externally. <b>NOTE: </b> After
	 * registration the methods {@link #isPersisted(Entity)} and {@link #isIdPersisted(Serializable)} will return
	 * <code>false</code> for the id that is set to the entity.
	 *
	 * @param entity
	 *            the entity
	 */
	void register(Entity<? extends Serializable> entity);

	/**
	 * Remove the given identifier from the internal registers. This is optional operation to unregister previously
	 * generated identifier if it's known never to be persisted.
	 *
	 * @param <S>
	 *            the identifier type
	 * @param id
	 *            the id to unregister
	 * @return <code>true</code> if the given id was registered at all and <code>false</code> if <code>null</code> or
	 *         not registered.
	 */
	<S extends Serializable> boolean unregisterId(S id);

	/**
	 * Unregister the id of the given entity
	 *
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @see #unregisterId(Serializable)
	 */
	default <S extends Serializable> void unregister(Entity<S> entity) {
		if (entity == null || entity.getId() == null) {
			return;
		}
		unregisterId(entity.getId());
	}

	/**
	 * Register an ID that is going to be persisted or ID was generated externally or not tracked initially.<br>
	 * <b>NOTE: </b> After registration the methods {@link #isPersisted(Entity)} and
	 * {@link #isIdPersisted(Serializable)} will return <code>false</code> for the same id.
	 *
	 * @param id
	 *            the entity
	 * @return <code>true</code> if the id was not registered before that and <code>false</code> if the id was known
	 *         before calling the method.
	 */
	boolean registerId(Serializable id);

	/**
	 * Creates a restore point for the given id. Calling any method that modifies the state for the id and then calling
	 * {@link #restoreSavedPoint(Serializable)} will return the internal state to the moment of calling this method.
	 * Calling the method again before calling the restore method will override the previous state.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 *
	 * @param id
	 *            the id
	 */
	void createRestorePoint(Serializable id);

	/**
	 * Checks if there is a restore point for the given id.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 *
	 * @param id
	 *            the id
	 * @return <code>true</code>, if restore point has been found and <code>false</code> if id is <code>null</code> not
	 *         found
	 */
	boolean hasRestorePoint(Serializable id);

	/**
	 * Restores any saved restore point. The method does nothing if there is no restore point created in advance.
	 * <p>
	 * <b>NOTE:</b> USE THIS METHOD IF YOU KNOW WHAT YOU ARE DOING AND WHY.
	 *
	 * @param id
	 *            the id
	 * @return <code>true</code>, if any modifications to the internal state has been done and <code>false</code> if
	 *         there is no restore point or the restore does not modify the internal state at all.
	 */
	boolean restoreSavedPoint(Serializable id);
}
