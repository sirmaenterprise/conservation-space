package com.sirma.sep.content.idoc.nodes.image;

/**
 * Dimension class. The dimensions are in {@link String} to support the Mirador format where the fraction
 * of the dimension represents a zoom level
 *
 * @author BBonev
 */
public class Dimension {

	private final String height;
	private final String width;

	/**
	 * Constructs the dimension by given raw height and width values.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Dimension(String width, String height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Constructs the dimension by given height and width.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Dimension(int width, int height) {
		this(Integer.toString(width), Integer.toString(height));
	}

	/**
	 * Construct a dimension instance using double values for height and width
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Dimension(double width, double height) {
		this(Double.toString(width), Double.toString(height));
	}

	public int getHeight() {
		return (int) Double.parseDouble(height);
	}

	public int getWidth() {
		return (int) Double.parseDouble(width);
	}

	public String getRawHeight() {
		return height;
	}

	public String getRawWidth() {
		return width;
	}
}
