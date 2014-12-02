package com.sirma.itt.pm.event;

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
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Provides events for {@link ProjectInstance}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesPm.PROJECT)
public class ProjectInstanceEventProvider extends BaseInstanceEventProvider<ProjectInstance> {
	@Override
	public InstanceCreateEvent<ProjectInstance> createCreateEvent(ProjectInstance instance) {
		return new ProjectCreateEvent(instance);
	}

	@Override
	public InstancePersistedEvent<ProjectInstance> createPersistedEvent(
			ProjectInstance instance, ProjectInstance old, String operationId) {
		return new ProjectPersistedEvent(instance, old, operationId);
	}

	@Override
	public InstanceChangeEvent<ProjectInstance> createChangeEvent(ProjectInstance instance) {
		return new ProjectChangeEvent(instance);
	}

	@Override
	public InstanceOpenEvent<ProjectInstance> createOpenEvent(ProjectInstance instance) {
		return new ProjectOpenEvent(instance);
	}

	@Override
	public BeforeInstancePersistEvent<ProjectInstance, ? extends AfterInstancePersistEvent<ProjectInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			ProjectInstance instance) {
		return new BeforeProjectPersistEvent(instance);
	}

	@Override
	public InstanceAttachedEvent<ProjectInstance> createAttachEvent(ProjectInstance instance,
			Instance child) {
		return new AttachedChildToProjectEvent(instance, child);
	}

	@Override
	public InstanceDetachedEvent<ProjectInstance> createDetachEvent(ProjectInstance instance,
			Instance child) {
		return new DetachedChildToProjectEvent(instance, child);
	}

	@Override
	public BeforeInstanceDeleteEvent<ProjectInstance, ? extends AfterInstanceDeleteEvent<ProjectInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			ProjectInstance instance) {
		return new BeforeProjectDeleteEvent(instance);
	}

	@Override
	public BeforeInstanceCancelEvent<ProjectInstance, ? extends AfterInstanceCancelEvent<ProjectInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			ProjectInstance instance) {
		return new BeforeProjectCancelEvent(instance);
	}
}