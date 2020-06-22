package com.sirma.sep.content;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * {@link ContentImport} represents a {@link Content} object that need to be registered into the system without actually
 * storing any content. Along with the {@link Content} methods this interface adds methods that describe the remote
 * content location that can be used for accessing the content itself via proper
 * {@code com.sirma.itt.seip.content.ContentStore}.
 * <p>
 * In order the {@link ContentImport} to be valid the following methods should not return <code>null</code> values
 * <ul>
 * <li>{@link #getInstanceId()}
 * <li>{@link #getName()}
 * <li>{@link #getPurpose()}
 * <li>{@link #getRemoteId()}
 * <li>{@link #getRemoteSourceName()}
 * <li>{@link #getMimeType()}. The mime type could be resolved if the method {@link #getContent()} returns non
 * <code>null</code> value.
 * <li>{@link #getCharset()} if not provided {@code UTF-8} will be used
 * </ul>
 *
 * @author BBonev
 */
public interface ContentImport extends Content {

	@Override
	ContentImport setCharset(String charset);

	@Override
	ContentImport setContent(FileDescriptor content);

	@Override
	ContentImport setContent(String content, String charset);

	@Override
	default ContentImport setContent(byte[] data) {
		return (ContentImport) Content.super.setContent(data);
	}

	@Override
	default ContentImport setContent(File file) {
		return (ContentImport) Content.super.setContent(file);
	}

	@Override
	ContentImport setIndexable(boolean indexable);

	@Override
	ContentImport setContentLength(Long length);

	@Override
	ContentImport setMimeType(String mimeType);

	@Override
	ContentImport setPurpose(String purpose);

	@Override
	ContentImport setView(boolean view);

	@Override
	ContentImport setName(String name);

	@Override
	ContentImport setProperties(Map<String, Serializable> properties);

	@Override
	ContentImport setContentId(String id);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that content reuse is not allowed during content import due to:
	 * <ul>
	 * <li>If importing external content we do not have any actual content to manage
	 * <li>If importing locally managed content we are not actually uploading any content
	 * </ul>
	 * Calling this method will result in {@link UnsupportedOperationException}
	 */
	@Override
	ContentImport allowReuse();

	/**
	 * Sets the remote id content id that will be used for accessing the content. The provided id may be specific to the
	 * implementation of the store that will handle the actual content access.
	 *
	 * @param remoteId
	 *            the remote id to set
	 * @return the same instance to allow method chaining
	 */
	ContentImport setRemoteId(String remoteId);

	/**
	 * Returns already set remote id that identifies a content at remote system.
	 *
	 * @return the remote id
	 */
	String getRemoteId();

	/**
	 * Sets the name of the remote source. This is the name of the
	 * {@code com.sirma.itt.seip.content.ContentStore#getName()} implementation that will handle the provided remote id.
	 *
	 * @param remoteSystemName
	 *            the remote system name
	 * @return the same instance to allow method chaining
	 */
	ContentImport setRemoteSourceName(String remoteSystemName);

	/**
	 * Gets the remote source name.
	 *
	 * @return the remote source name
	 */
	String getRemoteSourceName();

	/**
	 * Sets the instance id that relates to the given content
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the same instance to allow method chaining
	 */
	ContentImport setInstanceId(Serializable instanceId);

	/**
	 * Gets the instance id that relates to the content.
	 *
	 * @return the instance id
	 */
	Serializable getInstanceId();

	@Override
	ContentImport disableContentStoreEnforcingOnVersionUpdate();

	/**
	 * Creates an empty {@link ContentImport} implementation. The default implementation supports setting all fields.
	 *
	 * @return writable content import instance
	 */
	static ContentImport createEmpty() {
		return new ContentImport.ImportContentData();
	}

	/**
	 * Create a {@link ContentImport} object filled with the properties of the given {@link ContentInfo}. Effectively
	 * the the returned object allows the original content to be imported/assigned for other instance without actually
	 * cloning the content.<br>
	 * The method does not set the {@link #setInstanceId(Serializable)}.<br>
	 * If the original {@link ContentInfo#exists()} reports <code>false</code> an {@link IllegalArgumentException} will
	 * be thrown as it's not allowed to copy non existent content
	 *
	 * @param info
	 *            the source content info to copy from, required
	 * @return new content import instance filled with the info from the argument. Will not have it's instance id set!
	 */
	static ContentImport copyFrom(ContentInfo info) {
		Objects.requireNonNull(info, "Source ContentInfo is required");
		if (!info.exists()) {
			throw new IllegalArgumentException("The original content should exist in the system in order to copy it");
		}
		return createEmpty()
				.setCharset(info.getCharset())
					.setContentLength(info.getLength())
					.setIndexable(info.isIndexable())
					.setMimeType(info.getMimeType())
					.setName(info.getName())
					.setPurpose(info.getContentPurpose())
					.setRemoteId(info.getRemoteId())
					.setRemoteSourceName(info.getRemoteSourceName())
					.setView(info.isView());
	}

	/**
	 * Basic implementation of {@link ContentImport}. The class stores the and reports the values provided by the set
	 * methods.
	 *
	 * @author BBonev
	 */
	class ImportContentData extends Content.ContentData implements ContentImport {

		private static final long serialVersionUID = 9161350770610195811L;

		private String remoteId;
		private String remoteSystemName;
		private Serializable instanceId;

		@Override
		public ContentImport setContent(FileDescriptor content) {
			return (ContentImport) super.setContent(content);
		}

		@Override
		public ContentImport setContent(String content, String charset) {
			return (ContentImport) super.setContent(content, charset);
		}

		@Override
		public ContentImport setContent(String content, Charset charset) {
			return (ContentImport) super.setContent(content, charset);
		}

		@Override
		public ContentImport setCharset(String charset) {
			return (ContentImport) super.setCharset(charset);
		}

		@Override
		public ContentImport setContentLength(Long length) {
			return (ContentImport) super.setContentLength(length);
		}

		@Override
		public ContentImport setMimeType(String mimeType) {
			return (ContentImport) super.setMimeType(mimeType);
		}

		@Override
		public ContentImport setPurpose(String purpose) {
			return (ContentImport) super.setPurpose(purpose);
		}

		@Override
		public ContentImport setView(boolean view) {
			return (ContentImport) super.setView(view);
		}

		@Override
		public ContentImport setName(String name) {
			return (ContentImport) super.setName(name);
		}

		@Override
		public ContentImport setProperties(Map<String, Serializable> properties) {
			return (ContentImport) super.setProperties(properties);
		}

		@Override
		public ContentImport setIndexable(boolean indexable) {
			return (ContentImport) super.setIndexable(indexable);
		}

		@Override
		public ContentImport setRemoteId(String remoteId) {
			this.remoteId = remoteId;
			return this;
		}

		@Override
		public String getRemoteId() {
			return remoteId;
		}

		@Override
		public ContentImport setRemoteSourceName(String remoteSystemName) {
			this.remoteSystemName = remoteSystemName;
			return this;
		}

		@Override
		public String getRemoteSourceName() {
			return remoteSystemName;
		}

		@Override
		public ContentImport setInstanceId(Serializable instanceId) {
			this.instanceId = instanceId;
			return this;
		}

		@Override
		public ContentImport allowReuse() {
			throw new UnsupportedOperationException("Content reuse is not supported during content import!");
		}

		@Override
		public Serializable getInstanceId() {
			return instanceId;
		}

		@Override
		public ContentImport setDetectedMimeTypeFromContent(boolean isDetectedFromContent) {
			throw new UnsupportedOperationException(
					"Mimetype detection using the content is not supported during content import!");
		}

		@Override
		public boolean shouldDetectedMimeTypeFromContent() {
			// when importing we actually don't have content, so we can't use it to detect mimetype
			return false;
		}

		@Override
		public ContentImport setContentId(String id) {
			super.setContentId(id);
			return this;
		}

		@Override
		public ContentImport disableContentStoreEnforcingOnVersionUpdate() {
			return (ContentImport) super.disableContentStoreEnforcingOnVersionUpdate();
		}
	}
}