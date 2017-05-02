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

	/** The new revision. */
	private final Instance newRevision;
	/** The previous revision. */
	private final Instance previousRevision;
	/** The latest revision instance. */
	private final Instance latestRevisionInstance;

	/**
	 * Instantiates a new publish approved revision event.
	 *
	 * @param instance
	 *            the instance
	 * @param newRevision
	 *            the new revision
	 * @param previousRevision
	 *            the previous revision
	 * @param latestRevisionInstance
	 *            the latest revision instance
	 */
	public PublishApprovedRevisionEvent(Instance instance, Instance newRevision, Instance previousRevision,
			Instance latestRevisionInstance) {
		super(instance);
		this.newRevision = newRevision;
		this.previousRevision = previousRevision;
		this.latestRevisionInstance = latestRevisionInstance;
	}

	/**
	 * Getter method for newRevision.
	 *
	 * @return the newRevision
	 */
	public Instance getNewRevision() {
		return newRevision;
	}

	/**
	 * Getter method for previousRevision.
	 *
	 * @return the previousRevision
	 */
	public Instance getPreviousRevision() {
		return previousRevision;
	}

	/**
	 * Getter method for latestRevisionInstance.
	 *
	 * @return the latestRevisionInstance
	 */
	public Instance getLatestRevisionInstance() {
		return latestRevisionInstance;
	}

}
