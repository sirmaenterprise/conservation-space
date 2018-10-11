package com.sirma.itt.seip.adapters.iiif;

/**
 * Dimension class
 * 
 * @param <T>
 *            Type of width/height dimensions
 * @author radoslav
 */
public class Dimension<T> {

	private final T height;
	private final T width;

	/**
	 * Constructs the dimension by given height and width.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Dimension(T width, T height) {
		this.width = width;
		this.height = height;
	}

	public T getHeight() {
		return height;
	}

	public T getWidth() {
		return width;
	}
}
