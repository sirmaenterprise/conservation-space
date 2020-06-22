package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;

/**
 * Event fired on link remove operation. System event.
 *
 * @author BBonev
 */
public class LinkRemovedEvent implements ObjectPropertyRemoveEvent {

	private final LinkReference removedLink;

	/**
	 * Instantiates a new link removed event.
	 *
	 * @param removedLink
	 *            the removed link
	 */
	public LinkRemovedEvent(LinkReference removedLink) {
		this.removedLink = removedLink;
	}

	/**
	 * Gets the removed link.
	 *
	 * @return the removed link
	 */
	public LinkReference getRemovedLink() {
		return removedLink;
	}

	@Override
	public Serializable getSourceId() {
		return removedLink.getFrom().getId();
	}

	@Override
	public String getObjectPropertyName() {
		return removedLink.getIdentifier();
	}

	@Override
	public Serializable getTargetId() {
		return removedLink.getTo().getId();
	}
}
