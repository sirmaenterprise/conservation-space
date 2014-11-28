/**
 * Copyright (c) 2010 10.02.2010 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.bitmask;

import java.util.BitSet;

/**
 * Initialize a {@link BitSet} according the provided {@link String} mask.
 * Created instance of {@link BitMask} is modifiable and its size is always
 * enlarged, except if {@link #compact()} is called which will compact the size
 * of the instance to the index of last set bit. Otherwise if other method which
 * modify the mask is called it will enlarge the mask if needed to provide the
 * functionality. So if no {@link #compact()} is called the {@link BitMask}
 * should be threat as an instance which is not limited to its size in time.
 * 
 * @see #compact()
 * @author Hristo Iliev
 */
public class BitMask extends BitSet {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -283198769608785461L;

	/**
	 * specify the real size of the bit mask. If a method modify the mask in
	 * order to set an bet at position larger than current size of mask the size
	 * will be set to correspond to the size of the bit it is set
	 */
	private int realSize;

	/**
	 * Bit mask to be initialized. Mask is read from left to right.
	 * 
	 * @param mask
	 *            {@link String}, mask to be set
	 */
	public BitMask(final String mask) {
		this(mask, true);
	}

	/**
	 * Bit mask to be initialized.
	 * 
	 * @param mask
	 *            {@link String}, mask to be set
	 * @param leftToRight
	 *            boolean, if true the mask is read from left to right,
	 */
	public BitMask(final String mask, final boolean leftToRight) {
		super(mask.length());
		this.realSize = mask.length();
		if (leftToRight) {
			for (int i = 0; i < mask.length(); i++) {
				char bit = mask.charAt(i);
				if (bit == '1') {
					super.set(i);
				} else if (bit == '0') {
					super.clear(i);
				} else {
					throw new IllegalArgumentException("Character of bit " + i
							+ " is invalid in the mask " + mask);
				}
			}
		} else {
			for (int i = mask.length() - 1; i >= 0; i--) {
				char bit = mask.charAt(i);
				if (bit == '1') {
					super.set(i);
				} else if (bit == '0') {
					super.clear(i);
				} else {
					throw new IllegalArgumentException("Character of bit " + i
							+ " is invalid in the mask " + mask);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.toString(true);
	}

	/**
	 * Construct string from the {@link BitMask}. Parameter specify from which
	 * direction to construct - if {@code true} the bit which will be retrieved
	 * if {@code get(0)} is called is the first character in the returned
	 * {@link String}, otherwise if {@code false} it will be the last character.
	 * 
	 * @param leftToRight
	 *            boolean, is the bits are ordered from left to right or right
	 *            to left
	 * @return {@link String}, the string representation of the mask
	 */
	public String toString(final boolean leftToRight) {
		StringBuilder builder = new StringBuilder(this.realSize);
		if (leftToRight) {
			for (int i = 0; i < this.realSize; i++) {
				builder.append(get(i) ? '1' : '0');
			}
		} else {
			for (int i = this.realSize - 1; i >= 0; i--) {
				builder.append(get(i) ? '1' : '0');
			}
		}
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object clone() {
		BitMask result = (BitMask) super.clone();
		result.realSize = getRealSize();
		return result;
	}

	/**
	 * Shrunk the mask to the index of last set bit. After calling this method
	 * the {@link #getRealSize()} and {@link #length()} should return the same
	 * value.
	 */
	public void compact() {
		// implement shrunk of the size to the index of last set bit
		setRealSize(length());
	}

	/**
	 * Change the real size of the bitMask if the specified length index is
	 * greater than the current size. If the specified length is smaller, it
	 * will not shrunk the the real size.
	 * 
	 * @param length
	 *            int, the size to which will be tried to be enlarged
	 */
	private void enlargeSizeIfNeeded(final int length) {
		setRealSize(Math.max(getRealSize(), length));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void and(final BitSet set) {
		super.and(set);
		enlargeSizeIfNeeded(set.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void andNot(final BitSet set) {
		super.andNot(set);
		enlargeSizeIfNeeded(set.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		super.clear();
		enlargeSizeIfNeeded(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final int bitIndex) {
		super.clear(bitIndex);
		enlargeSizeIfNeeded(bitIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final int fromIndex, final int toIndex) {
		super.clear(fromIndex, toIndex);
		enlargeSizeIfNeeded(toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flip(final int bitIndex) {
		super.flip(bitIndex);
		enlargeSizeIfNeeded(bitIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flip(final int fromIndex, final int toIndex) {
		super.flip(fromIndex, toIndex);
		enlargeSizeIfNeeded(toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void or(final BitSet set) {
		super.or(set);
		enlargeSizeIfNeeded(set.length());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int bitIndex) {
		super.set(bitIndex);
		enlargeSizeIfNeeded(bitIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int bitIndex, final boolean value) {
		super.set(bitIndex, value);
		enlargeSizeIfNeeded(bitIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int fromIndex, final int toIndex) {
		super.set(fromIndex, toIndex);
		enlargeSizeIfNeeded(toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(final int fromIndex, final int toIndex, final boolean value) {
		super.set(fromIndex, toIndex, value);
		enlargeSizeIfNeeded(toIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void xor(final BitSet set) {
		super.xor(set);
		enlargeSizeIfNeeded(set.length());
	}

	/**
	 * Getter method for realSize.
	 * 
	 * @return the realSize
	 */
	public int getRealSize() {
		return this.realSize;
	}

	/**
	 * Setter method for realSize.
	 * 
	 * @param realSize
	 *            the realSize to set
	 */
	private void setRealSize(final int realSize) {
		this.realSize = realSize;
	}

}
