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

import org.apache.commons.io.IOUtils;

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
}
