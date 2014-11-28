/**
 * Copyright (c) 2010 13.02.2010 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.bitmask;

import java.lang.ref.SoftReference;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.commons.exceptions.UnmodifiableException;

/**
 * Unmodifiable BitMask class. This class is an descendant of {@link BitMask}
 * class which is supposed to be consistent at the whole life of the object and
 * will not be changed. If the
 *
 * @author Hristo Iliev
 */
public final class UnmodifiableBitMask extends BitMask {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5099952104979617967L;

	/** cache of {@link UnmodifiableBitMask} entries */
	private static final Map<String, SoftReference<UnmodifiableBitMask>> CACHE = new HashMap<String, SoftReference<UnmodifiableBitMask>>();

	/**
	 * Constructor with specified mask.
	 *
	 * @param mask
	 *            {@link String}, the mask used to set the mask
	 */
	protected UnmodifiableBitMask(final String mask) {
		super(mask);
	}

	/**
	 * Constructor with specified mask and direction of the mask.
	 *
	 * @param mask
	 *            {@link String}, the mask used to set the mask
	 * @param leftToRight
	 *            boolean, the direction of the {@link String} representation
	 */
	protected UnmodifiableBitMask(final String mask, final boolean leftToRight) {
		super(mask, leftToRight);
	}

	/**
	 * Implements or method from the super class.
	 *
	 * @param first
	 *            is the first mask to be processed
	 * @param second
	 *            is the second mask to be processed
	 * @return result mask
	 */
	public static UnmodifiableBitMask or(UnmodifiableBitMask first,
			UnmodifiableBitMask second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		BitMask mask = new BitMask(first.toString());
		mask.or(new BitMask(second.toString()));
		return UnmodifiableBitMask.valueOf(mask.toString());
	}

	/**
	 * Get an {@link UnmodifiableBitMask} object which represent the specified
	 * value. The order of the value is supposed to be left to right. See
	 * {@link UnmodifiableBitMask#valueOf(String, boolean)} for more
	 * information.
	 *
	 * @param value
	 *            {@link String}, the {@link String} representation of the bit
	 *            set
	 * @return {@link UnmodifiableBitMask}, mask which is ought to be
	 *         unmodifiable
	 */
	public static UnmodifiableBitMask valueOf(final String value) {
		return UnmodifiableBitMask.valueOf(value, true);
	}

	/**
	 * Get an {@link UnmodifiableBitMask} object which represent the specified
	 * value. If this method is called for second time, it is possible to return
	 * the same value as the value of the first call. The returned values may be
	 * cached by its {@link String} representation taken in manner of left to
	 * right direction, so there is no matter does the {@code leftToRight}
	 * variable is set to {@code true} or {@code false}, if the original
	 * representation of the {@link BitMask} is the same, it may return the same
	 * instance. There is no guarantee that the returned instance will be the
	 * same every time, if the method is called with the same argument. This is
	 * implementation dependent factor which should not be taken as rule. In
	 * general, because of memory optimization the values can be cached, but in
	 * such order that it will not constraint the usability of the memory. So if
	 * there is {@link OutOfMemoryError} thrown, it should be ensured that the
	 * cache will be emptied, in order to provide the used memory for other
	 * parts of application.
	 *
	 * @param value
	 *            {@link String}, the {@link String} representation of the bit
	 *            set
	 * @param leftToRight
	 *            boolean, boolean variable which shows the representation of
	 *            the {@link String}
	 * @return {@link UnmodifiableBitMask}, mask which is ought to be
	 *         unmodifiable
	 * @see SoftReference
	 */
	public static UnmodifiableBitMask valueOf(final String value,
			final boolean leftToRight) {
		String leftToRightValue;
		if (leftToRight) {
			leftToRightValue = value;
		} else {
			// in cache all keys are left to right
			leftToRightValue = new StringBuilder(value).reverse().toString();
		}
		SoftReference<UnmodifiableBitMask> maskReference = UnmodifiableBitMask.CACHE
				.get(leftToRightValue);
		UnmodifiableBitMask mask = null;
		if (maskReference != null) {
			// if such mask was occurred get its value if not cleared by Garbage
			// collector
			mask = maskReference.get();
		}
		if (mask == null) {
			// if such mask does not occur by now or it is cleared by Garbage
			// collector
			mask = new UnmodifiableBitMask(leftToRightValue);
			// replace the old reference if such exist so the empty
			// SoftReference will be cleared
			UnmodifiableBitMask.CACHE.put(leftToRightValue,
					new SoftReference<UnmodifiableBitMask>(mask));
		}
		return mask;
	}

	/**
	 * Method which should be called if the object is tried to be modified.
	 */
	private void tryModification() {
		throw new UnmodifiableException(
				"Instance of com.sirma.itt.commons.utils.bitmask.UnmodifiableBitMask cannot be changed.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void and(final BitSet set) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void andNot(final BitSet set) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final int bitIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final int fromIndex, final int toIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flip(final int bitIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flip(final int fromIndex, final int toIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void or(final BitSet set) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int bitIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int bitIndex, final boolean value) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int fromIndex, final int toIndex) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int fromIndex, final int toIndex, final boolean value) {
		tryModification();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void xor(final BitSet set) {
		tryModification();
	}
}