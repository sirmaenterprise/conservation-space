package com.sirma.itt.seip.instance.content;

import java.io.Serializable;
import java.util.Optional;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;

/**
 * Controls the updates of document contents.
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
	 */
	ContentInfo checkIn(Content content, Serializable contentId);

	/**
	 * Check out the working copy of the document and generate a download link for it. The download link will contain a
	 * custom name for the document that will be in the form of filename.contentId.extension. The contentId is used for
	 * the checkIn operation. Checking out a document locks it, preventing other users from checkout it out again.
	 *
	 * @param contentId
	 *            the content id of the document
	 * @return the download url if available
	 */
	Optional<String> checkOut(Serializable contentId);
}
