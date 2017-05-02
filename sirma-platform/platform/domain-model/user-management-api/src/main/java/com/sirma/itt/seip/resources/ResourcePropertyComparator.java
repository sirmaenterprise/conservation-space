package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Base resource comparator that could sort resources by custom column/property identifier.
 *
 * @author BBonev
 * @param <T>
 *            the resource type
 * @param <C>
 *            the sort column identifier type
 */
public abstract class ResourcePropertyComparator<T extends Resource, C> implements Comparator<T>, Serializable {
	private static final long serialVersionUID = 6575547198729669706L;
	private final C sortColumn;
	private boolean nullsAreHigh;

	/**
	 * Instantiates a new user comparator.
	 *
	 * @param sortColumn
	 *            the sort column
	 */
	public ResourcePropertyComparator(C sortColumn) {
		this(sortColumn, true);
	}

	/**
	 * Instantiates a new user comparator.
	 *
	 * @param sortColumn
	 *            the sort column
	 * @param nullsAreHigh
	 *            the nulls are high
	 */
	public ResourcePropertyComparator(C sortColumn, boolean nullsAreHigh) {
		this.sortColumn = sortColumn;
		this.nullsAreHigh = nullsAreHigh;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(T t1, T t2) {
		String o1 = getSortedField(t1, sortColumn);
		String o2 = getSortedField(t2, sortColumn);
		if (o1 == o2) {
			return 0;
		}
		if (o1 == null || o1.trim().isEmpty()) {
			return this.nullsAreHigh ? -1 : 1;
		}
		if (o2 == null || o2.trim().isEmpty()) {
			return this.nullsAreHigh ? 1 : -1;
		}
		return o1.compareToIgnoreCase(o2);
	}

	/**
	 * Gets the sorted field.
	 *
	 * @param group
	 *            the group
	 * @param column
	 *            the column
	 * @return the sorted field
	 */
	protected abstract String getSortedField(T group, C column);

}
