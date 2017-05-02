package com.sirma.itt.cmf.alfresco4.remote;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.httpclient.methods.multipart.PartSource;

import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.seip.context.Context;

/**
 * Used to store required data for correct file update in alfresco.
 *
 * @author A. Kunchev
 */
public class ContentUpdateContext extends Context<String, Object> {

	private static final long serialVersionUID = -1880532029488502858L;

	private static final int INITIAL_CONTEXT_SIZE = 9;

	private static final String SERVICE_URL = "serviceUrl";
	private static final String FILE_PART = "filePart";
	private static final String NODE_DMS_ID = "nodeId";
	private static final String CONTENT_TYPE = "contentType";
	private static final String PROPERTIES = "properties";
	private static final String ASPECT_PROPERTIES = "aspectProperties";
	private static final String MAJOR_VERSION = "majorVersion";
	private static final String VERSION_DESCRIPTION = "versionDescription";
	private static final String THUMBNAIL_MODE = "thumbnailMode";

	private ContentUpdateContext(String serviceUrl, String dmsId) {
		super(INITIAL_CONTEXT_SIZE);
		put(SERVICE_URL, serviceUrl);
		put(NODE_DMS_ID, dmsId);
	}

	/**
	 * Creates context object.
	 *
	 * @param serviceUrl
	 *            the URL of the service through which will be done the update. Required
	 * @param dmsId
	 *            the id of the node that will be updated. Required
	 * @return {@link ContentUpdateContext} object
	 */
	public static ContentUpdateContext create(String serviceUrl, String dmsId) {
		Objects.requireNonNull(serviceUrl, "Service URL is required.");
		Objects.requireNonNull(dmsId, "Dms id is required.");
		return new ContentUpdateContext(serviceUrl, dmsId);
	}

	public String getServiceURL() {
		return getIfSameType(SERVICE_URL, String.class);
	}

	public ContentUpdateContext setFilePart(PartSource partSource) {
		put(FILE_PART, partSource);
		return this;
	}

	public PartSource getFilePart() {
		return getIfSameType(FILE_PART, PartSource.class);
	}

	public String getNodeDmsId() {
		return getIfSameType(NODE_DMS_ID, String.class);
	}

	public ContentUpdateContext setContentType(String type) {
		put(CONTENT_TYPE, type);
		return this;
	}

	public String getContentType() {
		return getIfSameType(CONTENT_TYPE, String.class);
	}

	public ContentUpdateContext setProperties(Map<String, Serializable> properties) {
		put(PROPERTIES, properties);
		return this;
	}

	public Map<String, Serializable> getProperties() {
		return getIfSameType(PROPERTIES, Map.class);
	}

	public ContentUpdateContext setAspectProperties(Set<String> aspectProperties) {
		put(ASPECT_PROPERTIES, aspectProperties);
		return this;
	}

	public Set<String> getAspectProperties() {
		return getIfSameType(ASPECT_PROPERTIES, Set.class, new HashSet<>());
	}

	/**
	 * For update this always should return {@link Boolean#TRUE}.
	 *
	 * @return {@link Boolean#TRUE}
	 */
	@SuppressWarnings("static-method")
	public Boolean shouldOverwrite() {
		return Boolean.TRUE;
	}

	/**
	 * Sets flag that shows that major version should be increased instead of minor.
	 *
	 * @deprecated the versions are managed by our system
	 * @param majorVersion
	 *            {@link Boolean} that shows if major version should be increased
	 * @return current object to allow chaining
	 */
	@Deprecated
	public ContentUpdateContext setMajorVersion(Boolean majorVersion) {
		put(MAJOR_VERSION, majorVersion);
		return this;
	}

	/**
	 * Getter for flag that shows if the major version should be increased instead of minor.
	 *
	 * @deprecated the versions are managed by our system
	 * @return {@link Boolean#TRUE} if the major version should be increased, {@link Boolean#FALSE} otherwise
	 */
	@Deprecated
	public Boolean isMajorVersion() {
		return getIfSameType(MAJOR_VERSION, Boolean.class, Boolean.FALSE);
	}

	public ContentUpdateContext setVersionDescription(String description) {
		put(VERSION_DESCRIPTION, description);
		return this;
	}

	public String getVersionDescription() {
		return getIfSameType(VERSION_DESCRIPTION, String.class, "");
	}

	public ContentUpdateContext setThumbnailMode(String mode) {
		put(THUMBNAIL_MODE, mode);
		return this;
	}

	public String getThumbnailMode() {
		return getIfSameType(THUMBNAIL_MODE, String.class, ThumbnailGenerationMode.NONE.toString());
	}

}
