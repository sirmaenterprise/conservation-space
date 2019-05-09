package com.sirma.sep.content;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * {@link Content} define methods that describes a content data that can be used for adding that content to the system.
 * In order the request object to be valid it should have at least name and content provider in the form of
 * {@link FileDescriptor}. So the methods {@link #getName()} and {@link #getContent()} should return non
 * <code>null</code> values.
 *
 * @author BBonev
 */
public interface Content extends Serializable {

	/** Content type for primary instance view. For example the idoc html is considered PRIMARY_VIEW. */
	String PRIMARY_VIEW = "primaryView";
	/**
	 * This content type is associated with instances that have some stored binary data. For example when one uploads a
	 * pdf through the upload dialog.
	 */
	String PRIMARY_CONTENT = "primaryContent";
	/**
	 * Content purpose for a preview of {@link #PRIMARY_CONTENT}.
	 */
	String PRIMARY_CONTENT_PREVIEW = "primaryContentPreview";

	/**
	 * Gets the content that need to be stored/uploaded/assigned to the instance.
	 *
	 * @return the content descriptor to store
	 */
	FileDescriptor getContent();

	/**
	 * Sets the content that should be stored. The content will be retrieved using
	 * {@link FileDescriptor#getInputStream()} method.
	 *
	 * @param content
	 *            the new content
	 * @return the same instance to allow method chaining
	 */
	Content setContent(FileDescriptor content);

	/**
	 * Alternative method for setting the content returned by the method {@link #getContent()}. The method may
	 * automatically set the content length based on the data bytes count based on the given charset. Also the charset
	 * field will also may be set based on the provided argument.
	 *
	 * @param content
	 *            the new content
	 * @param charset
	 *            the charset to use. If null is provided the default value UTF-8 will be used
	 * @return the same instance to allow method chaining
	 */
	default Content setContent(String content, String charset) {
		Charset dataCharset = StringUtils.isBlank(charset) ? StandardCharsets.UTF_8 : Charset.forName(charset);
		return setContent(content, dataCharset);
	}

	/**
	 * Alternative method for setting the content returned by the method {@link #getContent()}. The method may
	 * automatically set the content length based on the data bytes count based on the given charset. Also the charset
	 * field will also may be set based on the provided argument.
	 * <p>
	 * The default implementation uses {@link StandardCharsets#UTF_8} as default charset if one is not provided and a
	 * {@link FileDescriptor#create(Supplier, Supplier, long)} to instantiate a descriptor from the given text content.
	 *
	 * @param content
	 *            the new content
	 * @param charset
	 *            the charset
	 * @return the same instance to allow method chaining
	 */
	default Content setContent(String content, Charset charset) {
		Objects.requireNonNull(content, "Cannot store null content");

		Charset dataCharset = charset == null ? StandardCharsets.UTF_8 : charset;
		setCharset(dataCharset.name());
		return setContent(content.getBytes(dataCharset));
	}

	/**
	 * Sets a content in the form of byte array.
	 *
	 * @param data
	 *            the data to store. Required value.
	 * @return the same instance to allow method chaining
	 */
	default Content setContent(byte[] data) {
		Objects.requireNonNull(data, "Cannot accept null byte[] data!");
		setContentLength(Long.valueOf(data.length));
		return setContent(FileDescriptor.create(this::getName, () -> new ByteArrayInputStream(data), data.length));
	}

	/**
	 * Sets the content to be the given local {@link File}. Note that the file is not required to exists when this
	 * method is called but it's required when save is called for the constructed {@link Content} instance.
	 *
	 * @param file
	 *            the file to set as content. Required value.
	 * @return the same instance to allow method chaining
	 */
	default Content setContent(File file) {
		Objects.requireNonNull(file, "Cannot accept null java.io.File!");
		if (file.exists()) {
			setContentLength(Long.valueOf(file.length()));
		}
		return setContent(FileDescriptor.create(this::getName, () -> {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new EmfRuntimeException("Could not load file", e);
			}
		}, file.length()));
	}

	/**
	 * Gets the purpose of the content relative to the instance that this content is being persisted. If
	 * <code>null</code> some default value may be used.
	 *
	 * @return the purpose
	 */
	String getPurpose();

	/**
	 * Sets the purpose of the current content relative to the assign instance. If not set a some default value may be
	 * used.
	 *
	 * @param purpose
	 *            the purpose of the content
	 * @return the same instance to allow method chaining
	 */
	Content setPurpose(String purpose);

	/**
	 * Gets the name of the content/file being stored. If this method returns <code>null</code> then the name will be
	 * taken from {@link FileDescriptor#getId()} from the {@link #getContent()}.
	 *
	 * @return the name of the content/file. This name will be used when downloading the file to the user.
	 */
	String getName();

	/**
	 * Sets custom name. The name could be different by the one returned from {@link FileDescriptor#getId()} returned by
	 * the {@link #getContent()}. Later when the content is retrieved this name will be reported.
	 *
	 * @param name
	 *            the new name of the content/file
	 * @return the same instance to allow method chaining
	 */
	Content setName(String name);

	/**
	 * Gets the mime type of the provided content.
	 *
	 * @return the mime type
	 */
	String getMimeType();

	/**
	 * Sets the mime type of the saved content. If the content is not known it may be resolved later.
	 *
	 * @param mimeType
	 *            the new mime type
	 * @return the same instance to allow method chaining
	 */
	Content setMimeType(String mimeType);

	/**
	 * Gets the content length. This is optional and may be used for some optimizations during persisting.
	 *
	 * @return the same instance to allow method chaining length or <code>null</code> if unknown.
	 */
	Long getContentLength();

	/**
	 * Sets the content length in bytes. The content length should be provided if known.
	 *
	 * @param length
	 *            the content length in bytes
	 * @return the same instance to allow method chaining
	 */
	Content setContentLength(Long length);

	/**
	 * Gets the charset of the stored content if the content is character representable. This method may return
	 * <code>null</code> if the content is non directly convertible to binary to text or if it's unknown.
	 *
	 * @return the charset or <code>null</code> if not applicable or unknown.
	 */
	String getCharset();

	/**
	 * Sets the charset of the stored content. This field is applicable if the content is text representable. If the
	 * content is undetermined binary content this field may be <code>null</code>.
	 *
	 * @param charset
	 *            the new charset
	 * @return the same instance to allow method chaining
	 */
	Content setCharset(String charset);

	/**
	 * Gets the additional properties that should be should be passed to the service for persisting.
	 *
	 * @return the properties, may be <code>null</code> if no properties are set.
	 */
	Map<String, Serializable> getProperties();

	/**
	 * Additional properties that should be passed to the service for persisting.
	 *
	 * @param properties
	 *            the properties
	 * @return the same instance to allow method chaining
	 */
	Content setProperties(Map<String, Serializable> properties);

	/**
	 * Checks if the current content represents an instance view.
	 *
	 * @return true, if is view
	 */
	boolean isView();

	/**
	 * Marks the current content as instance view. Instance views are may be processed differently than the other
	 * content and depends on the implementation of the content persisting.
	 *
	 * @param view
	 *            the new view
	 * @return the same instance to allow method chaining
	 */
	Content setView(boolean view);

	/**
	 * Checks if new version should be created for the current content or the old one should be override.
	 *
	 * @return <code>true</code> if new version should be created, <code>false</code> otherwise
	 */
	boolean isVersionable();

	/**
	 * Marks the current content for versioning. If this is set to <code>true</code> new version of the content will be
	 * created, if there is one already. Primary used in instance content versioning.
	 *
	 * @param versionable
	 *            <code>true</code> if new version should be created, <code>false</code> if the old one should be
	 *            override
	 * @return same content instance to allow method chaining
	 */
	Content setVersionable(boolean versionable);

	/**
	 * Checks if indexing is enabled for this content.
	 *
	 * @return true, if is indexable
	 */
	boolean isIndexable();

	/**
	 * Enables/disables indexing of the data represented by the current content.
	 *
	 * @param indexable
	 *            the indexable
	 * @return same content instance to allow method chaining
	 */
	Content setIndexable(boolean indexable);

	/**
	 * Returns if mime type should be detected from the content even if we have set type in the content.
	 *
	 * @return true to explicitly detect from content.
	 */
	boolean shouldDetectedMimeTypeFromContent();

	/**
	 * Sets if mime type should be detected from the content of the file. At the moment the mime type relies on the file
	 * parts that are sent from the browser when doing multipart request. However if we for example rename a .pdf file
	 * to .xls incorrect content type will be saved. If this is set to true we use Apache Tika library to detect the
	 * mime type from the content directly and ignore the type related file parts from the browser.
	 *
	 * @param isDetectedFromContent
	 *            true to explicitly detect from the content even if we have set type in the content.
	 * @return same content instance to allow method chaining
	 */
	Content setDetectedMimeTypeFromContent(boolean isDetectedFromContent);

	/**
	 * Checks if content reuse is allowed.
	 *
	 * @return <code>true</code> if the implementation should try to reuse the content on future saves
	 */
	boolean isReuseAllowed();

	/**
	 * Marks the content to be allowed to be reused by other requests. This means that if the same content is already
	 * uploaded it will be used and this content may not be stored at all.
	 * <p>
	 * This makes sure the saved content is only stored once. <br>
	 * Note that this option may involve digesting the provided content in order to determine it's uniqueness. This may
	 * hinder the upload/save performance. Also the provided content should allow to be accessed multiple times.
	 * <p>
	 * Note that content reuse is discouraged for content that will be updated in the future without versioning
	 *
	 * @return same content instance to allow method chaining
	 */
	Content allowReuse();

	/**
	 * Retrieves the id of the content that will be saved. If the content is not saved and the id is not set, this
	 * method will return <code>null</code>.
	 *
	 * @return the id of the content or <code>null</code> if the id is not set and the content is not saved yet
	 */
	String getContentId();

	/**
	 * Sets the content id for the content that will be stored. It could be used to preset content id, if the one needs
	 * to know what would it be, before the actual content save. By default it will be generated, if not set. <br>
	 * Note that two content with same content id are not allowed and the system will throw error, if such case happens.
	 *
	 * @param id
	 *            the id of the content
	 * @return same content instance to allow method chaining
	 */
	Content setContentId(String id);

	/**
	 * Checks if the previous content store is enforced.
	 *
	 * @return true if store is enforced.
	 */
	boolean isContentStoreEnforcedOnVersionUpdate();

	/**
	 * By default, during update of content the original content store is enforced. This means that the new content will
	 * be stored to the store where the previous content is stored. This option will disable this behaviour and the
	 * newly stored content will have it's destination recalculated.
	 *
	 * @return the current instance for method chaining
	 */
	Content disableContentStoreEnforcingOnVersionUpdate();

	/**
	 * Creates an empty {@link Content} implementation. The default implementation supports setting all fields.
	 *
	 * @return writable content instance
	 */
	static Content createEmpty() {
		return new ContentData();
	}

	/**
	 * Creates {@link Content} instance and populates the required fields name and content of the returned
	 * {@link Content} instance. All other fields may be populated if needed and if data is available.
	 *
	 * @param name
	 *            the name of the file that is represented by the given descriptor/content
	 * @param descriptor
	 *            the descriptor that can be used for actual content access.
	 * @return writable content instance
	 */
	static Content create(String name, FileDescriptor descriptor) {
		ContentData data = new ContentData();
		data.setName(name);
		data.setContent(descriptor);
		return data;
	}

	/**
	 * Creates new {@link Content} by using the {@link #createEmpty()} and copy the properties from the given
	 * {@link ContentInfo}. The only properties not copied are the actual content. This method is useful when modifying
	 * existing content
	 *
	 * @param info
	 *            the info to copy from
	 * @return the content instance with will information from the given info
	 */
	static Content createFrom(ContentInfo info) {
		Objects.requireNonNull(info, "ContentInfo is required argument.");
		return createEmpty()
				.setMimeType(info.getMimeType())
					.setName(info.getName())
					.setPurpose(info.getContentPurpose())
					.setView(info.isView())
					.setCharset(info.getCharset())
					.setIndexable(info.isIndexable());
	}

	/**
	 * Basic implementation of {@link Content}. The class stores the and reports the values provided by the set methods.
	 *
	 * @author BBonev
	 */
	class ContentData implements Content {

		private static final long serialVersionUID = 149109860624495696L;

		private Map<String, Serializable> properties;
		private String charset;
		private Long length;
		private String mimeType;
		private String name;
		private String purpose;
		private FileDescriptor content;
		private boolean view;
		private boolean versionable;
		private boolean indexable;
		private boolean allowReuse = false;
		private boolean isMimetypeDetectedFromContent;
		private String contentId;
		private boolean contentStoreEnforcedOnVersionUpdate = true;

		@Override
		public FileDescriptor getContent() {
			return content;
		}

		@Override
		public Content setContent(FileDescriptor content) {
			this.content = content;
			return this;
		}

		@Override
		public String getPurpose() {
			return purpose;
		}

		@Override
		public Content setPurpose(String purpose) {
			this.purpose = purpose;
			return this;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Content setName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public String getMimeType() {
			return mimeType;
		}

		@Override
		public Content setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		@Override
		public Long getContentLength() {
			return length;
		}

		@Override
		public Content setContentLength(Long length) {
			this.length = length;
			return this;
		}

		@Override
		public String getCharset() {
			return charset;
		}

		@Override
		public Content setCharset(String charset) {
			this.charset = charset;
			return this;
		}

		@Override
		public Map<String, Serializable> getProperties() {
			return properties;
		}

		@Override
		public Content setProperties(Map<String, Serializable> properties) {
			this.properties = properties;
			return this;
		}

		@Override
		public boolean isView() {
			return view;
		}

		@Override
		public Content setView(boolean view) {
			this.view = view;
			return this;
		}

		@Override
		public boolean isVersionable() {
			return versionable;
		}

		@Override
		public Content setVersionable(boolean versionable) {
			this.versionable = versionable;
			return this;
		}

		@Override
		public boolean isIndexable() {
			return indexable;
		}

		@Override
		public Content setDetectedMimeTypeFromContent(boolean isDetectedFromContent) {
			isMimetypeDetectedFromContent = isDetectedFromContent;
			return this;
		}

		@Override
		public boolean shouldDetectedMimeTypeFromContent() {
			return isMimetypeDetectedFromContent;
		}

		@Override
		public Content setIndexable(boolean indexable) {
			this.indexable = indexable;
			return this;
		}

		@Override
		public boolean isReuseAllowed() {
			return allowReuse;
		}

		@Override
		public Content allowReuse() {
			allowReuse = true;
			return this;
		}

		@Override
		public String getContentId() {
			return contentId;
		}

		@Override
		public Content setContentId(String id) {
			contentId = id;
			return this;
		}

		@Override
		public boolean isContentStoreEnforcedOnVersionUpdate() {
			return contentStoreEnforcedOnVersionUpdate;
		}

		@Override
		public Content disableContentStoreEnforcingOnVersionUpdate() {
			contentStoreEnforcedOnVersionUpdate = false;
			return this;
		}
	}

}
