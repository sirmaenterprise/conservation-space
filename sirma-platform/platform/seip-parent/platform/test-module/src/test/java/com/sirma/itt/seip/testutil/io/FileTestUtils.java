package com.sirma.itt.seip.testutil.io;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

/**
 * File utility class test purposes.
 *
 * @author BBonev
 */
public class FileTestUtils {

	/**
	 * Copy resources to temp folder.
	 *
	 * @param base
	 *            the base
	 * @param files
	 *            the files
	 * @return the temp folder
	 */
	public static File copyResourcesToTempFolder(Class<?> base, Collection<String> files) {
		File currentDir = new File(UUID.randomUUID().toString());
		currentDir.mkdirs();
		for (String file : files) {
			File outFile = new File(currentDir, UUID.randomUUID().toString() + ".xml");
			try {
				outFile.createNewFile();
			} catch (IOException e1) {
				fail(e1.getMessage(), e1);
			}
			try (InputStream stream = base.getResourceAsStream(file);
					OutputStream output = new FileOutputStream(outFile)) {
				IOUtils.copyLarge(stream, output);
			} catch (IOException e) {
				fail(e.getMessage(), e);
			}
		}
		return currentDir;
	}

	/**
	 * Copy resources to temp folder.
	 *
	 * @param base
	 *            the base
	 * @param files
	 *            the files
	 * @return the temp folder
	 */
	public static File copyFilesToTempFolder(Collection<String> files) {
		File currentDir = new File(UUID.randomUUID().toString());
		currentDir.mkdirs();
		for (String path : files) {
			File outFile = new File(currentDir, UUID.randomUUID().toString() + ".xml");
			try {
				outFile.createNewFile();
			} catch (IOException e1) {
				fail(e1.getMessage(), e1);
			}
			File inputFile = new File(path);
			assertTrue(inputFile.exists(), "File not found: " + inputFile.getAbsolutePath());
			try (InputStream stream = new FileInputStream(inputFile);
					OutputStream output = new FileOutputStream(outFile)) {
				IOUtils.copyLarge(stream, output);
			} catch (IOException e) {
				fail(e.getMessage(), e);
			}
		}
		return currentDir;
	}

	/**
	 * Clean directory and it's files
	 *
	 * @param currentDir
	 *            the current dir
	 */
	public static void cleanDirectory(File currentDir) {
		File[] files = currentDir.listFiles();
		for (File file : files) {
			file.delete();
		}
		currentDir.delete();
	}

	/**
	 * Get classpath resource as stream .
	 * @param res Resource identifier.
	 * @return {@link InputStream} for the identifier.
	 */
	public static InputStream getResourceAsStream(String res) {
		return FileTestUtils.class.getResourceAsStream(res);
	}

	/**
	 * Read a file on the classpath that is relative to the current class into a string.
	 *
	 * @param file
	 *            path to the file.
	 * @return Contents of the file as a string.
	 */
	public static String readFile(String file) {
		try (InputStream in = getResourceAsStream(file)) {
			return IOUtils.toString(in);
		} catch (IOException e) {
			fail("Could not read file: " + file, e);
		}
		return null;
	}

	/**
	 * Create file in the default temporary-file directory and copy content from <code>srcFilePath</code> to it.
	 *
	 * @param srcFilePath
	 * 		the source file path.
	 * @return created file.
	 * @throws IOException
	 */
	public static File copyFileToTempDir(String srcFilePath, String errorMessage) throws IOException {
		String fileExtention = srcFilePath.substring(srcFilePath.lastIndexOf("."));
		File fileIn = File.createTempFile("temp_", UUID.randomUUID() + fileExtention);
		try {
			File testFile = new File(FileTestUtils.class.getClassLoader().getResource(srcFilePath).toURI());
			FileUtils.copyFile(testFile, fileIn);
		} catch (Exception e) {
			deleteFile(fileIn);
			Assert.fail(errorMessage);
		}
		return fileIn;
	}

	/**
	 * Try to delete file if fail mark it for deletion on exit.
	 *
	 * @param file
	 * 		the file to be deleted.
	 */
	public static void deleteFile(File file) {
		if (file != null && file.exists()) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
		}
	}
}
