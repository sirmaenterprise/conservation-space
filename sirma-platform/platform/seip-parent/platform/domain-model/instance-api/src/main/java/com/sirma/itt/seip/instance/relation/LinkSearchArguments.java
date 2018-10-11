package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.Link;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;

/**
 * Search parameters for loading link references.
 *
 * @author BBonev
 */
public class LinkSearchArguments extends SearchArguments<LinkInstance>
		implements Link<InstanceReference, InstanceReference> {

	/** The from. */
	private InstanceReference from;

	/** The to. */
	private InstanceReference to;

	/** The link id. */
	private String linkId;
	
	/** Shows that the search is for context or subject */
	private boolean isFrom;

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	@Override
	public InstanceReference getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(InstanceReference from) {
		this.from = from;
	}

	/**
	 * Getter method for to.
	 *
	 * @return the to
	 */
	@Override
	public InstanceReference getTo() {
		return to;
	}

	/**
	 * Setter method for to.
	 *
	 * @param to
	 *            the to to set
	 */
	public void setTo(InstanceReference to) {
		this.to = to;
	}

	/**
	 * Getter method for linkId.
	 *
	 * @return the linkId
	 */
	public String getLinkId() {
		return linkId;
	}

	/**
	 * Setter method for linkId.
	 *
	 * @param linkId
	 *            the linkId to set
	 */
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	/**
	 * @return the isFrom
	 */
	public boolean isFrom() {
		return isFrom;
	}

	/**
	 * @param isFrom the isFrom to set
	 */
	public void setIsFrom(boolean isFrom) {
		this.isFrom = isFrom;
	}

}
