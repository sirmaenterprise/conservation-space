package com.sirma.itt.seip.instance.revision;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Fires event to indicate new created revision for instance.
 *
 * @author BBonev
 */
@Documentation("Fires event to indicate new created revision for instance.")
public class CreatedRevisionEvent extends AbstractInstanceEvent<Instance> {

	/** The revision. */
	private final Instance revision;

	/**
	 * Instantiates a new created revision event.
	 *
	 * @param instance
	 *            the instance
	 * @param revision
	 *            the revision
	 */
	public CreatedRevisionEvent(Instance instance, Instance revision) {
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
