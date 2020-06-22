package com.sirma.itt.seip.domain.search;

import com.sirma.itt.seip.domain.search.tree.Condition;

/**
 * Search context that provides additional information during rule building
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 25/04/2019
 */
public class SearchContext {
	private final Condition root;

	public SearchContext(Condition root) {this.root = root;}

	/**
	 * The current condition root
	 *
	 * @return the root condition.
	 */
	public Condition getRoot() {
		return root;
	}
}
