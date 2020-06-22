package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;

/**
 * Event fired on link add operation. System event.
 *
 * @author BBonev
 */
public class LinkAddedEvent implements ObjectPropertyAddEvent {

	private final LinkReference addedLink;

	/**
	 * Instantiates a new link added event.
	 *
	 * @param addedLink
	 *            the added link
	 */
	public LinkAddedEvent(LinkReference addedLink) {
		this.addedLink = addedLink;
	}

	/**
	 * Gets the added link.
	 *
	 * @return the added link
	 */
	public LinkReference getAddedLink() {
		return addedLink;
	}

	@Override
	public Serializable getSourceId() {
		return addedLink.getFrom().getId();
	}

	@Override
	public String getObjectPropertyName() {
		return addedLink.getIdentifier();
	}

	@Override
	public Serializable getTargetId() {
		return addedLink.getTo().getId();
	}

}
