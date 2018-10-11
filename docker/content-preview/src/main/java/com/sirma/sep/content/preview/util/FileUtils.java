package com.sirma.sep.content.preview.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

/**
 * Utility class for dealing with files in {@link com.sirma.sep.content.preview.ContentPreviewApplication}.
 *
 * @author Mihail Radkov
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private FileUtils() {
		// hide util constructor.
	}

	/**
	 * Tries to delete given {@link File} if it exists.
	 * <p>
	 * This is {@link Exception} safe, any resulting errors will be simply logged.
	 *
	 * @param file
	 * 		- the provided {@link File} for deletion. Can be <code>null</code> or non existing.
	 */
	public static void deleteFile(File file) {
		if (file == null) {
			return;
		}
		try {
			if (!Files.deleteIfExists(file.toPath())) {
				file.deleteOnExit();
			}
		} catch (Exception e) {
			LOGGER.warn(e.getMessage(), e);
		}
	}

	/**
	 * Tries to create a directory in the provided location. If the folder cannot be created then
	 * {@link IllegalArgumentException} will be thrown with the original exception.
	 *
	 * @param directory - the location to be created
	 */
	public static void createDirectory(File directory) {
		try {
			Files.createDirectories(directory.toPath());
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot create directory " + directory.getPath(), e);
		}
	}

	/**
	 * Strips file extension from provided file path as {@link String}
	 *
	 * @param path
	 * 		- the path to strip file extension from. Must not be <code>null</code> !
	 * @return the stripped path
	 */
	public static String withoutExtension(String path) {
		int pos = path.lastIndexOf('.');
		if (pos > 0) {
			return path.substring(0, pos);
		}
		return path;
	}
}
