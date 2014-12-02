package com.sirma.itt.emf.search.model;

import java.util.List;

import com.sirma.itt.emf.domain.Pair;

/**
 * Holder class for search filter configurations.
 *
 * @author BBonev
 */
public class SearchFilterConfig extends Pair<List<SearchFilter>, List<SearchFilter>> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6130426855362886871L;

	/**
	 * Instantiates a new search filter config.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	public SearchFilterConfig(List<SearchFilter> first, List<SearchFilter> second) {
		super(first, second);
	}

	/**
	 * Gets the filters.
	 *
	 * @return the filters
	 */
	public List<SearchFilter> getFilters() {
		return getFirst();
	}

	/**
	 * Gets the sorter fields.
	 *
	 * @return the sorter fields
	 */
	public List<SearchFilter> getSorterFields() {
		return getSecond();
	}

}
