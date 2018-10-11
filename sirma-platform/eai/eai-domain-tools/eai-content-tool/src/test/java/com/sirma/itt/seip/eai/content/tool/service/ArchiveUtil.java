package com.sirma.itt.seip.eai.content.tool.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.content.tool.exception.EAIRuntimeException;

public class ArchiveUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ArchiveUtil() {
		// utility class
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
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		// get the zip file content
		try (ZipInputStream zis = new ZipInputStream(zipFileStream)) {
			readZipInput(outputFolder, zis);
		} catch (IOException iox) {
			throw new EAIRuntimeException(iox.getMessage(), iox);
		}
	}

	private static void readZipInput(File outputFolder, ZipInputStream zis) throws IOException {
		// get the zipped file list entry
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			String fileName = ze.getName();
			File newFile = new File(outputFolder, fileName);
			if (ze.isDirectory()) {
				newFile.mkdirs();
				continue;
			}
			newFile.getParentFile().mkdirs();
			extractFile(zis, newFile);
		}
	}

	private static void extractFile(ZipInputStream zis, File newFile) throws IOException {
		byte[] buffer = new byte[1024];
		try (FileOutputStream fos = new FileOutputStream(newFile)) {
			int len = -1;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
	}
}
