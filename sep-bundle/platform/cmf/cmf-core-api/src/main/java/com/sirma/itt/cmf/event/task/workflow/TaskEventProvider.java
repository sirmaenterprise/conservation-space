package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
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
 * Event provider for workflow {@link TaskInstance}s.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
public class TaskEventProvider extends BaseInstanceEventProvider<TaskInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<TaskInstance> createCreateEvent(TaskInstance instance) {
		return new TaskCreateEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<TaskInstance> createPersistedEvent(TaskInstance instance,
			TaskInstance old, String operationId) {
		return new TaskPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<TaskInstance> createChangeEvent(TaskInstance instance) {
		return new TaskChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<TaskInstance> createOpenEvent(TaskInstance instance) {
		return new TaskOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<TaskInstance, ? extends AfterInstancePersistEvent<TaskInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			TaskInstance instance) {
		return new BeforeTaskPersistEvent(instance, instance.getParentTask());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<TaskInstance> createAttachEvent(TaskInstance instance,
			Instance child) {
		return new AttachedChildToTaskEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<TaskInstance> createDetachEvent(TaskInstance instance,
			Instance child) {
		return new DetachedChildToTaskEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<TaskInstance, ? extends AfterInstanceDeleteEvent<TaskInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			TaskInstance instance) {
		// tasks cannot be deleted only canceled
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<TaskInstance, ? extends AfterInstanceCancelEvent<TaskInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			TaskInstance instance) {
		return new BeforeTaskCancelEvent(instance);
	}
}