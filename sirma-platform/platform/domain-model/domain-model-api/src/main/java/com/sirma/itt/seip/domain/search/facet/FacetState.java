package com.sirma.itt.seip.domain.search.facet;

/**
 * Contains all states that a facet can have.
 *
 * @author nvelkov
 */
public enum FacetState {

	/**
	 * The facet will be expanded by default.
	 */
	EXPANDED("expanded"),

	/**
	 * The facet will be collapsed by default.
	 */
	COLLAPSED("collapsed");

	private final String state;

	/**
	 * Constructs a new state.
	 *
	 * @param state
	 *            the state
	 */
	private FacetState(String state) {
		this.state = state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getValue() {
		return state;
	}
}
