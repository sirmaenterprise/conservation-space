package com.sirma.sep.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sirma.sep.content.ContentMetadata.SimpleContentMetadata;

/**
 * Type-safely provides a metadata about a particular image content.
 *
 * @author Adrian Mitev
 */
public class ImageContentMetadata extends SimpleContentMetadata {

	public static final String HEIGHT = "height";
	public static final String WIDTH = "width";
	public static final String IMAGE_ID = "id";

	/**
	 * Initializes the object using predefined metadata.
	 *
	 * @param metadata
	 *            predefined metadata.
	 */
	public ImageContentMetadata(Map<String, Serializable> metadata) {
		super(metadata);
	}

	/**
	 * Initializes the object using predefined metadata.
	 *
	 * @param imageId
	 *            image id
	 * @param width
	 *            image width
	 * @param height
	 *            image height
	 * @return constructed instance
	 */
	public static ImageContentMetadata build(String imageId, int width, int height) {
		Map<String, Serializable> metdata = new HashMap<>();
		metdata.put(IMAGE_ID, imageId);
		metdata.put(WIDTH, width);
		metdata.put(HEIGHT, height);

		return new ImageContentMetadata(metdata);
	}

	/**
	 * Wraps {@link ContentMetadata} in a {@link ImageContentMetadata}
	 *
	 * @param metadata
	 *            object to wrap.
	 * @return wrapped object
	 */
	public static ImageContentMetadata wrapContentMetadata(ContentMetadata metadata) {
		if (metadata instanceof ImageContentMetadata) {
			return (ImageContentMetadata) metadata;
		}

		return new ImageContentMetadata(metadata.getProperties());
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public String getId() {
		return getString(IMAGE_ID);
	}

	/**
	 * Getter method for height.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return getInt(HEIGHT);
	}

	/**
	 * Getter method for width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return getInt(WIDTH);
	}

}
