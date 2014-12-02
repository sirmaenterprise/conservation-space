package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
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
import com.sirma.itt.emf.instance.dao.BaseInstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Event provider for {@link StandaloneTaskInstance}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.STANDALONE_TASK)
public class StandaloneTaskEventProvider extends BaseInstanceEventProvider<StandaloneTaskInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<StandaloneTaskInstance> createCreateEvent(
			StandaloneTaskInstance instance) {
		return new StandaloneTaskCreateEvent(instance, instance.getOwningInstance());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<StandaloneTaskInstance> createPersistedEvent(
			StandaloneTaskInstance instance, StandaloneTaskInstance old, String operationId) {
		return new StandaloneTaskPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<StandaloneTaskInstance> createChangeEvent(
			StandaloneTaskInstance instance) {
		return new StandaloneTaskChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<StandaloneTaskInstance> createOpenEvent(
			StandaloneTaskInstance instance) {
		return new StandaloneTaskOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<StandaloneTaskInstance, ? extends AfterInstancePersistEvent<StandaloneTaskInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			StandaloneTaskInstance instance) {
		return new BeforeStandaloneTaskPersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<StandaloneTaskInstance> createAttachEvent(
			StandaloneTaskInstance instance, Instance child) {
		return new AttachedChildToStandaloneTaskEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<StandaloneTaskInstance> createDetachEvent(
			StandaloneTaskInstance instance, Instance child) {
		return new DetachedChildToStandaloneTaskEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<StandaloneTaskInstance, ? extends AfterInstanceDeleteEvent<StandaloneTaskInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			StandaloneTaskInstance instance) {
		return new BeforeStandaloneTaskDeleteEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<StandaloneTaskInstance, ? extends AfterInstanceCancelEvent<StandaloneTaskInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			StandaloneTaskInstance instance) {
		return new BeforeStandaloneTaskCancelEvent(instance);
	}
}