package com.sirma.itt.seip.io;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * A helper class that provides temporary files, providing a common point to clean them up.
 * <p>
 * The contents of SEIP [%java.io.tmpdir%/SEIP] are managed by this class. Temporary files and directories are cleaned
 * by TempFileCleanerJob so that after a delay [default 1 hour] the contents of the application temp dir, both files and
 * directories are removed.
 * <p>
 * Some temporary files may need to live longer than 1 hour. The temp file provider allows special sub folders which are
 * cleaned less frequently. By default, files in the long life folders will remain for 24 hours unless cleaned by the
 * application code earlier.
 * <p>
 * The other contents of %java.io.tmpdir% are not touched by the cleaner job.
 * <p>
 * TempFileCleanerJob Job Data: protectHours, number of hours to keep temporary files, default 1 hour.
 * <p>
 *
 * @author derekh
 * @author mrogers
 * @author BBonev
 */
public interface TempFileProvider {

	/**
	 * Get the Java Temp dir e.g. java.io.tempdir
	 *
	 * @return Returns the system temporary directory i.e. <code>isDir == true</code>
	 */
	File getSystemTempDir();

	/**
	 * Get the SEIP temp dir, by defaut %java.io.tempdir%/SEIP. Will create the temp dir on the fly if it does not
	 * already exist.
	 *
	 * @return Returns a temporary directory, i.e. <code>isDir == true</code>
	 */
	File getTempDir();

	/**
	 * Creates a longer living temp dir. Files within the longer living temp dir will not be garbage collected as soon
	 * as "normal" temporary files. By default long life temp files will live for for 24 hours rather than 1 hour.
	 * <p>
	 * Code using the longer life temporary files should be careful to clean up since abuse of this feature may result
	 * in out of memory/disk space errors.
	 *
	 * @param key
	 *            can be blank in which case the system will generate a folder to be used by all processes or can be
	 *            used to create a unique temporary folder name for a particular process. At the end of the process the
	 *            client can simply delete the entire temporary folder.
	 * @return the long life temporary directory
	 */
	File createLongLifeTempDir(String key);

	/**
	 * Is this a long life folder ?.
	 *
	 * @param file
	 *            the file
	 * @return true, this is a long life folder.
	 */
	boolean isLongLifeTempDir(File file);

	/**
	 * Create a temp file in the SEIP temp dir.
	 *
	 * @param prefix
	 *            the prefix
	 * @param suffix
	 *            the suffix
	 * @return Returns a temp <code>File</code> that will be located in the <b>SEIP</b> subdirectory of the default temp
	 *         directory
	 * @see File#createTempFile(java.lang.String, java.lang.String)
	 */
	File createTempFile(String prefix, String suffix);

	/**
	 * Creates the temp file.
	 *
	 * @param prefix
	 *            the prefix
	 * @param suffix
	 *            the suffix
	 * @param directory
	 *            the directory
	 * @return Returns a temp <code>File</code> that will be located in the given directory
	 * @see File#createTempFile(java.lang.String, java.lang.String)
	 */
	File createTempFile(String prefix, String suffix, File directory);

	/**
	 * Creates the temp dir with the specified name.
	 *
	 * @param dirName
	 *            the dir name for the new dir
	 * @return the created dir or null if none is created
	 */
	File createTempDir(String dirName);

	/**
	 * Creates the temp dir starting with the specified prefix and an unique suffix.
	 *
	 * @param prefix the prefix for the new dir name.
	 * @return the created dir or null if none is created.
	 */
	File createUniqueTempDir(String prefix);

	/**
	 * Creates child dir in the specified parent folder with the specified name.
	 * If the target folder exists it will be cleared before returning.
	 *
	 * @param parent the parent dir
	 * @param dirName the name of the new dir
	 * @return the created dir or null if none is created or can not be emptied
	 */
	File createSubDir(File parent, String dirName);

	/**
	 * Delete file by first try to remove it, and on fail, tries to remove all child first recursively.
	 *
	 * @param tempFile
	 *            the temp file
	 */
	void deleteFile(File tempFile);

	/**
	 * Gets the extension from given name. 'application.pdf' would return .pdf, 'my.application.pdf' would return the
	 * same. If no extension is available '.tmp' is returned. If name is null or empty, null is returned
	 *
	 * @param name
	 *            is the filename
	 * @return the extracted extension or null
	 */
	static String getExtension(String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		int lastDot = name.trim().lastIndexOf(".");
		if (lastDot > -1) {
			return name.trim().substring(lastDot);
		}

		return ".tmp";
	}

}