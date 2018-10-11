package com.sirma.itt.seip.eai.content.tool.service;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.eai.content.tool.model.ErrorBuilderProvider;

/**
 * The {@link UploadbleStatusReport} holds the information for new/modified files for upload, the set of missing
 * required files for upload and the set of unmodified files. All files are bound to {@link ContentEntry}
 *
 * @author bbanchev
 */
public class UploadbleStatusReport {
	private Map<ContentEntry, File> filesForUpload = new LinkedHashMap<>();
	private Map<ContentEntry, File> unmodifiedFiles = new LinkedHashMap<>();
	private Map<ContentEntry, File> missingFiles = new LinkedHashMap<>();
	private Map<ContentEntry, Set<File>> duplicatedFiles = new LinkedHashMap<>();
	private Map<File, List<ContentEntry>> duplicatedEntries = new LinkedHashMap<>();
	private List<ContentEntry> notSetContentFiles = new LinkedList<>();

	private List<ContentEntry> processed = Collections.emptyList();
	private ErrorBuilderProvider errorAppender = new ErrorBuilderProvider();
	private boolean incompleteImports;
	private long totalUploadSize = -1L;

	/**
	 * Gets the files for upload.
	 *
	 * @return the files for upload
	 */
	public synchronized Map<ContentEntry, File> getFilesForUpload() {
		return filesForUpload;
	}

	/**
	 * Gets the missing files.
	 *
	 * @return the missing files
	 */
	public synchronized Map<ContentEntry, File> getMissingFiles() {
		return missingFiles;
	}

	/**
	 * Gets the unmodified files.
	 *
	 * @return the unmodified files
	 */
	public synchronized Map<ContentEntry, File> getUnmodifiedFiles() {
		return unmodifiedFiles;
	}

	/**
	 * Gets the non set content files - with no content id.
	 *
	 * @return the unset with content id files
	 */
	public synchronized List<ContentEntry> getNotSetContentFiles() {
		return notSetContentFiles;
	}

	/**
	 * Sets the successfully processed entries.
	 *
	 * @param processed
	 *            is the list to set
	 */
	public synchronized void setProcessed(List<ContentEntry> processed) {
		this.processed = processed;
	}

	/**
	 * Gets the successfully processed entries.
	 *
	 * @return the entries
	 */
	public synchronized List<ContentEntry> getProcessed() {
		return processed;
	}

	/**
	 * Adds а file duplication - entry has more than 1 file as content.
	 *
	 * @param entry
	 *            the entry to set duplication for
	 * @param file
	 *            the new file as duplication
	 */
	public synchronized void addFileDuplication(ContentEntry entry, File file) {
		duplicatedFiles.computeIfAbsent(entry, e -> new HashSet<File>()).add(file);
	}

	/**
	 * Gets the file duplications - entries that have more than 1 file as content.
	 *
	 * @return the file duplications
	 */
	public synchronized Map<ContentEntry, Set<File>> getFileDuplications() {
		return duplicatedFiles;
	}

	/**
	 * Gets the content duplications - single file is requested for multiple entries.
	 *
	 * @return the content duplications
	 */
	public synchronized Map<File, List<ContentEntry>> getContentDuplications() {
		return duplicatedEntries;
	}

	/**
	 * Retrieves the error log appender wrapper.
	 * 
	 * @return the {@link ErrorBuilderProvider}
	 */
	public synchronized ErrorBuilderProvider getErrorAppender() {
		return errorAppender;
	}

	/**
	 * Sets the flag that indicates detected incomplete imports.
	 *
	 * @param incompleteImports
	 *            whether there is or there are no incomplete imports
	 */
	public void setIncompleteImports(boolean incompleteImports) {
		this.incompleteImports = incompleteImports;
	}

	/**
	 * Gets the flag that indicates detected incomplete imports.
	 *
	 * @return true if it has
	 */
	public boolean hasIncompleteImports() {
		return incompleteImports;
	}

	/**
	 * Get the total size of the content that need to be uploaded.
	 *
	 * @return size in bytes
	 */
	public synchronized long getTotalUploadSize() {
		if (totalUploadSize < 0) {
			totalUploadSize = getFilesForUpload().values().stream().mapToLong(File::length).sum();
		}
		return totalUploadSize;
	}

}
