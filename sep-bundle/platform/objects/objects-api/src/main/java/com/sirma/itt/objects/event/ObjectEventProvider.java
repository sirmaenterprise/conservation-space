package com.sirma.itt.objects.event;

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
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event provider for {@link ObjectInstance}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesObject.OBJECT)
public class ObjectEventProvider extends BaseInstanceEventProvider<ObjectInstance> {
	@Override
	public InstancePersistedEvent<ObjectInstance> createPersistedEvent(ObjectInstance instance,
			ObjectInstance old, String operationId) {
		return new ObjectPersistedEvent(instance, old, operationId);
	}

	@Override
	public InstanceOpenEvent<ObjectInstance> createOpenEvent(ObjectInstance instance) {
		return new ObjectOpenEvent(instance);
	}

	@Override
	public InstanceCreateEvent<ObjectInstance> createCreateEvent(ObjectInstance instance) {
		return new ObjectCreateEvent(instance);
	}

	@Override
	public InstanceChangeEvent<ObjectInstance> createChangeEvent(ObjectInstance instance) {
		return new ObjectChangeEvent(instance);
	}

	@Override
	public BeforeInstancePersistEvent<ObjectInstance, ? extends AfterInstancePersistEvent<ObjectInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			ObjectInstance instance) {
		return new BeforeObjectPersistEvent(instance);
	}

	@Override
	public InstanceAttachedEvent<ObjectInstance> createAttachEvent(ObjectInstance instance,
			Instance child) {
		return new AttachedChildToObjectEvent(instance, child);
	}

	@Override
	public InstanceDetachedEvent<ObjectInstance> createDetachEvent(ObjectInstance instance,
			Instance child) {
		return new DetachedChildToObjectEvent(instance, child);
	}

	@Override
	public BeforeInstanceDeleteEvent<ObjectInstance, ? extends AfterInstanceDeleteEvent<ObjectInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			ObjectInstance instance) {
		return new BeforeObjectDeleteEvent(instance);
	}

	@Override
	public BeforeInstanceCancelEvent<ObjectInstance, ? extends AfterInstanceCancelEvent<ObjectInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			ObjectInstance instance) {
		return null;
	}
}