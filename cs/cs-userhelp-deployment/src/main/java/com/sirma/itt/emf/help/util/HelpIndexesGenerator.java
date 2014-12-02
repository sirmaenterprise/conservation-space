/**
 * Copyright (c) 2012 15.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.help.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

// TODO: Auto-generated Javadoc
/**
 * The Class IndexToHelpFilesGenerator generates all necessary files for starting javahelp
 * application.
 *
 * @author Borislav Banchev
 */
public class HelpIndexesGenerator {
	/** index file for havahelp . */
	private final String helpIndexFile = "userhelpIndex.xml";
	/** mapping file for havahelp . */
	private final String helpMapFile = "userhelpMap.jhm";
	/** toc file for havahelp . */
	private final String helpTOCFile = "userhelpTOC.xml";

	/** The Constant NL. */
	private static final String NL = "\n";
	/** store location for help system. */
	private File storeLocation;

	/** The result. */
	private final StringBuilder result = new StringBuilder();

	/** The last line. */
	private String lastLine = "";

	/** The toc writer. */
	private FileWriter tocWriter;

	/** The jhm writer. */
	private FileWriter jhmWriter;

	/** The index writer. */
	private FileWriter indexWriter;
	/** generated id for current level and element. */
	private String id = "";

	/** The id number. */
	private Integer idNumber = new Integer(1);

	/** The html location. */
	private File htmlLocation;

	/**
	 * Generate.
	 *
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String generate() throws IOException {
		assert htmlLocation != null;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(htmlLocation,
				"index.html")));
		String line = null;
		boolean startParse = false;
		try {
			prepare();
			while ((line = bufferedReader.readLine()) != null) {
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
			}
			indexWriter.append(result.toString().replaceAll("tocitem", "indexitem"));
			end();
		} finally {
			bufferedReader.close();
			finish();
		}
		return result.toString();
	}

	/**
	 * Prepare.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void prepare() throws IOException {
		tocWriter = new FileWriter(new File(getStoreLocation(), helpTOCFile));
		jhmWriter = new FileWriter(new File(getStoreLocation(), helpMapFile));
		indexWriter = new FileWriter(new File(getStoreLocation(), helpIndexFile));
		StringBuilder header = new StringBuilder();
		header.append("<?xml version='1.0' encoding='UTF-8'  ?>").append(NL);

		tocWriter.append(header.toString());
		tocWriter.append(startToc());
		jhmWriter.append(header.toString());
		jhmWriter.append(startMap());
		indexWriter.append(header.toString());
		indexWriter.append(startIndex());
	}

	/**
	 * Start toc.
	 *
	 * @return the string
	 */
	private String startToc() {
		StringBuilder header = new StringBuilder();
		header.append("<!DOCTYPE toc ").append(NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\"")
				.append(NL);
		header.append("\"http://java.sun.com/products/javahelp/toc_2_0.dtd\">").append(NL)
				.append(NL);
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
		header.append("<!DOCTYPE map ").append(NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN\"")
				.append(NL);
		header.append("\"http://java.sun.com/products/javahelp/map_2_0.dtd\">").append(NL)
				.append(NL);
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
		header.append("<!DOCTYPE index ").append(NL);
		header.append("PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 2.0//EN\"")
				.append(NL);
		header.append("\"http://java.sun.com/products/javahelp/index_2_0.dtd\">").append(NL)
				.append(NL);
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
		tocWriter.append("</toc>");
		jhmWriter.append("</map>");
		indexWriter.append("</index>");
	}

	/**
	 * Finish.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void finish() throws IOException {
		tocWriter.close();
		jhmWriter.close();
		indexWriter.close();
	}

	/**
	 * Append attributes.
	 *
	 * @param line
	 *            the line
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void appendAttributes(String line) throws IOException {
		String data = line.replace("<a href=\"", "").trim();
		data = data.replace("\">", "#");
		data = data.replace("</a>", "");
		String[] split = data.split("#");
		if (split.length != 2) {
			throw new RuntimeException(Arrays.toString(split));
		}
		lastLine = " text=\"" + split[1].replace("&quot;", "\\&quot;") + "\"";
		lastLine += " target=\"id" + id + "\"";
		result.append(lastLine);
		tocWriter.append(lastLine);
		String mapEntry = NL + "<mapID ";
		mapEntry += " target=\"id" + id + "\"";
		String url = split[0].replaceAll("\\s", "");
		new File(htmlLocation, split[0]).renameTo(new File(htmlLocation, url));
		mapEntry += " url=\"" + htmlLocation.getName() + '/' + url.replace("\"", "\\\"") + "\" />";
		jhmWriter.write(mapEntry);
	}

	/**
	 * Close tag.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void closeTag() throws IOException {
		if (lastLine.matches(".*target=.*")) {
			lastLine = "/>";
		} else {
			lastLine = "</tocitem>";
			id = id.substring(0, id.lastIndexOf("."));
		}
		result.append(lastLine);
		tocWriter.append(lastLine);
	}

	/**
	 * Start tag.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void startTag() throws IOException {
		if (!lastLine.equals("</tocitem>") && !lastLine.equals("/>") && !lastLine.isEmpty()) {
			result.append(">");
			tocWriter.append(">");
			idNumber = new Integer(0);
			id = id + "." + idNumber;

		} else if (id.lastIndexOf(".") != -1) {
			String substring = id.substring(id.lastIndexOf(".") + 1, id.length());
			idNumber = new Integer(substring);
		}
		lastLine = "\n<tocitem";
		result.append(lastLine);
		idNumber++;
		id = id.replaceFirst("\\.\\d{1,2}$", "." + idNumber);
		tocWriter.append(lastLine);
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments [0] - the main store location [1] - the html files sub directory
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File rootDir = new File(args[0]);
		HelpIndexesGenerator indexToTOCFileGenerator = new HelpIndexesGenerator();
		indexToTOCFileGenerator.setStoreLocation(rootDir);
		indexToTOCFileGenerator.setHTMLLocation(new File(args[1]));
		indexToTOCFileGenerator.generate();
	}

	/**
	 * Sets the hTML location.
	 *
	 * @param file
	 *            the new hTML location
	 */
	private void setHTMLLocation(File file) {
		htmlLocation = file;
	}

	/**
	 * Getter method for helpIndexFile.
	 *
	 * @return the helpIndexFile
	 */
	public String getHelpIndexFile() {
		return helpIndexFile;
	}

	/**
	 * Getter method for helpMapFile.
	 *
	 * @return the helpMapFile
	 */
	public String getHelpMapFile() {
		return helpMapFile;
	}

	/**
	 * Getter method for helpTOCFile.
	 *
	 * @return the helpTOCFile
	 */
	public String getHelpTOCFile() {
		return helpTOCFile;
	}

	/**
	 * Setter method for storeLocation. Sets the root folder for javahelp files.
	 *
	 * @param storeLocation
	 *            the storeLocation to set
	 */
	public void setStoreLocation(File storeLocation) {
		this.storeLocation = storeLocation;
	}

	/**
	 * Getter method for storeLocation.
	 *
	 * @return the storeLocation
	 */
	public File getStoreLocation() {
		return storeLocation;
	}
}
