package com.sirma.itt.cmf.constants;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Property keys for Document instance.
 *
 * @author BBonev
 */
public interface DocumentProperties extends DefaultProperties {

	String TYPE_DOCUMENT_ATTACHMENT = "attachedDocument";

	String TYPE_DOCUMENT_STRUCTURED = "structuredDocument";

	/** (TRANSIENT) The Id of the case in DMS. */
	String CASE_DMS_ID = "caseDmsId";

	/** (TRANSIENT) The is major revision. */
	String IS_MAJOR_VERSION = "$isMajorVersion$";

	/** (TRANSIENT) Description for new version. */
	String VERSION_DESCRIPTION = "$versionDescription$";

	/**
	 * (TRANSIENT) Description for thumbnail mode.See {@link com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode}
	 */
	String DOCUMENT_THUMB_MODE = "$thumbnailMode$";

	/** The location of the working copy of the file in the DMS. */
	String WORKING_COPY_LOCATION = "workingCopyLocation";

	String CLONED_DMS_ID = "$clonedId$";
}
