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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.help.exception.HelpGenerationException;

/**
 * The Class IndexToHelpFilesGenerator generates all necessary files for starting javahelp application.
 *
 * @author Borislav Banchev
 */
public class HelpGenerationPreprocessor {

	private Map<String, StringPair> idToNameAndLocationMapping = new LinkedHashMap<>();
	private File helpFilesFolder;
	private Map<Integer, Integer> set = new HashMap<>();
	int liDepth = -1;

	/**
	 * Generate.
	 *
	 * @param rootDir
	 *            the root dir the main store location [1]
	 * @param helpFilesFolder
	 *            the html files directory
	 * @return the map of internal file name to actual name as defined in index file
	 */
	public Map<String, StringPair> process(File helpFilesFolder) {
		this.helpFilesFolder = helpFilesFolder;
		processInternal();
		return idToNameAndLocationMapping;
	}

	/**
	 * Processs the index retrieval and update. Generates the help files {@link #FILE_HELP_INDEX},
	 * {@link #FILE_HELP_MAP}, {@link #FILE_HELP_TOC}
	 */
	private void processInternal() {
		File input = new File(helpFilesFolder, "index.html");
		File output = new File(helpFilesFolder, "index_new.html");
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(input), UTF_8));
				Writer writer = new OutputStreamWriter(new FileOutputStream(output), UTF_8)) {
			StringBuilder processInternal = processInternal(fileReader);
			// now store the updated index
			writer.append(processInternal);
			writer.flush();
		} catch (Exception e) {
			throw new HelpGenerationException(e);
		}
		try {
			FileUtils.forceDelete(input);
			FileUtils.moveFile(output, input);
		} catch (IOException e) {
			throw new HelpGenerationException(e);
		}

	}

	private StringBuilder processInternal(BufferedReader fileReader) throws IOException {
		StringBuilder indexResult = new StringBuilder();
		String line = null;
		boolean startParse = false;
		while ((line = fileReader.readLine()) != null) {
			String indexFileNextLine = line;
			if (startParse) {
				if (line.contains("<li>")) {
					startTag();
				} else if (line.contains("href=")) {
					appendAttributes(line);
				} else if (line.contains("</li>")) {
					closeTag();
				}
			} else if (line.contains("Available Pages:</h2>")) {
				startParse = true;
			}
			indexResult.append(indexFileNextLine).append(HelpGeneratorUtil.NL);
		}
		return indexResult;
	}

	private void closeTag() {
		Integer depthKey = Integer.valueOf(liDepth);
		if (set.containsKey(depthKey)) {
			set.put(depthKey, Integer.valueOf(0));
		}
		liDepth--;
	}

	private void startTag() {
		Integer depthKey = Integer.valueOf(liDepth);
		if (liDepth > -1) {
			Integer newValue = set.get(depthKey);
			newValue = newValue == null ? Integer.valueOf(1) : Integer.valueOf(newValue.intValue() + 1);
			set.put(depthKey, newValue);
		}
		liDepth++;
	}

	/**
	 * Append attributes.
	 *
	 * @param line
	 *            the line
	 * @return
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String appendAttributes(String line) throws IOException {
		String data = line.replace("<a href=\"", "").trim();
		data = data.replace("\">", "#");
		data = data.replace("</a>", "");
		String[] fileAndTitle = data.split("#");
		if (fileAndTitle.length != 2) {
			return HelpGeneratorUtil.CONFIG_CUSTOM_FOOTER;
		}
		// build the key in format 1.10.1
		StringBuilder key = new StringBuilder(10);
		for (int i = 0; i < liDepth; i++) {
			key.append(set.get(Integer.valueOf(i)));
			if (i < liDepth - 1) {
				key.append('.');
			}
		}
		idToNameAndLocationMapping.put(key.toString(),
				new StringPair(URLEncoder.encode(fileAndTitle[1], "UTF-8"), fileAndTitle[0]));
		return line;
	}

}
