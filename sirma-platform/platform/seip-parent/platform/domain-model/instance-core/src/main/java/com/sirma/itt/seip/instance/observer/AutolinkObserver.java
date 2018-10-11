package com.sirma.itt.seip.instance.observer;

import javax.enterprise.event.Observes;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Observer that populates links related to instance move/attach/detach
 *
 * @author BBonev
 */
@ApplicationScoped
public class AutolinkObserver {

	@Inject
	private InstancePropertyNameResolver nameResolver;
	/**
	 * When instance is detached from another instance additional unlink/link is applied if event is of interest.
	 * 
	 * @param event
	 *            the event
	 */
	public void onAfterInstanceMoved(
			@Observes(during = TransactionPhase.BEFORE_COMPLETION) ParentChangedEvent event) {
		unlinkInternal(event.getOldParent(), event.getInstance());

	}

	/**
	 * When instance is detached from another instance additional unlink is applied if event is of interest.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstanceDetachedEvent(
			@Observes(during = TransactionPhase.BEFORE_COMPLETION) InstanceDetachedEvent<? extends Instance> event) {
		unlinkInternal(event.getInstance(), event.getChild());
	}

	/**
	 * When instance is attached to another instance additional link is applied if event is of interest.
	 * 
	 * @param event
	 *            the event is the source event
	 */
	public void onInstanceAttachedEvent(
			@Observes(during = TransactionPhase.BEFORE_COMPLETION) InstanceAttachedEvent<? extends Instance> event) {
		if (Operation.isUserOperationAs(event.getOperation(), ActionTypeConstants.ATTACH_DOCUMENT,
				ActionTypeConstants.ATTACH_OBJECT, ActionTypeConstants.ADD_LIBRARY)) {
			linkInternal(event.getInstance(), event.getChild());
		}
	}

	private void linkInternal(Instance parent, Instance child) {
		if (parent != null && child != null) {
			parent.append(LinkConstants.HAS_ATTACHMENT, child.getId(), nameResolver);
			child.add(LinkConstants.IS_ATTACHED_TO, parent.getId(), nameResolver);
		}
	}

	private void unlinkInternal(Instance parent, Instance child) {
		if (parent != null && child != null) {
			child.remove(LinkConstants.IS_ATTACHED_TO, nameResolver);
			parent.remove(LinkConstants.HAS_ATTACHMENT, child.getId(), nameResolver);
		}
	}
}
