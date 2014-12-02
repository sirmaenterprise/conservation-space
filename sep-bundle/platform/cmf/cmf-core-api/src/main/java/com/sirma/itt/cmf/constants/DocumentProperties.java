package com.sirma.itt.cmf.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Property keys for Document instance.
 *
 * @author BBonev
 */
public interface DocumentProperties extends DefaultProperties {

	/** The case attachment. */
	String TYPE_DOCUMENT_ATTACHMENT = "attachedDocument";
	/** The case structured attachment. */
	String TYPE_DOCUMENT_STRUCTURED = "structuredDocument";
	/**
	 * The Constant DEFAULT_PATH. The name of the path for all document definitions
	 */
	String DEFAULT_PATH = "document";
	/** The working copy aspect. */
	String WORKING_COPY = "workingCopy";
	/** The business identifier of the document. */
	String UNIQUE_DOCUMENT_IDENTIFIER = "uniqueDocumentIdentifier";

	/** The document MIMETYPE. */
	String MIMETYPE = "mimetype";
	/**
	 * (TRANSIENT) The location to the file to be uploaded. The type of the value should implement
	 * the interface {@link com.sirma.itt.emf.adapter.FileDescriptor}
	 */
	String FILE_LOCATOR = "$file_locator$";

	/** The location of the file in the DMS. */
	String ATTACHMENT_LOCATION = "documentLocation";
	/** (TRANSIENT) The Id of the case in DMS. */
	String CASE_DMS_ID = "caseDmsId";

	/** The document version. */
	String VERSION = "documentVersion";

	/** (TRANSIENT) The is major revision. */
	String IS_MAJOR_VERSION = "$isMajorVersion$";

	/** (TRANSIENT) Description for new version. */
	String VERSION_DESCRIPTION = "$versionDescription$";

	/**
	 * (TRANSIENT) Description for thumbnail mode.See
	 * {@link com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode}
	 */
	String DOCUMENT_THUMB_MODE = "$thumbnailMode$";

	/** The file size. */
	String FILE_SIZE = "fileSize";

	/** The location of the working copy of the file in the DMS. */
	String WORKING_COPY_LOCATION = "workingCopyLocation";

	/**
	 * The Constant DOCUMENT_CONTENT_KEY used when searching for something in the documents content.
	 */
	String DOCUMENT_CONTENT_KEY = "documentContent";
	/** datetime for signing document */
	String DOCUMENT_SIGNED_DATE = "signedOn";

	/** The emf user who signed the document */
	String DOCUMENT_SIGNED_BY = "signedBy";

	/** Temporary property used to indicate that the document is being singed */
	String DOCUMENT_SIGNED = "$documentSigned$";

	String SECTION_LOCATION = "dmsId";
	/**
	 * Comment for DOCUMENT_CURRENT_VERSION_INSTANCE.
	 */
	String DOCUMENT_CURRENT_VERSION_INSTANCE = "currentVersionInstance";

	/** The not clonable properties. */
	Set<String> NOT_CLONABLE_DOCUMENT_PROPERTIES = Collections.unmodifiableSet(CollectionUtils
			.addToCollection(new HashSet<>(NOT_CLONABLE_PROPERTIES), VERSION, CASE_DMS_ID,
					LOCKED_BY, WORKING_COPY_LOCATION));

	/** The cloned dms id. */
	String CLONED_DMS_ID = "$clonedId$";

	/** The document purpose IDOC. */
	String PURPOSE_IDOC = "iDoc";

}
