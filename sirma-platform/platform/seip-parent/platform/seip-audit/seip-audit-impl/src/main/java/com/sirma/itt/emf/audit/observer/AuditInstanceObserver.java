package com.sirma.itt.emf.audit.observer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.processor.AuditProcessor;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.content.event.CheckInEvent;
import com.sirma.itt.seip.instance.content.event.CheckOutEvent;
import com.sirma.itt.seip.instance.event.AfterInstanceMoveEvent;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.lock.AfterLockEvent;
import com.sirma.itt.seip.instance.lock.BeforeUnlockEvent;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Logs instance-related events in the audit log.
 *
 * @author nvelkov
 */
@Auditable
@Singleton
public class AuditInstanceObserver {

	@Inject
	private AuditProcessor auditProcessor;

	/**
	 * Observes any fired {@link AuditableEvent}.
	 *
	 * @param event
	 *            the event
	 */
	public void onAuditableEvent(@Observes AuditableEvent event) {
		// do not log events that do not carry operation id
		if (event.getOperationId() != null) {
			auditProcessor.process(event.getInstance(), event.getOperationId(), event);
		}
	}

	/**
	 * Observes any fired {@link AfterInstanceMoveEvent}.
	 *
	 * @param event
	 *            the event
	 */
	public void onAfterInstanceMoveEvent(@Observes AfterInstanceMoveEvent event) {

		Instance source = event.getSourceInstance();
		Instance target = event.getTargetInstance();
		String operationId = ActionTypeConstants.MOVE;

		// If the old or new context is section we want for audit log the case
		// where this section is placed
		source = getParentInstance(source);
		target = getParentInstance(target);
		String context = "";
		if (source != null) {
			context = source.getId() + ";";
		}
		context += target.getId();

		AuditablePayload payload = new AuditablePayload().setInstance(event.getInstance()).setOperationId(operationId)
				.setTriggeredBy(event).setShowParentPath(false).setTargetProperties(target.getId().toString())
				.setRelationStatus(AuditActivity.STATUS_ADDED);

		auditProcessor.process(payload, context);
	}

	/**
	 * Retrieves the parent instance of the provided instance if it is a SectionInstance.
	 *
	 * @param instance
	 *            - the provided instance
	 * @return the parent instance if it is a SectionInstance or the current if it is not
	 */
	private static Instance getParentInstance(Instance instance) {
		return instance;
	}

	/**
	 * Called when an instance is attached.
	 *
	 * @param instanceAttachedEvent
	 *            the instance attached event
	 */
	public void onInstanceAttachedEvent(@Observes InstanceAttachedEvent<?> instanceAttachedEvent) {
		AuditablePayload payload = new AuditablePayload(instanceAttachedEvent.getChild(),
				instanceAttachedEvent.getOperationId(), instanceAttachedEvent, true);
		payload.setExtraContext(instanceAttachedEvent.getInstance());
		auditProcessor.process(payload);
	}

	/**
	 * Observes changes to EMF instances.
	 *
	 * @param event
	 *            the event holding the changed EMF instance
	 */
	public void onInstanceChange(@Observes PropertiesChangeEvent event) {
		if (auditProcessor.isInstanceApplicable(event.getInstance())) {
			auditProcessor.process(event.getInstance(), Operation.getUserOperationId(event.getOperation()), event);
		}
	}

	/**
	 * Listens for instance lock events to update audit log
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceLocked(@Observes AfterLockEvent event) {
		Instance lockedInstance = event.getLockInfo().getLockedInstance().toInstance();
		if (auditProcessor.isInstanceApplicable(lockedInstance)) {
			auditProcessor.process(lockedInstance, ActionTypeConstants.LOCK, event);
		}
	}

	/**
	 * Listens for instance unlock events to update audit log
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceUnlocked(@Observes BeforeUnlockEvent event) {
		Instance unlockedInstance = event.getLockInfo().getLockedInstance().toInstance();
		if (auditProcessor.isInstanceApplicable(unlockedInstance)) {
			auditProcessor.process(unlockedInstance, ActionTypeConstants.UNLOCK, event);
		}
	}

	/**
	 * Listens for instance checkOut event and log it in the audit log.
	 *
	 * @param event
	 *            the checkout event
	 */
	public void onDocumentCheckOut(@Observes CheckOutEvent event) {
		Instance checkedOutInstance = event.getInstance();
		if (auditProcessor.isInstanceApplicable(checkedOutInstance)) {
			auditProcessor.process(checkedOutInstance, ActionTypeConstants.EDIT_OFFLINE, event);
		}
	}

	/**
	 * Listens for instance CheckIn event and log it in the audit log.
	 *
	 * @param event
	 *            the checkin event
	 */
	public void onDocumentCheckIn(@Observes CheckInEvent event) {
		Instance checkedInInstance = (Instance) event.getOwner();
		if (auditProcessor.isInstanceApplicable(checkedInInstance)) {
			auditProcessor.process(checkedInInstance, ActionTypeConstants.UPLOAD_NEW_VERSION, event);
		}
	}

}
