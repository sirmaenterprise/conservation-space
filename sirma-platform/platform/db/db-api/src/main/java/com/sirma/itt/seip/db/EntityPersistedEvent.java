package com.sirma.itt.seip.db;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Base event fired after persist of every object. The event is fired just after the database persist in the same
 * transaction. If the observer modifies the object the changes will not be saved in the DB unless persisted manually.
 *
 * @param <E>
 *            the any entity type
 * @param <S>
 *            the entity id type
 * @author BBonev
 */
@Documentation("Base event fired after persist of every object. The event is fired just after the database persist in the same transaction. If the observer modifies the object the changes will not be saved in the DB unless persisted manually.")
public class EntityPersistedEvent<S extends Serializable, E extends Entity<S>> extends AbstractInstanceEvent<E> {

	/**
	 * Instantiates a new entity persisted event.
	 *
	 * @param instance
	 *            the instance
	 */
	public EntityPersistedEvent(E instance) {
		super(instance);
	}

}
