package com.sirma.itt.seip.time;

import java.util.List;

/**
 * Configuration object used for creating dynamic date ranges based on {@link DateOffset}.
 *
 * @author Mihail Radkov
 */
public class DateRangeConfig implements Comparable<DateRangeConfig> {

	private String id;

	private int order;

	private DateOffset startOffset;

	private DateOffset endOffset;

	private List<String> includedRangesIds;

	private String label;

	private String labelId;

	@Override
	public int compareTo(DateRangeConfig config) {
		return Integer.compare(order, config.getOrder());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (endOffset == null ? 0 : endOffset.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (includedRangesIds == null ? 0 : includedRangesIds.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (labelId == null ? 0 : labelId.hashCode());
		result = prime * result + order;
		result = prime * result + (startOffset == null ? 0 : startOffset.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DateRangeConfig)) {
			return false;
		}
		DateRangeConfig config = (DateRangeConfig) obj;
		if (config.getId() != id) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Getter method for order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Setter method for order.
	 *
	 * @param order
	 *            the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Getter method for startOffset.
	 *
	 * @return the startOffset
	 */
	public DateOffset getStartOffset() {
		return startOffset;
	}

	/**
	 * Setter method for startOffset.
	 *
	 * @param startOffset
	 *            the startOffset to set
	 */
	public void setStartOffset(DateOffset startOffset) {
		this.startOffset = startOffset;
	}

	/**
	 * Getter method for endOffset.
	 *
	 * @return the endOffset
	 */
	public DateOffset getEndOffset() {
		return endOffset;
	}

	/**
	 * Setter method for endOffset.
	 *
	 * @param endOffset
	 *            the endOffset to set
	 */
	public void setEndOffset(DateOffset endOffset) {
		this.endOffset = endOffset;
	}

	/**
	 * Getter method for includedRangesIds.
	 *
	 * @return the includedRangesIds
	 */
	public List<String> getIncludedRangesIds() {
		return includedRangesIds;
	}

	/**
	 * Setter method for includedRangesIds.
	 *
	 * @param includedRangesIds
	 *            the includedRangesIds to set
	 */
	public void setIncludedRangesIds(List<String> includedRangesIds) {
		this.includedRangesIds = includedRangesIds;
	}

	/**
	 * Getter method for label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Setter method for label.
	 *
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Getter method for labelId.
	 *
	 * @return the labelId
	 */
	public String getLabelId() {
		return labelId;
	}

	/**
	 * Setter method for labelId.
	 *
	 * @param labelId
	 *            the labelId to set
	 */
	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

}
