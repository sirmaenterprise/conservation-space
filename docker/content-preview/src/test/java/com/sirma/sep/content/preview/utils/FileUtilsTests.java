package com.sirma.sep.content.preview.utils;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.UUID;

/**
 * Tests utility logic in {@link com.sirma.sep.content.preview.util.FileUtils}
 *
 * @author Mihail Radkov
 */
public class FileUtilsTests {

	@Test
	public void deleteFile_shouldSafelyDeleteFiles() throws IOException {
		FileUtils.deleteFile(null);

		File missingFile = new File("");
		FileUtils.deleteFile(missingFile);

		File existingFile = TestFileUtils.getTempFile();
		FileUtils.deleteFile(existingFile);
		Assert.assertFalse(existingFile.exists());

		File mockedFile = Mockito.mock(File.class);
		Mockito.when(mockedFile.getPath()).thenThrow(new InvalidPathException("", ""));
		FileUtils.deleteFile(mockedFile);
	}

	@Test
	public void createDirectory_shouldCreateIt() {
		File systemTempDir = TestFileUtils.getSystemTempDir();
		String uniqueDirectoryPath = systemTempDir.getPath() + "/" + UUID.randomUUID().toString();
		File uniqueDirectory = new File(uniqueDirectoryPath);
		try {
			Assert.assertFalse(uniqueDirectory.exists());
			FileUtils.createDirectory(uniqueDirectory);
			Assert.assertTrue(uniqueDirectory.exists());
		} finally {
			FileUtils.deleteFile(uniqueDirectory);
		}
	}

	@Test
	public void withoutExtension_shouldStripFileExtensions() {
		Assert.assertEquals("", FileUtils.withoutExtension(""));
		Assert.assertEquals("file", FileUtils.withoutExtension("file"));
		Assert.assertEquals("/tmp/file", FileUtils.withoutExtension("/tmp/file"));
		Assert.assertEquals("/tmp/file", FileUtils.withoutExtension("/tmp/file.doc"));
	}
}
