package com.sirma.itt.emf.instance.dao;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.event.instance.InstanceEventType;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Provider class for different life cycle event objects based on a concrete {@link Instance} type.
 * 
 * @author BBonev
 * @param <I>
 *            the concrete {@link Instance} type
 */
public interface InstanceEventProvider<I extends Instance> {

	/**
	 * Creates the event of the given type and target instance. If the event requires other
	 * arguments they could be passed in the varargs.
	 * 
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 * @param otherArgs
	 *            the other args
	 * @return the emf event
	 */
	EmfEvent createEvent(InstanceEventType type, I instance, Object... otherArgs);

	/**
	 * Creates the create event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	InstanceCreateEvent<I> createCreateEvent(I instance);

	/**
	 * Creates the persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param oldVersion
	 *            the old version
	 * @param operationId
	 *            the operation that triggered the given persisted event
	 * @return the created event if supported or <code>null</code> if not.
	 */
	InstancePersistedEvent<I> createPersistedEvent(I instance, I oldVersion, String operationId);

	/**
	 * Creates the change event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	InstanceChangeEvent<I> createChangeEvent(I instance);

	/**
	 * Creates the open event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	InstanceOpenEvent<I> createOpenEvent(I instance);

	/**
	 * Creates the before instance persist event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	BeforeInstancePersistEvent<I, ? extends AfterInstancePersistEvent<I, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			I instance);

	/**
	 * Creates the before instance delete event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	BeforeInstanceDeleteEvent<I, ? extends AfterInstanceDeleteEvent<I, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			I instance);

	/**
	 * Creates the before instance cancel event.
	 * 
	 * @param instance
	 *            the instance
	 * @return the created event if supported or <code>null</code> if not.
	 */
	BeforeInstanceCancelEvent<I, ? extends AfterInstanceCancelEvent<I, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			I instance);

	/**
	 * Creates an attach event for the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param child
	 *            the child
	 * @return the instance attached event
	 */
	InstanceAttachedEvent<I> createAttachEvent(I instance, Instance child);

	/**
	 * Creates a detach event for the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param child
	 *            the child
	 * @return the instance detached event
	 */
	InstanceDetachedEvent<I> createDetachEvent(I instance, Instance child);
}
