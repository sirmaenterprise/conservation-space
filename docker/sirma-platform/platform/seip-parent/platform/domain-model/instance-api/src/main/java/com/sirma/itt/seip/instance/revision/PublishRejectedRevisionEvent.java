package com.sirma.itt.seip.instance.revision;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Fired to notify for new revision that will be published and will be with rejected state.
 *
 * @author BBonev
 */
@Documentation("Fired to notify for new revision that will be published and will be with rejected state.")
public class PublishRejectedRevisionEvent extends AbstractInstanceEvent<Instance> {

	/** The revision. */
	private final Instance revision;

	/**
	 * Instantiates a new publish rejected revision event.
	 *
	 * @param instance
	 *            the instance
	 * @param revision
	 *            the revision
	 */
	public PublishRejectedRevisionEvent(Instance instance, Instance revision) {
		super(instance);
		this.revision = revision;
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	public Instance getRevision() {
		return revision;
	}

}