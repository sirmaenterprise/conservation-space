package com.sirma.itt.emf.instance.dao;

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
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * InstanceEventProvider provider implementation that returns no events.
 * 
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public class EmptyInstanceEventProvider<I extends Instance> extends BaseInstanceEventProvider<I> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<I> createCreateEvent(I instance) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<I> createPersistedEvent(I instance, I old, String operationId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<I> createChangeEvent(I instance) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<I> createOpenEvent(I instance) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<I, ? extends AfterInstancePersistEvent<I, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			I instance) {
		return null;
	}

	@Override
	public InstanceAttachedEvent<I> createAttachEvent(I instance, Instance child) {
		return null;
	}

	@Override
	public InstanceDetachedEvent<I> createDetachEvent(I instance, Instance child) {
		return null;
	}

	@Override
	public BeforeInstanceDeleteEvent<I, ? extends AfterInstanceDeleteEvent<I, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			I instance) {
		return null;
	}

	@Override
	public BeforeInstanceCancelEvent<I, ? extends AfterInstanceCancelEvent<I, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			I instance) {
		return null;
	}

}
