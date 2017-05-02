package com.sirma.itt.seip.domain.search.facet;

/**
 * Enumerator for different sorting options related to the facets.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public enum FacetSortOptions {

	/** Sorts by the title of {@link FacetValue}. */
	SORT_ALPHABETICAL("alphabetical"),

	/** Sorts date {@link FacetValue} chronologically in time. */
	SORT_CHRONOLOGICAL("chronological"),

	/** Sorts by the counts of {@link FacetValue}. */
	SORT_MATCH("match"),

	/** Sorts {@link FacetValue} in ascending manner. */
	SORT_ASCENDING("ascending"),

	/** Sorts {@link FacetValue} in descending manner. */
	SORT_DESCENDING("descending");

	private final String option;

	/**
	 * Constructs a new sort option.
	 *
	 * @param option
	 *            the option
	 */
	private FacetSortOptions(String option) {
		this.option = option;
	}

	/**
	 * Gets the sort option.
	 *
	 * @return the sort option
	 */
	public String getValue() {
		return option;
	}

}
