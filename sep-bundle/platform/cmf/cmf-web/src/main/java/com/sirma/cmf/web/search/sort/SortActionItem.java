package com.sirma.cmf.web.search.sort;

import com.sirma.cmf.web.search.SortAction;

/**
 * SortActionItem data holder.
 * 
 * @author svelikov
 */
public class SortActionItem implements SortAction {

	/** The type. */
	private final String type;

	/** The label. */
	private final String label;

	private final String description;

	/**
	 * Instantiates a new sort action item.
	 * 
	 * @param actionType
	 *            the action type
	 * @param actionLabel
	 *            the action label
	 */
	public SortActionItem(String actionType, String actionLabel) {
		this.type = actionType;
		this.label = actionLabel;
		this.description = actionLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Getter method for description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
