package com.sirma.itt.seip.adapters.iiif;

/**
 * Provides functionality that computes physical scale ratio of an image
 *
 * @author radoslav
 */
public interface ImagePhysicalScaleService {

	/**
	 * Computes the scale ratio
	 *
	 * @param imageId
	 *            The image id
	 * @param canvas
	 *            The canvas dimensions
	 * @return Wrapper of the physical scale information
	 */
	PhysicalScale getPhysicalScale(String imageId, Dimension<Integer> canvas);

}
