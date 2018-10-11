package com.sirma.itt.seip.adapters.iiif;

/**
 * Dimensions of a physical object
 *
 * @author radoslav
 */
public class PhysicalDimension extends Dimension<Double> {

	private final String unit;

	/**
	 * Physical dimension
	 *
	 * @param width
	 *            The width
	 * @param height
	 *            The height
	 * @param unit
	 *            The measurement unit
	 */
	public PhysicalDimension(Double width, Double height, String unit) {
		super(width, height);
		this.unit = unit;
	}

	/**
	 * Physical dimension with only width available.
	 *
	 * @param width
	 *            the width
	 * @param unit
	 *            the unit
	 */
	public PhysicalDimension(Double width, String unit) {
		super(width, Double.valueOf(0.0));
		this.unit = unit;
	}

	/**
	 * Get the measurement unit
	 *
	 * @return The unit
	 */
	public String getMeasurementUnit() {
		return unit;
	}

	/**
	 * Checks if dimension is valid. A dimension is valid it has non <code>null</code> and positive width and has a
	 * defined a measurement unit.
	 *
	 * @param dimension
	 *            The dimension to be checked
	 * @return True if it is valid
	 */
	public static boolean isValid(PhysicalDimension dimension) {
		return dimension.getWidth() != null && dimension.getWidth().doubleValue() > 0.0
				&& dimension.getMeasurementUnit() != null;
	}

}
