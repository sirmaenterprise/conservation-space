package com.sirma.sep.content.preview;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Utilities related to testing the {@link ContentPreviewApplication}
 *
 * @author Mihail Radkov
 */
public class TestFileUtils {

	/**
	 * Produces temporary files which will be deleted after JVM termination.
	 *
	 * @return temporary {@link File}
	 * @throws IOException
	 * 		in case the file can't be created
	 */
	public static File getTempFile() throws IOException {
		File tempFile = File.createTempFile(UUID.randomUUID().toString(), null);
		tempFile.deleteOnExit();
		return tempFile;
	}

	public static File getSystemTempDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
}
