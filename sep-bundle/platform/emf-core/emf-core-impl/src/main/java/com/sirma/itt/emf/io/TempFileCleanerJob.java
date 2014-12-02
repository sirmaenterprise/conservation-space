package com.sirma.itt.emf.io;

import java.io.File;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.patch.PatchDbService;

/**
 * Cleans up <b>all</b> temporary files that are older than the given number of hours.
 * Subdirectories are emptied as well and all directories below the primary temporary subdirectory
 * are removed.
 * <p>
 * The job configuration can include a property
 * <tt>{@link ConfigProperties#TEMP_DIR_PROTECT_HOURS}</tt>, which is the number of hours to protect
 * a temporary file from deletion since its last modification.
 *
 * @author Derek Hulley
 * @author BBonev
 */
@Singleton
@Startup
@Lock(LockType.READ)
@DependsOn(value = PatchDbService.SERVICE_NAME)
@TransactionManagement(TransactionManagementType.BEAN)
public class TempFileCleanerJob {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TempFileCleanerJob.class);

	/** The temp file provider. */
	@Inject
	private TempFileProvider tempFileProviderImpl;

	/** The protect hours. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR_PROTECT_HOURS, defaultValue = "24")
	private Integer protectHours;

	/**
	 * Gets a list of all files in the {@link TempFileProviderImpl#LONG_LIFE_FILE_DIR temp
	 * directory} and deletes all those that are older than the given number of hours.
	 */
	@Schedule(second = "0", minute = "*/30", hour = "*", info = "Every 30 minutes to check fo files to clean", persistent = false)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void execute() {
		// get the number of hours to protect the temp files
		if ((protectHours < 0) || (protectHours > 8760)) {
			throw new EmfConfigurationException("Hours to protect temp files ("
					+ EmfConfigurationProperties.TEMP_DIR_PROTECT_HOURS
					+ ") must be 0 <= x <= 8760");
		}

		long now = System.currentTimeMillis();
		long aFewHoursBack = now - (3600L * 1000L * protectHours);

		long aLongTimeBack = now - (24 * 3600L * 1000L);

		File tempDir = tempFileProviderImpl.getTempDir();
		// don't delete this directory
		int count = removeFiles(tempDir, aFewHoursBack, aLongTimeBack, false);
		// done
		LOGGER.debug("Removed {} files from temp directory: {}", count, tempDir);
	}

	/**
	 * Removes all temporary files created before the given time.
	 * <p>
	 * The delete will cascade down through directories as well.
	 *
	 * @param removeBefore
	 *            only remove files created <b>before</b> this time
	 * @return Returns the number of files removed
	 */
	public int removeFiles(long removeBefore) {
		File tempDir = tempFileProviderImpl.getTempDir();
		return removeFiles(tempDir, removeBefore, removeBefore, false);
	}

	/**
	 * Removes the files.
	 *
	 * @param directory
	 *            the directory to clean out - the directory will optionally be removed
	 * @param removeBefore
	 *            only remove files created <b>before</b> this time
	 * @param longLifeBefore
	 *            the long life before
	 * @param removeDir
	 *            true if the directory must be removed as well, otherwise false
	 * @return Returns the number of files removed
	 */
	private int removeFiles(File directory, long removeBefore, long longLifeBefore,
			boolean removeDir) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory to clear: " + directory);
		}
		// check if there is anything to to
		if (!directory.exists()) {
			return 0;
		}
		// list all files
		File[] files = directory.listFiles();
		int count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				if (tempFileProviderImpl.isLongLifeTempDir(file)) {
					// long life for this folder and its children
					removeFiles(file, longLifeBefore, longLifeBefore, true);
				} else {
					// enter subdirectory and clean it out and remove itsynetics
					removeFiles(file, removeBefore, longLifeBefore, true);
				}
			} else {
				// it is a file - check the created time
				if (file.lastModified() > removeBefore) {
					// file is not old enough
					continue;
				}
				// it is a file - attempt a delete
				try {
					LOGGER.debug("Deleting temp file: " + file);
					if (file.delete()) {
						count++;
					}
				} catch (Exception e) {
					LOGGER.info("Failed to remove temp file: " + file);
				}
			}
		}
		// must we delete the directory we are in?
		if (removeDir) {
			// the directory must be removed if empty
			try {
				File[] listing = directory.listFiles();
				if ((listing != null) && (listing.length == 0)) {
					// directory is empty
					directory.delete();
				}
			} catch (Exception e) {
				LOGGER.info("Failed to remove temp directory: " + directory, e);
			}
		}
		// done
		return count;
	}
}