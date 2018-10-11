package com.sirma.sep.content.temp;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.security.context.SecurityContext;

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
@ApplicationScoped
public class TempFileProviderImpl implements TempFileProvider {
	/**
	 * subdirectory in the temp directory where SEIP temporary files will go.
	 */
	public static final String TEMP_FILE_DIR = "SEIP";

	/**
	 * The prefix for the long life temporary files.
	 */
	public static final String LONG_LIFE_FILE_DIR = "longLife";

	/** The system property key giving us the location of the temp directory. */
	public static final String SYSTEM_KEY_TEMP_DIR = "java.io.tmpdir";

	private static final Logger LOGGER = LoggerFactory.getLogger(TempFileProviderImpl.class);

	private static final int MAX_RETRIES = 3;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "temp.dir", sensitive = true, type = File.class, label = "Temporary directory path. If not specified or points to invalid folder or folder with no write access then the server will use a directory as %java.io.tmpdir%/EMF")
	private ConfigurationProperty<File> tempDir;

	@ConfigurationConverter("temp.dir")
	static File buildTempDir(ConverterContext converterContext, SecurityContext securityContext) {
		String path = converterContext.getRawValue();
		if (StringUtils.trimToNull(path) == null) {
			String systemTempDirPath = System.getProperty(SYSTEM_KEY_TEMP_DIR);
			if (systemTempDirPath == null) {
				throw new EmfRuntimeException("System property not available: " + SYSTEM_KEY_TEMP_DIR);
			}
			path = systemTempDirPath;
		}
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		path += securityContext.getCurrentTenantId();
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			file = new File(file.getAbsolutePath() + File.separator + UUID.randomUUID());
		}
		if (file.mkdirs()) {
			LOGGER.debug("Created temp folder: {}", file);
		}
		return file;
	}

	@Override
	public File getSystemTempDir() {
		return tempDir.get();
	}

	@Override
	public File getTempDir() {
		File systemTemp = getSystemTempDir();
		// append the SEIP directory
		File temp = new File(systemTemp, TEMP_FILE_DIR);
		// ensure that the temp directory exists
		if (temp.exists()) {
			// nothing to do
		} else {
			// not there yet
			if (!temp.mkdirs()) {
				throw new EmfRuntimeException("Failed to create temp directory: " + temp);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Created temp directory: {}", temp);
			}
		}
		// done
		return temp;
	}

	@Override
	public File createLongLifeTempDir(String key) {
		/**
		 * Long life temporary directories have a prefix at the start of the folder name.
		 */
		String folderName = LONG_LIFE_FILE_DIR + "_" + key;

		// append the SEIP directory
		File longLifeDir = new File(getTempDir(), folderName);
		// ensure that the temp directory exists

		if (longLifeDir.exists()) {
			LOGGER.debug("Already exists: {}", longLifeDir);
			// nothing to do
			return longLifeDir;
		}
		/**
		 * We need to create a temporary directory We may have a race condition here if more than one thread attempts to
		 * create the temp dir. mkdirs can't be synchronized See
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4742723
		 */
		for (int retry = 0; retry < MAX_RETRIES; retry++) {
			if (longLifeDir.mkdirs()) {
				// Yes we created the temp dir
				LOGGER.debug("Created long life temp directory: {}", longLifeDir);
				return longLifeDir;
			}
			if (longLifeDir.exists()) {
				// created by another thread, but that's O.K.
				LOGGER.debug("Another thread created long life temp directory: {}", longLifeDir);
				return longLifeDir;
			}
		}
		throw new EmfRuntimeException("Failed to create temp directory: " + longLifeDir);
	}

	@Override
	public boolean isLongLifeTempDir(File file) {
		return file.isDirectory() && file.getName().startsWith(LONG_LIFE_FILE_DIR)
				&& file.getParentFile().equals(getTempDir());
	}

	@Override
	public File createTempFile(String prefix, String suffix) {
		File temp = getTempDir();
		// we have the directory we want to use
		return createTempFile(prefix, suffix, temp);
	}

	@Override
	public File createTempFile(String prefix, String suffix, File directory) {
		try {
			// Check out File.createTempFile javadoc. prefixes shorter than 3 symbols are not
			// allowed for some reason so we append _temp to them.
			if (prefix.length() < 3) {
				LOGGER.warn(
						"Trying to create temp file with file name shorter than 3 symbols. Will append more symbols so File.createTempFile doesn't throw an exception.");
				return File.createTempFile(prefix + "_temp", suffix, directory);
			}
			return File.createTempFile(prefix, suffix, directory);
		} catch (IOException e) {
			throw new EmfRuntimeException("Failed to created temp file: \n" + "   prefix: " + prefix + "\n"
					+ "   suffix: " + suffix + "\n" + "   directory: " + directory, e);
		}
	}

	@Override
	public File createTempDir(String dirName) {
		File file = new File(getTempDir(), dirName);
		if (file.mkdirs()) {
			return file;
		}
		if (file.isDirectory()) {
			deleteChild(file);
			// if the directory is clean
			String[] list = file.list();
			return list != null && list.length == 0 ? file : null;
		}
		return null;
	}

	private void deleteChild(File file) {
		File[] listFiles = file.listFiles();
		if (listFiles == null) {
			return;
		}
		for (File child : listFiles) {
			deleteFile(child);
		}
	}

	@Override
	public void deleteFile(File tempFile) {
		if (tempFile != null) {
			if (!tempFile.delete() && tempFile.isDirectory()) {
				deleteChild(tempFile);
				if (!tempFile.delete()) {
					LOGGER.info("Could not delete folder: " + tempFile.getAbsolutePath());
				}
			}
			if (tempFile.canRead()) {
				tempFile.deleteOnExit();
			}
		}
	}

}
