package com.sirma.itt.emf.search.model;

/**
 * The Class Sorter that holds all data needed for sorting.
 */
public class Sorter {

	/** The Constant SORT_ASCENDING. */
	public static final String SORT_ASCENDING = "asc";

	/** The Constant SORT_DESCENDING. */
	public static final String SORT_DESCENDING = "desc";

	/** The ascending order. */
	private boolean ascendingOrder = true;

	/** The sort field. */
	private final String sortField;

	/**
	 * Instantiates a new sorter.
	 *
	 * @param key
	 *            the key
	 * @param isAscendingOrder
	 *            if the is ascending
	 */
	public Sorter(String key, boolean isAscendingOrder) {
		sortField = key;
		ascendingOrder = isAscendingOrder;
	}

	/**
	 * Instantiates a new sorter.
	 *
	 * @param key
	 *            the key
	 * @param order
	 *            the order
	 */
	public Sorter(String key, String order) {
		sortField = key;
		ascendingOrder = (order == null) || SORT_ASCENDING.equalsIgnoreCase(order);
	}

	/**
	 * Getter method for ascendingOrder.
	 *
	 * @return the ascendingOrder
	 */
	public boolean isAscendingOrder() {
		return ascendingOrder;
	}

	/**
	 * Setter method for ascendingOrder.
	 *
	 * @param ascendingOrder
	 *            the ascendingOrder to set
	 */
	public void setAscendingOrder(boolean ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}

	/**
	 * Gets the sort field.
	 *
	 * @return the sortField
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (ascendingOrder ? 1231 : 1237);
		result = (prime * result) + ((sortField == null) ? 0 : sortField.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Sorter)) {
			return false;
		}
		Sorter other = (Sorter) obj;
		if (ascendingOrder != other.ascendingOrder) {
			return false;
		}
		if (sortField == null) {
			if (other.sortField != null) {
				return false;
			}
		} else if (!sortField.equals(other.sortField)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Sorter [sortField=");
		builder.append(sortField);
		builder.append(", ascendingOrder=");
		builder.append(ascendingOrder);
		builder.append("]");
		return builder.toString();
	}

}