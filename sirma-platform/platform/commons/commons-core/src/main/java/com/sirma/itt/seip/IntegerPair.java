package com.sirma.itt.seip;

/**
 * Pair class intended for integer values
 *
 * @author BBonev
 */
public class IntegerPair extends Pair<Integer, Integer> {

	private static final long serialVersionUID = -7027678664315498967L;

	/** Represents a pair of values 0 for first and second */
	public static final IntegerPair EMPTY_RANGE = new IntegerPair(0, 0);

	/**
	 * Instantiates a new integer pair.
	 */
	public IntegerPair() {
		super();
	}

	/**
	 * Instantiates a new integer pair.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	public IntegerPair(Integer first, Integer second) {
		super(first, second);
	}

	/**
	 * Instantiates a new integer pair from a given pair
	 *
	 * @param
	 * 			<P>
	 *            the pair type
	 * @param copyFrom
	 *            the copy from
	 */
	public <P extends Pair<? extends Integer, ? extends Integer>> IntegerPair(P copyFrom) {
		super(copyFrom);
	}

}
