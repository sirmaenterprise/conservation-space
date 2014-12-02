package com.sirma.itt.emf.instance.event;

import com.sirma.itt.emf.domain.ObjectTypes;
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
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Event provider for {@link CommonInstance}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.INSTANCE)
public class CommonInstanceEventProvider extends BaseInstanceEventProvider<CommonInstance> {
	@Override
	public InstanceCreateEvent<CommonInstance> createCreateEvent(CommonInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InstancePersistedEvent<CommonInstance> createPersistedEvent(CommonInstance instance,
			CommonInstance oldVersion, String operationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InstanceChangeEvent<CommonInstance> createChangeEvent(CommonInstance instance) {
		return new CommonInstanceChangeEvent(instance);
	}

	@Override
	public InstanceOpenEvent<CommonInstance> createOpenEvent(CommonInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BeforeInstancePersistEvent<CommonInstance, ? extends AfterInstancePersistEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			CommonInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InstanceAttachedEvent<CommonInstance> createAttachEvent(CommonInstance instance,
			Instance child) {
		// cannot be attached
		return null;
	}

	@Override
	public InstanceDetachedEvent<CommonInstance> createDetachEvent(CommonInstance instance,
			Instance child) {
		// cannot be attached
		return null;
	}

	@Override
	public BeforeInstanceDeleteEvent<CommonInstance, ? extends AfterInstanceDeleteEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			CommonInstance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BeforeInstanceCancelEvent<CommonInstance, ? extends AfterInstanceCancelEvent<CommonInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			CommonInstance instance) {
		// cannot be canceled
		return null;
	}
}