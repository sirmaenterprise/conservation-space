package com.sirma.itt.cmf.util;

import java.util.Comparator;

import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * User comparator that sorts the users by their display name using the default format: Given name
 * Family name
 *
 * @author BBonev
 */
public class UserDisplayNameComparator implements Comparator<Resource> {

	/** The ascending. */
	private boolean ascending;

	/**
	 * Instantiates a new user display name comparator with ascending sorting
	 */
	public UserDisplayNameComparator() {
		this(true);
	}

	/**
	 * Instantiates a new user display name comparator.
	 *
	 * @param ascending
	 *            the ascending
	 */
	public UserDisplayNameComparator(boolean ascending) {
		this.ascending = ascending;
	}

	@Override
	public int compare(Resource o1, Resource o2) {
		int compare = EqualsHelper.nullCompare(o1, o2);
		if (compare != 2) {
			return ascending ? compare : -compare;
		}
		String displayName1 = o1.getDisplayName();
		String displayName2 = o2.getDisplayName();
		compare = EqualsHelper.nullCompare(displayName1, displayName2);
		if (compare != 2) {
			return ascending ? compare : -compare;
		}
		compare = displayName1.compareTo(displayName2);
		return ascending ? compare : -compare;
	}

}
