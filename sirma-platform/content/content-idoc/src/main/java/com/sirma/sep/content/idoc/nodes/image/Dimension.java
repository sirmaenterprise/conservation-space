package com.sirma.sep.content.idoc.nodes.image;

/**
 * Dimension class
 *
 * @author BBonev
 */
public class Dimension {

	private final int height;
	private final int width;

	/**
	 * Constructs the dimension by given height and width.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
}
