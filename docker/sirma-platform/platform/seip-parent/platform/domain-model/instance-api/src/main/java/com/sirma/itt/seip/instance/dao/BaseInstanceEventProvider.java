package com.sirma.itt.seip.instance.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventType;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.event.AfterInstancePublishEvent;
import com.sirma.itt.seip.instance.event.BeforeInstancePublishEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;

/**
 * Base {@link InstanceEventProvider} implementation that realize the
 * {@link #createEvent(EventType, Instance, String, Object...)}.
 *
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public abstract class BaseInstanceEventProvider<I extends Instance> implements InstanceEventProvider<I> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceEventProvider.class);

	@Override
	@SuppressWarnings("unchecked")
	public EmfEvent createEvent(EventType type, Instance instance, String operationId, Object... otherArgs) {
		if (type == null || instance == null) {
			return null;
		}
		I local = (I) instance;
		switch (type) {
			case CREATE:
				return createCreateEvent(local);
			case FIRST_PERSIST:
				return createBeforeInstancePersistEvent(local);
			case PERSIST:
				checkRequiredArguments(1, otherArgs);
				return createPersistedEvent(local, (I) otherArgs[0], operationId);
			case OPEN:
				return createOpenEvent(local);
			case CHANGE:
				return createChangeEvent(local);
			case STOP:
				return createBeforeInstanceCancelEvent(local);
			case DELETE:
				return createBeforeInstanceDeleteEvent(local);
			case ATTACH:
				checkRequiredArguments(1, otherArgs);
				return createAttachEvent(local, (Instance) otherArgs[0]);
			case DETACH:
				checkRequiredArguments(1, otherArgs);
				return createDetachEvent(local, (Instance) otherArgs[0]);
			case PUBLISH:
				checkRequiredArguments(1, otherArgs);
				return createBeforeInstancePublishEvent(local, operationId, (Instance) otherArgs[0]);
			default:
				LOGGER.warn("Not supported event type {}", type);
				break;
		}
		return null;
	}

	@Override
	public BeforeInstancePublishEvent<I, ? extends AfterInstancePublishEvent<I, TwoPhaseEvent>> createBeforeInstancePublishEvent(
			I instance, String operationId, Instance revision) {
		LOGGER.trace("Trying to create publish event for instance {} that does not support publish, yet.",
				instance != null ? instance.getClass() : "null");
		return null;
	}

	/**
	 * Check required arguments.
	 *
	 * @param numberOfArgs
	 *            the number of arguments to check
	 * @param otherArgs
	 *            the other args
	 */
	private void checkRequiredArguments(int numberOfArgs, Object[] otherArgs) {
		if (otherArgs == null && numberOfArgs > 0 || otherArgs != null && otherArgs.length < numberOfArgs) {
			throw new EmfRuntimeException("Expected " + numberOfArgs + " instance arguments");
		}
		if (numberOfArgs <= 0) {
			return;
		}
		if (otherArgs == null) {
			return;
		}
		for (int i = 0; i < numberOfArgs; i++) {
			Object object = otherArgs[i];
			if (!(object instanceof Instance)) {
				throw new EmfRuntimeException("Expected Instance for argument [" + i + "] but found "
						+ (object == null ? null : object.getClass()));
			}
		}
	}
}
