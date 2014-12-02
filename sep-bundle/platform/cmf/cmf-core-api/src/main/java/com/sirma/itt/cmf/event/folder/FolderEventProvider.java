package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
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
 * Event provider for {@link FolderInstance}
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.FOLDER)
public class FolderEventProvider extends BaseInstanceEventProvider<FolderInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<FolderInstance> createCreateEvent(FolderInstance instance) {
		return new FolderCreateEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<FolderInstance> createPersistedEvent(FolderInstance instance,
			FolderInstance old, String operationId) {
		return new FolderPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<FolderInstance> createChangeEvent(FolderInstance instance) {
		return new FolderChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<FolderInstance> createOpenEvent(FolderInstance instance) {
		return new FolderOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<FolderInstance, ? extends AfterInstancePersistEvent<FolderInstance, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			FolderInstance instance) {
		return new BeforeFolderPersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<FolderInstance> createAttachEvent(FolderInstance instance,
			Instance child) {
		return new AttachedChildToFolderEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<FolderInstance> createDetachEvent(FolderInstance instance,
			Instance child) {
		return new DetachedChildToFolderEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<FolderInstance, ? extends AfterInstanceDeleteEvent<FolderInstance, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			FolderInstance instance) {
		return new BeforeFolderDeleteEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<FolderInstance, ? extends AfterInstanceCancelEvent<FolderInstance, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			FolderInstance instance) {
		// sections cannot be canceled
		return null;
	}
}