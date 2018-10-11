package com.sirma.sep.ocr.utils;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for the handling files.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 26/10/2017
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ERROR_MESSAGE = "Failed to delete file, cause: {}";

	private FileUtils() {
		// hide util constructor.
	}

	/**
	 * Deletes the given as input argument file.
	 * <br />
	 * Files.delete is used because if deleting fails it gives a detailed reason why. Also file.delete has some
	 * oddities.
	 * <br />
	 *
	 * @param file a file
	 */
	public static void deleteFile(File file) {
		if (file == null) {
			LOGGER.error("Tried to delete a non existing file (null)");
			return;
		}
		try {
			if (!Files.deleteIfExists(file.toPath())) {
				file.deleteOnExit();
			}
		} catch (NoSuchFileException e) {
			LOGGER.error("{}: no such file or directory%n", file.toPath());
			LOGGER.error(ERROR_MESSAGE, e);
		} catch (DirectoryNotEmptyException e) {
			LOGGER.error("{} is a directory and is not empty", file.toPath());
			LOGGER.error(ERROR_MESSAGE, e);
		} catch (IOException e) {
			// File permission problems are caught here.
			LOGGER.error(ERROR_MESSAGE, e);
		}
	}
}