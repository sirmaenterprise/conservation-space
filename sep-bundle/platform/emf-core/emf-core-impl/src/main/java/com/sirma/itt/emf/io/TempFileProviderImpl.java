/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.io;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * A helper class that provides temporary files, providing a common point to clean them up.
 * <p>
 * The contents of EMF [%java.io.tmpdir%/EMF] are managed by this class. Temporary files and
 * directories are cleaned by TempFileCleanerJob so that after a delay [default 1 hour] the contents
 * of the alfresco temp dir, both files and directories are removed.
 * <p>
 * Some temporary files may need to live longer than 1 hour. The temp file provider allows special
 * sub folders which are cleaned less frequently. By default, files in the long life folders will
 * remain for 24 hours unless cleaned by the application code earlier.
 * <p>
 * The other contents of %java.io.tmpdir% are not touched by the cleaner job.
 * <p>
 * TempFileCleanerJob Job Data: protectHours, number of hours to keep temporary files, default 1
 * hour.
 * <p>
 * 
 * @author derekh
 * @author mrogers
 */
@ApplicationScoped
public class TempFileProviderImpl implements TempFileProvider {
	/**
	 * subdirectory in the temp directory where Alfresco temporary files will
	 * go.
	 */
	public static final String CMF_TEMP_FILE_DIR = "EMF";

	/**
	 * The prefix for the long life temporary files.
	 */
	public static final String LONG_LIFE_FILE_DIR = "longLife";

	/** the system property key giving us the location of the temp directory. */
	public static final String SYSTEM_KEY_TEMP_DIR = "java.io.tmpdir";

	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TempFileProviderImpl.class);

	/** The max retries. */
	private static int MAX_RETRIES = 3;
	/** The temp dir. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR)
	private File tempDir;

	/** The system temp dir. */
	private File systemTempDir;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getSystemTempDir() {
		if ((tempDir != null) && tempDir.canWrite()) {
			return tempDir;
		}
		if (systemTempDir == null) {
			String systemTempDirPath = System.getProperty(SYSTEM_KEY_TEMP_DIR);
			if (systemTempDirPath == null) {
				throw new EmfRuntimeException("System property not available: "
						+ SYSTEM_KEY_TEMP_DIR);
			}
			systemTempDir = new File(systemTempDirPath);
		}
		return systemTempDir;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getTempDir() {
		File systemTemp = getSystemTempDir();
		// append the Alfresco directory
		File temp = new File(systemTemp, CMF_TEMP_FILE_DIR);
		// ensure that the temp directory exists
		if (temp.exists()) {
			// nothing to do
		} else {
			// not there yet
			if (!temp.mkdirs()) {
				throw new EmfRuntimeException("Failed to create temp directory: " + temp);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Created temp directory: " + temp);
			}
		}
		// done
		return temp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File createLongLifeTempDir(String key) {
		/**
		 * Long life temporary directories have a prefix at the start of the
		 * folder name.
		 */
		String folderName = LONG_LIFE_FILE_DIR + "_" + key;

		// append the Alfresco directory
		File longLifeDir = new File(getTempDir(), folderName);
		// ensure that the temp directory exists

		if (longLifeDir.exists()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Already exists: " + longLifeDir);
			}
			// nothing to do
			return longLifeDir;
		}
		/**
		 * We need to create a temporary directory We may have a race condition here if more than
		 * one thread attempts to create the temp dir. mkdirs can't be synchronized See
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4742723
		 */
		for (int retry = 0; retry < MAX_RETRIES; retry++) {
			boolean created = longLifeDir.mkdirs();

			if (created) {
				// Yes we created the temp dir
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Created long life temp directory: " + longLifeDir);
				}
				return longLifeDir;
			}
			if (longLifeDir.exists()) {
				// created by another thread, but that's O.K.
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Another thread created long life temp directory: " + longLifeDir);
				}
				return longLifeDir;
			}
		}
		throw new EmfRuntimeException("Failed to create temp directory: " + longLifeDir);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLongLifeTempDir(File file) {
		if (file.isDirectory() && file.getName().startsWith(LONG_LIFE_FILE_DIR)
				&& file.getParentFile().equals(getTempDir())) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File createTempFile(String prefix, String suffix) {
		File temp = getTempDir();
		// we have the directory we want to use
		return createTempFile(prefix, suffix, temp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File createTempFile(String prefix, String suffix, File directory) {
		try {
			return File.createTempFile(prefix, suffix, directory);
		} catch (IOException e) {
			throw new EmfRuntimeException("Failed to created temp file: \n" + "   prefix: "
					+ prefix + "\n" + "   suffix: " + suffix + "\n" + "   directory: " + directory,
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File createTempDir(String dirName) {
		File file = new File(getTempDir(), dirName);
		if (file.mkdirs()) {
			return file;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteFile(File tempFile) {
		if (tempFile != null) {
			if ((!tempFile.delete()) && (tempFile.isDirectory())) {
				File[] listFiles = tempFile.listFiles();
				for (File child : listFiles) {
					deleteFile(child);
				}
				tempFile.delete();
			}
			if (tempFile.canRead()) {
				tempFile.deleteOnExit();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtension(String name) {
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
