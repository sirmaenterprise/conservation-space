package com.sirma.itt.seip.collections;

import java.util.Collections;
import java.util.Set;

/**
 * Sealable {@link Set}. When sealed the collection does not allow data modifications. Any call to methods that modifies
 * the content will be ignored without exceptions.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 */
public class SealedSet<E> extends SealedCollection<E, Set<E>>implements Set<E> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 739994045049055480L;

	/**
	 * Instantiates a new already sealed set.
	 *
	 * @param set
	 *            the set
	 */
	public SealedSet(Set<E> set) {
		this(set, true);
	}

	/**
	 * Instantiates a new set that can be sealed later.
	 *
	 * @param set
	 *            the set
	 * @param sealNow
	 *            the seal now
	 */
	public SealedSet(Set<E> set, boolean sealNow) {
		super(set == null ? Collections.<E> emptySet() : set, sealNow);
	}

}
