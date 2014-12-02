package com.sirma.itt.emf.util;

import java.util.Comparator;

import com.sirma.itt.emf.domain.model.Sortable;

/**
 * Comparator that is capable of sorting objects that implement {@link Sortable} interface.
 * 
 * @author svelikov
 */
public class SortableComparator implements Comparator<Sortable> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Sortable o1, Sortable o2) {
		Integer sortable1 = o1.getOrder();
		Integer sortable2 = o2.getOrder();

		if ((sortable1 == null) || (sortable2 == null)) {
			return EqualsHelper.nullCompare(sortable1, sortable2);
		}
		return sortable1.compareTo(sortable2);
	}

}
