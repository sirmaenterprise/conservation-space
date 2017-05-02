package com.sirma.itt.seip.domain.util;

import java.util.Comparator;

import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Comparator that is capable of sorting objects that implement {@link Ordinal} interface. If elements are found equal
 * by their order. The comparator provides secondary sorting if the items implement comparable and are of the same type
 * to provide more stable sorting that does not depend on the occurrence in the source collection
 *
 * @author svelikov
 */
public class SortableComparator implements Comparator<Ordinal> {

	private boolean additionalCompare;

	/**
	 * Instantiates a new sortable comparator that does not perform additional comparison if elements are equal
	 */
	public SortableComparator() {
		this(false);
	}

	/**
	 * Instantiates a new sortable comparator.
	 *
	 * @param additionalCompare
	 *            the additional compare to perform if elements are equal
	 */
	public SortableComparator(boolean additionalCompare) {
		this.additionalCompare = additionalCompare;
	}

	@Override
	public int compare(Ordinal o1, Ordinal o2) {
		Integer sortable1 = o1.getOrder();
		Integer sortable2 = o2.getOrder();

		int compare = EqualsHelper.nullSafeCompare(sortable1, sortable2);
		if (compare == 0 && additionalCompare) {
			return secondaryCompare(o1, o2);
		}
		return compare;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int secondaryCompare(Ordinal o1, Ordinal o2) {
		if (o1.getClass().isInstance(o2) && o1 instanceof Comparable) {
			return ((Comparable) o1).compareTo(o2);
		}
		return 0;
	}

}
