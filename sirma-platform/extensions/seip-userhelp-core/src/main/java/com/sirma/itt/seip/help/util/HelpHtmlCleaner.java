/**
 * Copyright (c) 2012 15.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.help.util;

import static com.sirma.itt.seip.help.util.HelpGeneratorUtil.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.help.exception.HelpGenerationException;

/**
 * The Class cleans the html pages from data that is not necessary. Fixes the external link
 *
 * @author Borislav Banchev
 */
public class HelpHtmlCleaner {
	/** Footer start tag. */
	private static final String FOOTER_START = "<section class=\"footer-body\">";
	/** Footer end tag. */
	private static final String FOOTER_END = "</section>";
	/** Attachments div start. */
	private static final String DIV_CLASS_ATTACHMENTS = "<div class=\"greybox\" align=\"left\">";
	/** Table title div start. */
	private static final String DIV_CLASS_TABLETITLE = "<div class=\"tabletitle\">";
	/** Page subheading div start. */
	private static final String DIV_CLASS_PAGESUBHEADING = "<div class=\"pagesubheading\">";

	/** The html location. */
	private File helpFilesFolder;

	private Map<String, String> fileUrlToFileMapping;

	/**
	 * Clean up code - remove extra code and update needed.
	 *
	 * @param helpFilesFolder
	 *            the html files location
	 * @param fileUrlToFileMapping
	 *            the mapping of expected url(last entry) to actual file
	 */
	public void cleanUp(File helpFilesFolder, Map<String, StringPair> fileUrlToFileMapping) {
		this.fileUrlToFileMapping = new HashMap<>();
		for (StringPair nextFile : fileUrlToFileMapping.values()) {
			this.fileUrlToFileMapping.put(nextFile.getFirst(), nextFile.getSecond());
		}
		this.helpFilesFolder = helpFilesFolder;
		cleanUpInternal();
	}

	/**
	 * Replace external link.
	 *
	 * @param line
	 *            the is the string that holds possible links
	 * @return the resulted string
	 */
	public String replaceExternalLink(String line) {
		int start = line.indexOf(HelpGeneratorUtil.CONFIG_EXTERNAL_URL);
		if (start == -1) {
			return line;
		}
		int externalLinkLength = HelpGeneratorUtil.CONFIG_EXTERNAL_URL.length();
		start += externalLinkLength;
		StringBuilder replacmentString = new StringBuilder();
		int index = start;
		while (line.charAt(index) != '"') {
			replacmentString.append(line.charAt(index));
			index++;
		}

		String replaceable = replacmentString.toString();

		String newName = null;
		if (fileUrlToFileMapping.containsKey(replaceable)) {
			newName = fileUrlToFileMapping.get(replaceable);
		} else {
			newName = replaceable.replaceAll("[\\+\\s]", "").toLowerCase() + HelpGeneratorUtil.FILE_EXTENSION_HTML;
		}

		String result = line.substring(0, start - externalLinkLength) + newName + line.substring(index);
		return replaceExternalLink(result);
	}

	/**
	 * Travers html files and clean them.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void cleanUpInternal() {
		if (helpFilesFolder == null) {
			throw new HelpGenerationException("Missing argument: htmls location!");
		}
		File[] htmlFiles = HelpGeneratorUtil.listHtmlFiles(helpFilesFolder);
		try {
			for (File nextFile : htmlFiles) {
				String newContent = removeUnnecessaryData(nextFile);
				writeBackData(nextFile, newContent);
			}
		} catch (Exception e) {
			throw new HelpGenerationException("Error during cleanup!", e);
		}
	}

	/**
	 * Write back data to the same file.
	 *
	 * @param nextFile
	 *            is the file name
	 * @param data
	 *            the data is the data to write
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeBackData(File nextFile, String data) throws IOException {
		try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(nextFile), UTF_8)) {
			fileWriter.append(data);
			fileWriter.flush();
		}
	}

	/**
	 * Removes the unnecessary data from html content.
	 *
	 * @param nextFile
	 *            is the file name
	 * @return the new content of file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String removeUnnecessaryData(File nextFile) throws IOException {
		StringBuilder result = new StringBuilder();
		try (BufferedReader fileReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(nextFile), UTF_8))) {
			removeDataInternal(result, fileReader);
		}
		return result.toString();

	}

	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private void removeDataInternal(StringBuilder result, BufferedReader fileReader) throws IOException {
		String line = null;
		boolean skipReading = false;
		boolean processingFooter = false;
		while ((line = fileReader.readLine()) != null) {
			String trimmed = line.trim();
			if (trimmed.equals(DIV_CLASS_PAGESUBHEADING) || trimmed.equals(DIV_CLASS_TABLETITLE)
					|| trimmed.equals(DIV_CLASS_ATTACHMENTS)) {
				skipReading = true;
			} else if (skipReading && trimmed.equals("</div>")) {
				skipReading = false;
				continue;
			} else if (trimmed.equals(FOOTER_START)) {
				processingFooter = true;
			}
			if (!processingFooter && !skipReading) {
				line = replaceExternalLink(line);
				result.append(line).append(HelpGeneratorUtil.NL);
			}
			if (processingFooter && line.contains(FOOTER_END)) {
				result.append(HelpGeneratorUtil.customFooter());
				processingFooter = false;
			}
		}
	}

}
