/**
 * Copyright (c) 2012 15.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.help.util;

import static com.sirma.itt.seip.help.util.HelpGeneratorUtil.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.help.exception.HelpGenerationException;

/**
 * The Class IndexToHelpFilesGenerator generates all necessary files for starting javahelp application.
 *
 * @author Borislav Banchev
 */
public class HelpIndexGenerator {

	/** index file for havahelp . */
	private static final String FILE_HELP_INDEX = "userhelpIndex.xml";
	/** mapping file for havahelp . */
	private static final String FILE_HELP_MAP = "userhelpMap.jhm";
	/** toc file for havahelp . */
	private static final String FILE_HELP_TOC = "userhelpTOC.xml";

	/** The toc writer. */
	private Writer tocWriter;

	/** The jhm writer. */
	private Writer jhmWriter;

	/** The index writer. */
	private Writer indexWriter;

	/** The index writer. */
	private StringBuilder indexBuilder;
	/** generated id for current level and element. */

	/** The html location. */
	private File helpFilesFolder;

	private Map<String, StringPair> uriIdToFileMapping = new LinkedHashMap<>();
	int currentIndex = 0;

	/**
	 * Generates the search index files.
	 *
	 * @param rootDir
	 *            the root dir the main store location [1]
	 * @param helpFilesFolder
	 *            the html files sub directory
	 * @param uriIdToFileMapping
	 *            is the files mapping
	 */
	public void process(File helpFilesFolder, Map<String, StringPair> uriIdToFileMapping) {
		this.helpFilesFolder = helpFilesFolder;
		this.uriIdToFileMapping = uriIdToFileMapping;
		process();
	}

	/**
	 * Processs the index retrieval and update. Generates the help files {@link #FILE_HELP_INDEX},
	 * {@link #FILE_HELP_MAP}, {@link #FILE_HELP_TOC}
	 */
	private void process() {
		try {
			LinkedList<String> keys = new LinkedList<>(uriIdToFileMapping.keySet());
			ListIterator<String> listIterator = keys.listIterator();
			prepare();
			while (listIterator.hasNext()) {
				String levelId = listIterator.next();
				StringPair value = uriIdToFileMapping.get(levelId);
				startTag();
				appendAttributes(levelId, value.getFirst(), value.getSecond());
				if (listIterator.hasNext()) {
					String next = listIterator.next();
					int depthChange = getDepthChange(levelId, next);
					closeTag(depthChange);
					listIterator.previous();
				} else {
					closeTag(-currentIndex);
				}
			}
			end();
		} catch (Exception e) {
			throw new HelpGenerationException(e);
		} finally {
			finish();
		}
	}

	/**
	 * Prepare.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void prepare() throws IOException {
		indexBuilder = new StringBuilder(8000);
		File rootFolder = helpFilesFolder.getParentFile();
		tocWriter = new OutputStreamWriter(new FileOutputStream(new File(rootFolder, FILE_HELP_TOC)), UTF_8);
		jhmWriter = new OutputStreamWriter(new FileOutputStream(new File(rootFolder, FILE_HELP_MAP)), UTF_8);
		indexWriter = new OutputStreamWriter(new FileOutputStream(new File(rootFolder, FILE_HELP_INDEX)), UTF_8);
		StringBuilder header = new StringBuilder();
		header.append("<?xml version='1.0' encoding='UTF-8'  ?>").append(HelpGeneratorUtil.NL);

		jhmWriter.append(header.toString());
		jhmWriter.append(startMap());
		indexWriter.append(header.toString());
		indexWriter.append(startIndex());
		tocWriter.append(header.toString());
		tocWriter.append(startToc());
	}

	/**
	 * Start toc.
	 *
	 * @return the string
	 */
	private String startToc() {
		StringBuilder header = new StringBuilder();
		header.append("<!DOCTYPE toc ").append(HelpGeneratorUtil.NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\"").append(
				HelpGeneratorUtil.NL);
		header.append("\"http://java.sun.com/products/javahelp/toc_2_0.dtd\">").append(HelpGeneratorUtil.NL).append(
				HelpGeneratorUtil.NL);
		header.append("<toc version=\"2.0\">");
		return header.toString();
	}

