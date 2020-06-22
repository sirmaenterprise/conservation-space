package com.sirma.itt.seip.adapters.iiif;

import java.io.Serializable;
import java.util.List;

/**
 * Interface that provides functionality for processing image manifests.
 *
 * @author Nikolay Ch
 * @author radoslav
 */
public interface ImageManifestService {
	String MANIFEST_PURPOSE = "manifest";

	/**
	 * Obtains the content info of the given image ids, creates or updates the manifest and stores it in the content
	 * service.
	 *
	 * @param manifestId
	 *            the id of the manifest /null if the manifest is not created yet/
	 * @param imageWidgetID
	 *            the id of the image widget
	 * @param uploadedImageIDs
	 *            the ids of the selected images
	 * @return the id of the created manifest stored in the content service
	 */
	String processManifest(String manifestId, String imageWidgetID, List<? extends Serializable> uploadedImageIDs);

	/**
	 * Obtains the manifest from the content service and returns an object with its information.
	 *
	 * @param manifestId
	 *            the id of the manifest
	 * @return the object that contains the information about the manifest or null if no manifest is found with thegiven
	 *         id
	 */
	Manifest getManifest(String manifestId);

}
