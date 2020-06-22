package com.sirma.itt.seip.util.file;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Utility class for working with archives
 *
 * @author bbanchev
 */
public class ArchiveUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Instantiates a new archive util.
	 */
	private ArchiveUtil() {
		// utility class
	}

	/**
	 * Unzip the given archive into the given filder
	 *
	 * @param zipFile
	 *            the zip file
	 * @param outputFolder
	 *            the output folder
	 */
	public static void unZip(File zipFile, File outputFolder) {
		try (InputStream zipFileStream = new FileInputStream(zipFile)) {
			unZip(zipFileStream, outputFolder);
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Unzip stream to provided directory
	 *
	 * @param zipFileStream
	 *            the input stream
	 * @param outputFolder
	 *            the output location
	 */
	public static void unZip(InputStream zipFileStream, File outputFolder) {

		// create output directory is not exists
		if (!outputFolder.exists() && outputFolder.mkdirs()) {
			LOGGER.debug("Created output folder {}", outputFolder);
		}

		// get the zip file content
		try (ZipInputStream zis = new ZipInputStream(zipFileStream)) {
			readZipInput(outputFolder, zis);
		} catch (IOException iox) {
			throw new EmfRuntimeException(iox);
		}
	}

	private static void readZipInput(File outputFolder, ZipInputStream zis) throws IOException {
		// get the zipped file list entry
		ZipEntry ze;

		while ((ze = zis.getNextEntry()) != null) {
			String fileName = ze.getName();
			File newFile = new File(outputFolder, fileName);
			if (ze.isDirectory()) {
				if (!newFile.mkdirs()) {
					LOGGER.warn("Could not create zip folder {}", newFile);
				}
				continue;
			}
			if (!newFile.getParentFile().exists() && !newFile.getParentFile().mkdirs()) {
				LOGGER.warn("Could not create parent dir: {}", newFile.getParentFile());
			}
			extractFile(zis, newFile);
		}
	}

	private static void extractFile(ZipInputStream zis, File newFile) throws IOException {
		byte[] buffer = new byte[1024];
		try (FileOutputStream fos = new FileOutputStream(newFile)) {
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
	}

	/**
	 * Compress the given file in the given output zip File. If the given file is a directory, all files from it will be
	 * added to the zip archive without the parent folder, meaning that if you compress the following structure:<br>
	 * <ul>
	 * 	<li>files/</li>
	 * 	<ul>
	 * 		<li>file1</li>
	 * 		<li>file2</li>
	 * 	</ul>
	 * </ul>
	 * you will end up with the following archive structure:<br>
	 * <ul>
	 * 	<li>archive.zip</li>
	 * 	<ul>
	 * 		<li>file1</li>
	 * 		<li>file2</li>
	 * 	</ul>
	 * </ul>
	 *
	 * @param file
	 *            the file to be compressed
	 * @param outputZipFile
	 *            the compressed output file
	 */
	public static void zipFile(File file, File outputZipFile) {
		try (ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
			Path filePath = Paths.get(file.toURI());
			if (file.isDirectory()) {
				try (Stream<Path> files = Files.walk(filePath)) {
					files.filter(path -> !path.toFile().isDirectory())
							.forEach(path -> addZipEntry(zs, filePath, path));
				}
			} else {
				addZipEntry(zs, Paths.get(file.getParent()), filePath);
			}
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		}
	}

	private static void addZipEntry(ZipOutputStream zs, Path filePath, Path path) {
		ZipEntry zipEntry = new ZipEntry(filePath.relativize(path).toString());
		try (InputStream inputStream = Files.newInputStream(path, READ)) {
			zs.putNextEntry(zipEntry);
			IOUtils.copyLarge(inputStream, zs);
			zs.closeEntry();
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		}
	}
}
