package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
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
 * Event provider for {@link CaseInstance}.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.CASE)
public class CaseEventProvider extends BaseInstanceEventProvider<CaseInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<CaseInstance> createCreateEvent(
			CaseInstance instance) {
		return new CaseCreateEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<CaseInstance> createPersistedEvent(CaseInstance instance,
			CaseInstance old, String operationId) {
		return new CasePersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<CaseInstance> createChangeEvent(CaseInstance instance) {
		return new CaseChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<CaseInstance> createOpenEvent(CaseInstance instance) {
		return new CaseOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<CaseInstance, ? extends AfterInstancePersistEvent<CaseInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			CaseInstance instance) {
		return new BeforeCasePersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<CaseInstance> createAttachEvent(CaseInstance instance,
			Instance child) {
		return new AttachedChildToCaseEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<CaseInstance> createDetachEvent(CaseInstance instance,
			Instance child) {
		return new DetachedChildToCaseEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<CaseInstance, ? extends AfterInstanceDeleteEvent<CaseInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			CaseInstance instance) {
		return new BeforeCaseDeleteEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<CaseInstance, ? extends AfterInstanceCancelEvent<CaseInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			CaseInstance instance) {
		return new BeforeCaseCancelEvent(instance);
	}
}