	/**
	 * Start map.
	 *
	 * @return the string
	 */
	private String startMap() {
		StringBuilder header = new StringBuilder();
		header.append("<!DOCTYPE map ").append(HelpGeneratorUtil.NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN\"").append(
				HelpGeneratorUtil.NL);
		header.append("\"http://java.sun.com/products/javahelp/map_2_0.dtd\">").append(HelpGeneratorUtil.NL).append(
				HelpGeneratorUtil.NL);
		header.append("<map version=\"2.0\">");
		return header.toString();
	}

	/**
	 * Start index.
	 *
	 * @return the string
	 */
	private String startIndex() {
		StringBuilder header = new StringBuilder();
		header.append("<!DOCTYPE index ").append(HelpGeneratorUtil.NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 2.0//EN\"").append(
				HelpGeneratorUtil.NL);
		header.append("\"http://java.sun.com/products/javahelp/index_2_0.dtd\">").append(HelpGeneratorUtil.NL).append(
				HelpGeneratorUtil.NL);
		header.append("<index version=\"2.0\">");
		return header.toString();
	}

	/**
	 * End.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void end() throws IOException {
		indexWriter.append(indexBuilder);
		indexWriter.append("</index>");
		tocWriter.append(
				indexBuilder.toString().replaceAll("<indexitem", "<tocitem").replaceAll("</indexitem", "</tocitem"));
		tocWriter.append("</toc>");
		jhmWriter.append("</map>");
	}

	/**
	 * Finish.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void finish() {
		IOUtils.closeQuietly(tocWriter);
		IOUtils.closeQuietly(jhmWriter);
		IOUtils.closeQuietly(indexWriter);
		indexBuilder = null;
	}

	/**
	 * Start tag.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void startTag() throws IOException {
		indexBuilder.append("\n<indexitem ");
	}

	/**
	 * Append attributes.
	 *
	 * @param line
	 *            the line
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void appendAttributes(String id, String fileId, String fileName) throws IOException {
		StringBuilder builderLine = new StringBuilder();
		builderLine.append(" text=\"");
		builderLine.append(URLDecoder.decode(fileId, UTF_8).replace("&quot;", "\\&quot;"));
		builderLine.append('"');
		builderLine.append(" target=\"id");
		builderLine.append(id);
		builderLine.append('"');
		String lastLine = builderLine.toString();
		indexBuilder.append(lastLine);
		String mapEntry = HelpGeneratorUtil.NL + "<mapID ";
		mapEntry += " target=\"id" + id + '"';

		mapEntry += " url=\"" + helpFilesFolder.getName() + '/' + fileName.replace("\"", "\\\"") + "\" />";
		jhmWriter.write(mapEntry);
	}

	/**
	 * Close tag.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void closeTag(int levelChange) throws IOException {
		StringBuilder closeTag = new StringBuilder();
		int levelChangeLocal = levelChange;
		if (levelChangeLocal == 0) {
			closeTag.append("/>");
		} else if (levelChangeLocal == 1) {
			closeTag.append(">");
			currentIndex++;
		} else {
			closeTag.append("/>");
			while (levelChangeLocal < 0) {
				closeTag.append("</indexitem>");
				currentIndex--;
				levelChangeLocal++;
			}
		}
		indexBuilder.append(closeTag);
	}

	private int getDepthChange(String lastId, String currentId) {
		int lastIndexDepth = 0;
		int currentIndexDepth = 0;
		if (lastId.length() > 0) {
			lastIndexDepth = lastId.split("\\.").length;
		}
		if (currentId.length() > 0) {
			currentIndexDepth = currentId.split("\\.").length;
		}
		if (currentIndexDepth == lastIndexDepth) {
			return 0;
		}
		return currentIndexDepth - lastIndexDepth;
	}

}
