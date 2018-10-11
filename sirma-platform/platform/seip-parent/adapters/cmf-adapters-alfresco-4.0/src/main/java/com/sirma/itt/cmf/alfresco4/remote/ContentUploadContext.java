package com.sirma.itt.cmf.alfresco4.remote;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.httpclient.methods.multipart.PartSource;

import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.seip.context.Context;

/**
 * Context object containing data for alfresco file upload.
 *
 * @author A. Kunchev
 */
public class ContentUploadContext extends Context<String, Object> {

	private static final long serialVersionUID = -6663399465407481858L;

	private static final int INITIAL_CONTEXT_SIZE = 13;

	private static final String SERVICE_URL = "serviceUrl";
	private static final String FILE_PART = "filePart";
	private static final String SITE_ID = "siteId";
	private static final String FOLDER = "folfer";
	private static final String PARENT_NODE_ID = "parentNodeId";
	private static final String CONTENT_TYPE = "contentType";
	private static final String PROPERTIES = "properties";
	private static final String ASPECT_PROPERTIES = "aspectProperties";
	private static final String OVERWRITE = "overwrite";
	private static final String MAJOR_VERSION = "majorVersion";
	private static final String VERSION_DESCRIPTION = "versionDescription";
	private static final String UPLOAD_MODE = "uploadMode";
	private static final String THUMBNAIL_MODE = "thumbnailMode";

	private ContentUploadContext(String serviceUrl, String uploadMode) {
		super(INITIAL_CONTEXT_SIZE);
		put(SERVICE_URL, serviceUrl);
		put(UPLOAD_MODE, uploadMode);
	}

	/**
	 * Creates context object.
	 *
	 * @param serviceUrl
	 *            the URL of the service through which will be done the upload. Required
	 * @param uploadMode
	 *            the upload mode. Required
	 * @return {@link ContentUploadContext} object
	 */
	public static ContentUploadContext create(String serviceUrl, UploadMode uploadMode) {
		Objects.requireNonNull(serviceUrl, "Service URL is required.");
		Objects.requireNonNull(uploadMode, "Upload mode is required.");
		return new ContentUploadContext(serviceUrl, uploadMode.toString());
	}

	/**
	 * Checks if there is set at least one alfresco location for the file. The checked locations are:
	 * <ul>
	 * <li>parent node id</li>
	 * <li>site id</li>
	 * <li>folder</li>
	 * </ul>
	 *
	 * @return <code>true</code> if at least one location is set, <code>false</code> if there is no location set
	 */
	public boolean isLocationAvailable() {
		return getParentNodeId() != null || getSiteId() != null || getFolder() != null;
	}

	public String getServiceURL() {
		return getIfSameType(SERVICE_URL, String.class);
	}

	public ContentUploadContext setFilePart(PartSource partSource) {
		put(FILE_PART, partSource);
		return this;
	}

	public PartSource getFilePart() {
		return getIfSameType(FILE_PART, PartSource.class);
	}

	public ContentUploadContext setSiteId(String site) {
		put(SITE_ID, site);
		return this;
	}

	public String getSiteId() {
		return getIfSameType(SITE_ID, String.class);
	}

	public ContentUploadContext setFolder(String folder) {
		put(FOLDER, folder);
		return this;
	}

	public String getFolder() {
		return getIfSameType(FOLDER, String.class);
	}

	public ContentUploadContext setParentNodeId(String nodeId) {
		put(PARENT_NODE_ID, nodeId);
		return this;
	}

	public String getParentNodeId() {
		return getIfSameType(PARENT_NODE_ID, String.class);
	}

	public ContentUploadContext setContentType(String type) {
		put(CONTENT_TYPE, type);
		return this;
	}

	public String getContentType() {
		return getIfSameType(CONTENT_TYPE, String.class);
	}

	public ContentUploadContext setProperties(Map<String, Serializable> properties) {
		put(PROPERTIES, properties);
		return this;
	}

	public Map<String, Serializable> getProperties() {
		return getIfSameType(PROPERTIES, Map.class);
	}

	public ContentUploadContext setAspectProperties(Set<String> aspectProperties) {
		put(ASPECT_PROPERTIES, aspectProperties);
		return this;
	}

	public Set<String> getAspectProperties() {
		return getIfSameType(ASPECT_PROPERTIES, Set.class, new HashSet<>());
	}

	public ContentUploadContext setOverwrite(Boolean overwrite) {
		put(OVERWRITE, overwrite);
		return this;
	}

	/**
	 * Checks if the current file should be overwritten. By default returns {@link Boolean#FALSE}
	 *
	 * @return {@link Boolean#TRUE} if the current file should be overwritten, {@link Boolean#FALSE} otherwise
	 */
	public Boolean shouldOverwrite() {
		return getIfSameType(OVERWRITE, Boolean.class, Boolean.FALSE);
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
	public ContentUploadContext setMajorVersion(Boolean majorVersion) {
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

	public ContentUploadContext setVersionDescription(String description) {
		put(VERSION_DESCRIPTION, description);
		return this;
	}

	public String getVersionDescription() {
		return getIfSameType(VERSION_DESCRIPTION, String.class);
	}

	public String getUploadMode() {
		return getIfSameType(UPLOAD_MODE, String.class);
	}

	public ContentUploadContext setThumbnailMode(String mode) {
		put(THUMBNAIL_MODE, mode);
		return this;
	}

	public String getThumbnailMode() {
		return getIfSameType(THUMBNAIL_MODE, String.class, ThumbnailGenerationMode.NONE.toString());
	}

}
