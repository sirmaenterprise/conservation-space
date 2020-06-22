package com.sirma.itt.seip.instance.revision;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Fired to notify for new revision that will be published and will be with approved state.
 *
 * @author BBonev
 */
@Documentation("Fired to notify for new revision that will be published and will be with approved state.")
public class PublishApprovedRevisionEvent extends AbstractInstanceEvent<Instance> {

	private final Instance newRevision;

	private final Instance previousRevision;

	/**
	 * Instantiates a new publish approved revision event.
	 *
	 * @param instance the instance
	 * @param newRevision the new revision
	 * @param previousRevision the previous revision
	 */
	public PublishApprovedRevisionEvent(Instance instance, Instance newRevision, Instance previousRevision) {
		super(instance);
		this.newRevision = newRevision;
		this.previousRevision = previousRevision;
	}

	public Instance getNewRevision() {
		return newRevision;
	}

	public Instance getPreviousRevision() {
		return previousRevision;
	}
}
