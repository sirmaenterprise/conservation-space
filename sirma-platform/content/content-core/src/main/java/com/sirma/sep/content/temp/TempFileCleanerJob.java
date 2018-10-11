package com.sirma.sep.content.temp;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.Schedule;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Cleans up <b>all</b> temporary files that are older than the given number of hours. Subdirectories are emptied as
 * well and all directories below the primary temporary subdirectory are removed.
 * <p>
 * The job configuration can include a property <tt>{@link ConfigProperties#TEMP_DIR_PROTECT_HOURS}</tt>, which is the
 * number of hours to protect a temporary file from deletion since its last modification.
 *
 * @author Derek Hulley
 * @author BBonev
 */
@Singleton
public class TempFileCleanerJob {

	private static final String TEMP_DIR_PROTECT_HOURS = "temp.dir.protectHours";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TempFileCleanerJob.class);

	/** The temp file provider. */
	@Inject
	private TempFileProvider tempFileProvider;

	/** The protect hours. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = TEMP_DIR_PROTECT_HOURS, defaultValue = "24", sensitive = true, type = Integer.class, label = "The number of hours to keep the files into the system temporary folder.")
	private ConfigurationProperty<Integer> protectHours;

	/**
	 * Gets a list of all files in the {@link TempFileProviderImpl#LONG_LIFE_FILE_DIR temp directory} and deletes all
	 * those that are older than the given number of hours.
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, async = true)
	@RunAsAllTenantAdmins
	@Schedule(identifier = "TempFileCleanerJob", transactionMode = TransactionMode.NOT_SUPPORTED, system = false)
	@ConfigurationPropertyDefinition(name = "temp.dir.cleaner.schedule", defaultValue = "0 0/30 * ? * *", system = true, sensitive = true, label = "Cron like expression for interval of temp file cleanup.")
	public void execute() {
		// get the number of hours to protect the temp files
		Integer property = protectHours.get();
		if (property == null || property < 0 || property > 8760) {
			throw new EmfConfigurationException(
					"Hours to protect temp files (" + TEMP_DIR_PROTECT_HOURS + ") must be 0 <= x <= 8760");
		}

		long now = System.currentTimeMillis();
		long aFewHoursBack = now - 3600L * 1000L * property;

		long aLongTimeBack = now - 24 * 3600L * 1000L;

		File tempDir = tempFileProvider.getTempDir();
		// don't delete this directory
		int count = removeFiles(tempDir, aFewHoursBack, aLongTimeBack, false);
		// done
		if (count > 0) {
			LOGGER.debug("Cleaning removed {} files from temp directory: {}", count, tempDir);
		}
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
		File tempDir = tempFileProvider.getTempDir();
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
	private int removeFiles(File directory, long removeBefore, long longLifeBefore, boolean removeDir) {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory to clear: " + directory);
		}
		// check if there is anything to to
		if (!directory.exists()) {
			return 0;
		}
		// list all files
		File[] files = directory.listFiles();
		if (files == null) {
			// not a directory - we have a check above but the code analysis reports an error
			return 0;
		}
		int count = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				removeDirectory(removeBefore, longLifeBefore, file);
			} else {
				count = removeFile(removeBefore, count, file);
			}
		}
		// must we delete the directory we are in?
		if (removeDir) {
			cleanEmptyDirectory(directory);
		}
		// done
		return count;
	}

	/**
	 * Clean empty directory.
	 *
	 * @param directory
	 *            the directory
	 */
	private static void cleanEmptyDirectory(File directory) {
		// the directory must be removed if empty
		try {
			File[] listing = directory.listFiles();
			if (listing != null && listing.length == 0 && !directory.delete()) {
				LOGGER.warn("Could not delete folder {}", directory);
			}
		} catch (Exception e) {
			LOGGER.info("Failed to remove temp directory: " + directory, e);
		}
	}

	/**
	 * Removes the file.
	 *
	 * @param removeBefore
	 *            the remove before
	 * @param count
	 *            the count
	 * @param file
	 *            the file
	 * @return the int
	 */
	private static int removeFile(long removeBefore, int count, File file) {
		// it is a file - check the created time
		if (file.lastModified() > removeBefore) {
			// file is not old enough
			return count;
		}
		// it is a file - attempt a delete
		try {
			LOGGER.debug("Deleting temp file: " + file);
			if (file.delete()) {
				return count + 1;
			}
		} catch (Exception e) {
			LOGGER.info("Failed to remove temp file: {}", file);
			LOGGER.trace("Failed to remove temp file: {}", file, e);
		}
		return count;
	}

	/**
	 * Removes the directory.
	 *
	 * @param removeBefore
	 *            the remove before
	 * @param longLifeBefore
	 *            the long life before
	 * @param file
	 *            the file
	 */
	private void removeDirectory(long removeBefore, long longLifeBefore, File file) {
		if (tempFileProvider.isLongLifeTempDir(file)) {
			// long life for this folder and its children
			removeFiles(file, longLifeBefore, longLifeBefore, true);
		} else {
			// enter subdirectory and clean it out and remove itsynetics
			removeFiles(file, removeBefore, longLifeBefore, true);
		}
	}
}