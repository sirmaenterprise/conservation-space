package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
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
import com.sirma.itt.seip.resources.Resource;

/**
 * Resource event provider.
 *
 * @author BBonev
 */
@InstanceType(type = "resource")
public class ResourceEventProvider extends BaseInstanceEventProvider<Resource> {

	@Override
	public InstanceCreateEvent<Resource> createCreateEvent(Resource instance) {
		// Not used method
		return null;
	}

	@Override
	public InstancePersistedEvent<Resource> createPersistedEvent(Resource instance, Resource old, String operationId) {
		return new ResourcePersistedEvent(instance, old, operationId);
	}

	@Override
	public InstanceChangeEvent<Resource> createChangeEvent(Resource instance) {
		// Not used method
		return null;
	}

	@Override
	public InstanceOpenEvent<Resource> createOpenEvent(Resource instance) {
		// Not used method
		return null;
	}

	@Override
	public BeforeInstancePersistEvent<Resource, ? extends AfterInstancePersistEvent<Resource, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			Resource instance) {
		// Not used method
		return null;
	}

	@Override
	public InstanceAttachedEvent<Resource> createAttachEvent(Resource instance, Instance child) {
		return new AttachedChildToResourceEvent(instance, child);
	}

	@Override
	public InstanceDetachedEvent<Resource> createDetachEvent(Resource instance, Instance child) {
		return new DetachedChildToResourceEvent(instance, child);
	}

	@Override
	public BeforeInstanceDeleteEvent<Resource, ? extends AfterInstanceDeleteEvent<Resource, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			Resource instance) {
		return new BeforeResourceDeleteEvent(instance);
	}

	@Override
	public BeforeInstanceCancelEvent<Resource, ? extends AfterInstanceCancelEvent<Resource, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			Resource instance) {
		// Not used method
		return null;
	}
}