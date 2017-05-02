package com.sirma.itt.seip.permissions;

import java.util.Collections;
import java.util.List;

/**
 * Marker interface for filterable entity
 *
 * @author bbanchev
 */
public interface Filterable {
	/**
	 * Sets the entity filters.
	 *
	 * @param filtersIds
	 *            the filters identities to use
	 */
	void setFilters(List<String> filtersIds);

	/**
	 * Gets the filters.
	 *
	 * @return the filters
	 */
	List<String> getFilters();

	/**
	 * Get the filters from the given object if the object implements the {@link Filterable} interface.
	 *
	 * @param source
	 *            the source instance
	 * @return the object filters or empty list if not
	 */
	static List<String> getFilters(Object source) {
		if (source instanceof Filterable) {
			return ((Filterable) source).getFilters();
		}
		return Collections.emptyList();
	}

	/**
	 * Sets the filters to the given object if the object implements the {@link Filterable} interface.
	 *
	 * @param source
	 *            the source instance
	 * @param filters
	 *            filters to set
	 */
	static void setFilters(Object source, List<String> filters) {
		if (source instanceof Filterable) {
			((Filterable) source).setFilters(filters);
		}
	}
}
