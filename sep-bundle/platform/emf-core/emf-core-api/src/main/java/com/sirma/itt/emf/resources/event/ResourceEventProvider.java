package com.sirma.itt.emf.resources.event;

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
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Resource event provider.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.RESOURCE)
public class ResourceEventProvider extends BaseInstanceEventProvider<Resource> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<Resource> createCreateEvent(Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<Resource> createPersistedEvent(Resource instance, Resource old, String operationId) {
		return new ResourcePersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<Resource> createChangeEvent(Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<Resource> createOpenEvent(Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<Resource, ? extends AfterInstancePersistEvent<Resource, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<Resource> createAttachEvent(Resource instance, Instance child) {
		return new AttachedChildToResourceEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<Resource> createDetachEvent(Resource instance, Instance child) {
		return new DetachedChildToResourceEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<Resource, ? extends AfterInstanceDeleteEvent<Resource, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<Resource, ? extends AfterInstanceCancelEvent<Resource, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			Resource instance) {
		// TODO Auto-generated method stub
		return null;
	}
}