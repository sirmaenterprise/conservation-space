package com.sirma.itt.seip.instance.content;

import java.io.File;
import java.io.Serializable;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Takes care of providing download of uploaded to instance file content, as well as updates instance's content with new
 * uploaded file.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
public interface CheckOutCheckInService {

	/**
	 * Checks-in the provided {@link Content} and unlocks the associated instance if it has been locked by the current
	 * user.
	 *
	 * @param content
	 *            is the content to check in
	 * @param contentId
	 *            is the content ID
	 * @return the {@link ContentInfo} of the updated content
	 * @deprecated use {@link #checkIn(UploadRequest, String)} instead.
	 */
	@Deprecated
	ContentInfo checkIn(Content content, Serializable contentId);

	/**
	 * Checks-in the provided {@link UploadRequest} and unlocks the associated instance.
	 *
	 * @param uploadRequest
	 *            is the uploaded file to check in
	 * @param instanceId
	 *            is the instance ID
	 * @return the {@link ContentInfo} of the updated content
	 */
	// TODO remove the second argument, we have it in the upload request already
	ContentInfo checkIn(UploadRequest uploadRequest, String instanceId);

	/**
	 * Extracts the uploaded content of an instance as file, updates it's properties if is supported MSOffice file and
	 * returns it.
	 *
	 * @param instanceId
	 *            the id of the instance, containing uploaded content
	 * @return the file if available
	 */
	File checkOut(Serializable instanceId);
}
