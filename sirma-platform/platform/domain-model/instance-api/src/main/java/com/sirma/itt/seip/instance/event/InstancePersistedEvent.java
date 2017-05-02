package com.sirma.itt.seip.instance.event;

import java.io.Serializable;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.db.EntityPersistedEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Event fired after persist of every object. The event is fired just after the database persist in the same
 * transaction. If the observer modifies the object the changes will not be saved in the DB unless persisted manually.
 *
 * @param <I>
 *            the instance type
 * @author BBonev
 */
@Documentation("Event fired after persist of every object. The event is fired just after the database persist in the same transaction. If the observer modifies the object the changes will not be saved in the DB unless persisted manually.")
public class InstancePersistedEvent<I extends Instance> extends EntityPersistedEvent<Serializable, I>
		implements OperationEvent {

	/** The old version. */
	private I oldVersion;
	private final String operationId;

	/**
	 * Instantiates a new instance persisted event.
	 *
	 * @param instance
	 *            the instance
	 * @param oldVersion
	 *            the old version
	 * @param operationId
	 *            the operation id that triggered the event
	 */
	public InstancePersistedEvent(I instance, I oldVersion, String operationId) {
		super(instance);
		this.oldVersion = oldVersion;
		this.operationId = operationId;
	}

	/**
	 * Gets the old version if any.
	 *
	 * @return the old version
	 */
	public I getOldVersion() {
		return oldVersion;
	}

	@Override
	public String getOperationId() {
		return operationId;
	}

}
