package com.sirma.itt.objects.event;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.BaseInstanceEventProvider;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.event.AfterInstanceCancelEvent;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.event.BeforeInstanceCancelEvent;
import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.itt.seip.instance.event.InstanceCreateEvent;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;

/**
 * Event provider for {@link ObjectInstance}.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.OBJECT)
public class ObjectEventProvider extends BaseInstanceEventProvider<ObjectInstance> {
	@Override
	public InstancePersistedEvent<ObjectInstance> createPersistedEvent(ObjectInstance instance, ObjectInstance old,
			String operationId) {
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
	public InstanceAttachedEvent<ObjectInstance> createAttachEvent(ObjectInstance instance, Instance child) {
		return new AttachedChildToObjectEvent(instance, child);
	}

	@Override
	public InstanceDetachedEvent<ObjectInstance> createDetachEvent(ObjectInstance instance, Instance child) {
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