package com.sirma.sep.content;

import java.io.InputStream;
import java.io.Serializable;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Represents a information about a saved content in the system. Provides all known information about it and means to
 * read/access it.
 *
 * @author BBonev
 */
public interface ContentInfo extends FileDescriptor, Named {

	/**
	 * Constant that can be used by method to not return <code>null</code> but this instance that represents a non
	 * existing content.
	 */
	ContentInfo DO_NOT_EXIST = new NonExistingContent();

	/**
	 * Gets the content id. This is unique content identifier that can be used to fetch the content directly.
	 *
	 * @return the non <code>null</code> content id if the content exists
	 */
	String getContentId();

	/**
	 * Gets the instance id of the associated instance id or other object
	 *
	 * @return the non <code>null</code> instance id
	 */
	Serializable getInstanceId();

	/**
	 * Gets the content purpose related to the instance.
	 *
	 * @return the content purpose
	 */
	String getContentPurpose();

	/**
	 * Checks if the content described by this {@link ContentInfo} instance actually exists. If this method returns
	 * <code>false</code> all other methods may not return proper results
	 *
	 * @return true, if exists and can be read and <code>false</code> if not exists and cannot be read.
	 */
	boolean exists();

	/**
	 * Gets the content mime type if known.
	 *
	 * @return the mime type or <code>null</code> if not known
	 */
	String getMimeType();

	/**
	 * Gets the length of the content in bytes
	 *
	 * @return the length or -1 if the content length is not identified, yet.
	 */
	long getLength();

	@Override
	default long length() {
		return getLength();
	}
	/**
	 * Checks if is this content info represents a instance view or not. Note that if method {@link #exists()} returns
	 * <code>false</code> then this method result must not be considered as relevant.
	 *
	 * @return true, if is view
	 */
	boolean isView();

	/**
	 * Gets the content character encoding if applicable.
	 *
	 * @return the charset or <code>null</code> if not applicable.
	 */
	String getCharset();

	/**
	 * Gets the remote content id. This id is the one that identifies the content where it's actually stored.
	 *
	 * @return the remote id
	 */
	String getRemoteId();
	
	/**
	 * Gets the remote source name.
	 *
	 * @return the remote source name
	 */
	String getRemoteSourceName();
	
	/**
	 * Gets the checksum of the content if available.
	 * 
	 * @return the checksum
	 */
	String getChecksum();
	
	/**
	 * Gets an additional content metadata.
	 *
	 * @return the metadata
	 */
	ContentMetadata getMetadata();

	/**
	 * If the current content is applicable for indexing the method should return <code>true</code> otherwise should
	 * return <code>false</code>.
	 *
	 * @return true, if is indexable
	 */
	boolean isIndexable();

	/**
	 * Content is reusable when it's saved with enabled {@link Content#allowReuse()} option. This means that this file
	 * is enabled for reusing<br>
	 * If the content does not exist the returned value does not have any meaning!
	 *
	 * @return if the represented content is reusable
	 */
	boolean isReuseable();

	/**
	 * Represents unknown or non existing content. The method {@link ContentInfo#exists()} returns always
	 * <code>false</code> and all other return <code>null</code>.
	 *
	 * @author BBonev
	 */
	class NonExistingContent implements ContentInfo {

		private static final long serialVersionUID = 1976376784999148562L;

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public String getMimeType() {
			return null;
		}

		@Override
		public long getLength() {
			return -1;
		}

		@Override
		public void close() {
			// nothing to do
		}

		@Override
		public String getContentId() {
			return null;
		}

		@Override
		public Serializable getInstanceId() {
			return null;
		}

		@Override
		public String getContentPurpose() {
			return null;
		}

		@Override
		public boolean isView() {
			return false;
		}

		@Override
		public String getCharset() {
			return null;
		}

		@Override
		public String getRemoteId() {
			return null;
		}

		@Override
		public String getRemoteSourceName() {
			return null;
		}

		@Override
		public ContentMetadata getMetadata() {
			return ContentMetadata.NO_METADATA;
		}

		@Override
		public boolean isReuseable() {
			return false;
		}

		@Override
		public boolean isIndexable() {
			return false;
		}

		@Override
		public String getChecksum() {
			return null;
		}
	}
}
