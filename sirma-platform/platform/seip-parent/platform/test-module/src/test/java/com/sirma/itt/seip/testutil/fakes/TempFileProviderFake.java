package com.sirma.itt.seip.testutil.fakes;

import java.io.File;
import java.io.IOException;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;

/**
 * Mocks the implementation of {@link TempFileProviderImpl}, but instead of taking the temp dir from a system
 * configuration, it receives it from the constructor when used in tests
 */
public class TempFileProviderFake implements TempFileProvider {

	private final File tempDir;

	public TempFileProviderFake(File tempDir) {
		super();
		this.tempDir = tempDir;
	}

	@Override
	public File getSystemTempDir() {
		return tempDir;
	}

	@Override
	public File getTempDir() {
		return tempDir;
	}

	@Override
	public File createLongLifeTempDir(String key) {
		// Not needed
		return null;
	}

	@Override
	public boolean isLongLifeTempDir(File file) {
		// Not needed
		return false;
	}

	@Override
	public File createTempFile(String prefix, String suffix) {
		// Not needed
		return null;
	}

	@Override
	public File createTempFile(String prefix, String suffix, File directory) {
		try {
			if (prefix.length() < 3) {
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
			}
			if (tempFile.canRead()) {
				tempFile.deleteOnExit();
			}
		}
	}

}
