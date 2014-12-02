package com.sirma.itt.emf.instance.dao;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.instance.InstanceEventType;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Base {@link InstanceEventProvider} implementation that realize the
 * {@link #createEvent(InstanceEventType, Instance, Object...)}.
 * 
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public abstract class BaseInstanceEventProvider<I extends Instance> implements
		InstanceEventProvider<I> {

	@Override
	public EmfEvent createEvent(InstanceEventType type, I instance, Object... otherArgs) {
		if ((type == null) || (instance == null)) {
			return null;
		}
		switch (type) {
			case CREATE:
				return createCreateEvent(instance);
			case FIRST_PERSIST:
				return createBeforeInstancePersistEvent(instance);
			case PERSIST:
				checkRequiredArguments(otherArgs);
				// TODO: we should pass the operation via the method args somehow
				return createPersistedEvent(instance, (I) otherArgs[0], null);
			case OPEN:
				return createOpenEvent(instance);
			case CHANGE:
				return createChangeEvent(instance);
			case STOP:
				return createBeforeInstanceCancelEvent(instance);
			case DELETE:
				return createBeforeInstanceDeleteEvent(instance);
			case ATTACH:
				checkRequiredArguments(otherArgs);
				return createAttachEvent(instance, (Instance) otherArgs[0]);
			case DETACH:
				checkRequiredArguments(otherArgs);
				return createDetachEvent(instance, (Instance) otherArgs[0]);
			default:
				// not supported event type
				break;
		}
		return null;
	}

	/**
	 * Check required arguments.
	 * 
	 * @param otherArgs
	 *            the other args
	 */
	private void checkRequiredArguments(Object... otherArgs) {
		if ((otherArgs.length == 0)
				|| ((otherArgs[0] != null) && !(otherArgs[0] instanceof Instance))) {
			throw new IllegalArgumentException("Expected one more instance element");
		}
	}
}
