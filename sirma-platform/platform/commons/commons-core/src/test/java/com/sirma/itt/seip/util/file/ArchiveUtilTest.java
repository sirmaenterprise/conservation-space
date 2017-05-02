package com.sirma.itt.seip.util.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Tests for {@link ArchiveUtil}.
 *
 * @author nvelkov
 */
public class ArchiveUtilTest {

	/**
	 * Test the compressing functionality of {@link ArchiveUtil} with a directory.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception.
	 * @throws ZipException
	 *             the zip exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testZipDirectory() throws URISyntaxException, ZipException, IOException {
		File file = new File(this.getClass().getResource("/folder").toURI());
		File outputFile = new File(this.getClass().getResource("/testOutput.zip").toURI());

		ArchiveUtil.zipFile(file, outputFile);

		try (ZipFile compressedFile = new ZipFile(outputFile)) {
			Assert.assertTrue(compressedFile.getEntry("file") != null);
		}
	}

	/**
	 * Test the compressing functionality of {@link ArchiveUtil} with a file.
	 *
	 * @throws URISyntaxException
	 *             the uri syntax exception.
	 * @throws ZipException
	 *             the zip exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testZipFile() throws URISyntaxException, ZipException, IOException {
		File file = new File(this.getClass().getResource("/file").toURI());
		File outputFile = new File(this.getClass().getResource("/testOutput.zip").toURI());

		ArchiveUtil.zipFile(file, outputFile);

		try (ZipFile compressedFile = new ZipFile(outputFile)) {
			Assert.assertTrue(compressedFile.getEntry("file") != null);
		}
	}

	/**
	 * Test the compressing functionality of {@link ArchiveUtil} with a missing file.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void testZipMissingFile() {
		File file = new File("");
		ArchiveUtil.zipFile(file, file);
	}
}
