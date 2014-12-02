package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
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
 * Event provider for {@link DocumentInstance}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.DOCUMENT)
public class DocumentEventProvider extends BaseInstanceEventProvider<DocumentInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<DocumentInstance> createCreateEvent(DocumentInstance instance) {
		return new DocumentCreateEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<DocumentInstance> createPersistedEvent(
			DocumentInstance instance, DocumentInstance old, String operationId) {
		return new DocumentPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<DocumentInstance> createChangeEvent(DocumentInstance instance) {
		return new DocumentChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<DocumentInstance> createOpenEvent(DocumentInstance instance) {
		return new DocumentOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<DocumentInstance, ? extends AfterInstancePersistEvent<DocumentInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			DocumentInstance instance) {
		return new BeforeDocumentPersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<DocumentInstance> createAttachEvent(DocumentInstance instance,
			Instance child) {
		return new AttachedChildToDocumentEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<DocumentInstance> createDetachEvent(DocumentInstance instance,
			Instance child) {
		return new DetachedChildToDocumentEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<DocumentInstance, ? extends AfterInstanceDeleteEvent<DocumentInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			DocumentInstance instance) {
		return new BeforeDocumentDeleteEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<DocumentInstance, ? extends AfterInstanceCancelEvent<DocumentInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			DocumentInstance instance) {
		// documents cannot be canceled
		return null;
	}
}