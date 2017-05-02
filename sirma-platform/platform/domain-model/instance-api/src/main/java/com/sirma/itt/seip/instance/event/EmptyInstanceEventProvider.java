package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.dao.BaseInstanceEventProvider;

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
