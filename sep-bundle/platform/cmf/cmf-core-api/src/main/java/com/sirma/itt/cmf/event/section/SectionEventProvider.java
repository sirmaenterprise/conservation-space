package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
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
 * Event provider for {@link SectionInstance}
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.SECTION)
public class SectionEventProvider extends BaseInstanceEventProvider<SectionInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<SectionInstance> createCreateEvent(SectionInstance instance) {
		return new SectionCreateEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<SectionInstance> createPersistedEvent(
			SectionInstance instance, SectionInstance old, String operationId) {
		return new SectionPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<SectionInstance> createChangeEvent(SectionInstance instance) {
		return new SectionChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<SectionInstance> createOpenEvent(SectionInstance instance) {
		return new SectionOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<SectionInstance, ? extends AfterInstancePersistEvent<SectionInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			SectionInstance instance) {
		return new BeforeSectionPersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<SectionInstance> createAttachEvent(SectionInstance instance,
			Instance child) {
		return new AttachedChildToSectionEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<SectionInstance> createDetachEvent(SectionInstance instance,
			Instance child) {
		return new DetachedChildToSectionEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<SectionInstance, ? extends AfterInstanceDeleteEvent<SectionInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			SectionInstance instance) {
		return new BeforeSectionDeleteEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<SectionInstance, ? extends AfterInstanceCancelEvent<SectionInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			SectionInstance instance) {
		// sections cannot be canceled
		return null;
	}
}