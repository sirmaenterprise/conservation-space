package com.sirma.itt.seip.eai.content.tool.service;

import static com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants.CONTENT_SOURCE;
import static com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants.PRIMARY_CONTENT_ID;

import java.io.File;
import java.util.Objects;

import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetEntry;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetEntryId;

/**
 * The {@link ContentEntry} is wrapper for uploadble content based on spreadsheet entry. {@link #equals(Object)} and
 * {@link #hashCode()} are overridden based on {@link #getSourceId()} {@link #equals(Object)} and {@link #hashCode()}
 */
class ContentEntry {
	private String contentId;
	private SpreadsheetEntry source;
	private File contentSource;
	private File contentName;

	/**
	 * Instantiates a new upload content entry.
	 *
	 * @param source
	 *            the source entry
	 */
	public ContentEntry(SpreadsheetEntry source) {
		Objects.requireNonNull(source, "Missing source value for uploadble content!");
		Objects.requireNonNull(source.getId(), "Invalid uploadble content id!");
		this.source = source;
		contentId = getContentId(source);
		contentName = getPrimaryContentFile(source);
	}

	private static File getPrimaryContentFile(SpreadsheetEntry sheet) {
		String fileName = (String) sheet.getProperties().getOrDefault(CONTENT_SOURCE, null);
		return fileName != null ? new File(fileName) : null;
	}

	private static String getContentId(SpreadsheetEntry sheet) {
		return (String) sheet.getProperties().getOrDefault(PRIMARY_CONTENT_ID, null);
	}

	/**
	 * Checks if is existing in remote system.
	 *
	 * @return true, if is existing
	 */
	boolean isExisting() {
		return contentId != null && !contentId.trim().isEmpty();
	}

	/**
	 * Checks if is source is uploadable - is it contains valid primary content
	 *
	 * @return true, if it is uploadable
	 */
	boolean isUploadable() {
		return contentSource != null && contentSource.canRead();
	}

	/**
	 * Gets the primary content - the actual file that would be uploaded.
	 *
	 * @return the primary content
	 */
	File getContentSource() {
		return contentSource;
	}

	/**
	 * Sets the primary content - the actual file that would be uploaded.
	 *
	 * @param selected
	 *            the new primary content
	 */
	public void setPrimaryContent(File selected) {
		this.contentSource = selected;
	}

	/**
	 * The original file request
	 * 
	 * @return the file as specified in the spreadsheet
	 */
	public File getContentName() {
		return contentName;
	}

	/**
	 * Checks if is source is uploadable - is it contains set primary content
	 *
	 * @return true, if it is uploadable
	 */
	boolean hasContent() {
		return contentName != null;
	}

	/**
	 * Gets the content id.
	 *
	 * @return the content id
	 */
	String getContentId() {
		return contentId;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	SpreadsheetEntry getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source
	 *            the new source
	 */
	void setSource(SpreadsheetEntry source) {
		this.source = source;
	}

	/**
	 * Gets the source id.
	 *
	 * @return the source id
	 */
	SpreadsheetEntryId getSourceId() {
		return source.getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getSourceId() == null) ? 0 : getSourceId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContentEntry other = (ContentEntry) obj;
		return Objects.equals(getSourceId(), other.getSourceId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContentEntry [").append(source).append("]");
		return builder.toString();
	}

}