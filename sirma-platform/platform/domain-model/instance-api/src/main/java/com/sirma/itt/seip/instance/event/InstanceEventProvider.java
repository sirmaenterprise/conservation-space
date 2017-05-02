package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventProvider;
import com.sirma.itt.seip.event.EventType;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Provider class for different life cycle event objects based on a concrete {@link Instance} type.
 *
 * @author BBonev
 * @param <I>
 *            the concrete {@link Instance} type
 */
public interface InstanceEventProvider<I extends Instance> extends EventProvider<I> {
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

	/**
	 * Creates the before instance publish event.
	 *
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 * @param revision
	 *            the revision
	 * @return an event instance that to be fired to notify that an instance will be published.
	 */
	BeforeInstancePublishEvent<I, ? extends AfterInstancePublishEvent<I, TwoPhaseEvent>> createBeforeInstancePublishEvent(
			I instance, String operationId, Instance revision);

	/**
	 * Event provider implementation that does nothing and always returns <code>null</code> events
	 *
	 * @author BBonev
	 */
	class NoOpInstanceEventProvider implements InstanceEventProvider<Instance> {

		private static final NoOpInstanceEventProvider INSTANCE = new NoOpInstanceEventProvider();

		private NoOpInstanceEventProvider() {
			// nothing to do
		}

		/**
		 * Singleton instance of the {@link NoOpInstanceEventProvider}
		 *
		 * @param <I>
		 *            the generic type
		 * @return the instance event provider
		 */
		@SuppressWarnings("unchecked")
		public static <I extends Instance> InstanceEventProvider<I> instance() {
			return (InstanceEventProvider<I>) INSTANCE;
		}

		@Override
		public EmfEvent createEvent(EventType type, Instance instance, String operationId, Object... otherArgs) {
			return null;
		}

		@Override
		public InstanceCreateEvent<Instance> createCreateEvent(Instance instance) {
			return null;
		}

		@Override
		public InstancePersistedEvent<Instance> createPersistedEvent(Instance instance, Instance oldVersion,
				String operationId) {
			return null;
		}

		@Override
		public InstanceChangeEvent<Instance> createChangeEvent(Instance instance) {
			return null;
		}

		@Override
		public InstanceOpenEvent<Instance> createOpenEvent(Instance instance) {
			return null;
		}

		@Override
		public BeforeInstancePersistEvent<Instance, ? extends AfterInstancePersistEvent<Instance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
				Instance instance) {
			return null;
		}

		@Override
		public BeforeInstanceDeleteEvent<Instance, ? extends AfterInstanceDeleteEvent<Instance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
				Instance instance) {
			return null;
		}

		@Override
		public BeforeInstanceCancelEvent<Instance, ? extends AfterInstanceCancelEvent<Instance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
				Instance instance) {
			return null;
		}

		@Override
		public InstanceAttachedEvent<Instance> createAttachEvent(Instance instance, Instance child) {
			return null;
		}

		@Override
		public InstanceDetachedEvent<Instance> createDetachEvent(Instance instance, Instance child) {
			return null;
		}

		@Override
		public BeforeInstancePublishEvent<Instance, ? extends AfterInstancePublishEvent<Instance, TwoPhaseEvent>> createBeforeInstancePublishEvent(
				Instance instance, String operationId, Instance revision) {
			return null;
		}
	}
}
