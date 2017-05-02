package com.sirma.itt.emf.instance.event;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.CommonInstance;
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
 * Event provider for {@link CommonInstance}.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.INSTANCE)
public class CommonInstanceEventProvider extends BaseInstanceEventProvider<CommonInstance> {
	@Override
	public InstanceCreateEvent<CommonInstance> createCreateEvent(CommonInstance instance) {
		return null;
	}

	@Override
	public InstancePersistedEvent<CommonInstance> createPersistedEvent(CommonInstance instance,
			CommonInstance oldVersion, String operationId) {
		return null;
	}

	@Override
	public InstanceChangeEvent<CommonInstance> createChangeEvent(CommonInstance instance) {
		return new CommonInstanceChangeEvent(instance);
	}

	@Override
	public InstanceOpenEvent<CommonInstance> createOpenEvent(CommonInstance instance) {
		return null;
	}

	@Override
	public BeforeInstancePersistEvent<CommonInstance, ? extends AfterInstancePersistEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			CommonInstance instance) {
		return null;
	}

	@Override
	public InstanceAttachedEvent<CommonInstance> createAttachEvent(CommonInstance instance, Instance child) {
		// cannot be attached
		return null;
	}

	@Override
	public InstanceDetachedEvent<CommonInstance> createDetachEvent(CommonInstance instance, Instance child) {
		// cannot be attached
		return null;
	}

	@Override
	public BeforeInstanceDeleteEvent<CommonInstance, ? extends AfterInstanceDeleteEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			CommonInstance instance) {
		// Not used method
		return null;
	}

	@Override
	public BeforeInstanceCancelEvent<CommonInstance, ? extends AfterInstanceCancelEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			CommonInstance instance) {
		// cannot be canceled
		return null;
	}
}