package com.sirma.itt.seip.domain.search.tree;

/**
 * Contains constants related to constructing criteria with wildcards.
 *
 * @author Mihail Radkov
 */
public class CriteriaWildcards {

	/**
	 * Setting this as {@link Rule} value would return results of all types.
	 */
	public static final String ANY_OBJECT = "anyObject";

	/**
	 * Using this as {@link Rule} field will find everything related to given value. This queries object properties.
	 */
	public static final String ANY_RELATION = "anyRelation";

	/**
	 * Using this as {@link Rule} field will search in all data properties with the given value.
	 */
	public static final String ANY_FIELD = "anyField";

	private CriteriaWildcards() {
		// Prevents instantiation for the utility class.
	}

}
