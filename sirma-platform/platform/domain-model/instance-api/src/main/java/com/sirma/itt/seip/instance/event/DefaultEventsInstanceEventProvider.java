package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.dao.BaseInstanceEventProvider;

/**
 * Default implementation of the {@link com.sirma.itt.seip.instance.event.InstanceEventProvider} that returns the generic
 * events for any instance passed to the methods.
 *
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public class DefaultEventsInstanceEventProvider<I extends Instance> extends BaseInstanceEventProvider<I> {

	@Override
	public InstanceCreateEvent<I> createCreateEvent(I instance) {
		return new InstanceCreateEvent<>(instance);
	}

	@Override
	public InstancePersistedEvent<I> createPersistedEvent(I instance, I old, String operationId) {
		return new InstancePersistedEvent<>(instance, old, operationId);
	}

	@Override
	public InstanceChangeEvent<I> createChangeEvent(I instance) {
		return new InstanceChangeEvent<>(instance);
	}

	@Override
	public InstanceOpenEvent<I> createOpenEvent(I instance) {
		return new InstanceOpenEvent<>(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<I, ? extends AfterInstancePersistEvent<I, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			I instance) {
		return new BeforeInstancePersistEvent<>(instance);
	}

	@Override
	public InstanceAttachedEvent<I> createAttachEvent(I instance, Instance child) {
		return new InstanceAttachedEvent<>(instance, child);
	}

	@Override
	public InstanceDetachedEvent<I> createDetachEvent(I instance, Instance child) {
		return new InstanceDetachedEvent<>(instance, child);
	}

	@Override
	public BeforeInstanceDeleteEvent<I, ? extends AfterInstanceDeleteEvent<I, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			I instance) {
		return new BeforeInstanceDeleteEvent<>(instance);
	}

	@Override
	public BeforeInstanceCancelEvent<I, ? extends AfterInstanceCancelEvent<I, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			I instance) {
		return new BeforeInstanceCancelEvent<>(instance);
	}

	@Override
	public BeforeInstancePublishEvent<I, ? extends AfterInstancePublishEvent<I, TwoPhaseEvent>> createBeforeInstancePublishEvent(
			I instance, String operationId, Instance revision) {
		return new BeforeInstancePublishEvent<>(instance, operationId, revision);
	}

}
