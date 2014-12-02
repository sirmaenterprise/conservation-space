/**
 * Copyright (c) 2012 23.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.help.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Main invoker of logic.
 *
 * @author Borislav Banchev
 */
public class HelpGeneratorRunner {
	private static Logger logger = Logger.getLogger(HelpGeneratorRunner.class);

	/**
	 * Unzip a stream representing zip file to the provided output directory
	 *
	 * @param zipFile
	 *            input zip file
	 * @param outputFolder
	 *            zip extraction root folder
	 * @return the output folder on success
	 */
	private static File unZipIt(InputStream zipFile, File outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(zipFile);
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder, fileName);

				logger.debug("Unziping : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				File file = new File(newFile.getParent());
				file.mkdirs();
				file.delete();
				file.mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				IOUtils.closeQuietly(fos);
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			IOUtils.closeQuietly(zis);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
		return outputFolder;
	}

	/**
	 * Invokes index generator and html page cleaner.
	 *
	 * @param args
	 *            are the main args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		logger.info("Start generating...");
		File rootDir = null;
		try {
			rootDir = File.createTempFile(new Long(System.currentTimeMillis()).toString(), "");
			rootDir.mkdirs();
			rootDir.delete();

			String zipFileLocation = args[0];

			File unZipIt = unZipIt(new FileInputStream(zipFileLocation), rootDir);
			File projectSubSpace = unZipIt.listFiles()[0];
			File htmlLocation = new File(rootDir, "Pages");
			projectSubSpace.renameTo(htmlLocation);

			File toolsDir = new File(rootDir.getAbsolutePath());
			toolsDir = unZipIt(HelpGeneratorRunner.class.getResourceAsStream("tools.zip"), toolsDir);
			// // the main args
			// // do add exported confluence pages in tools dir as Pages dir
			File helpSearchLocation = new File(rootDir, "JavaHelpSearch");
			String[] argsData = new String[] { rootDir.getAbsolutePath(),
					htmlLocation.getAbsolutePath() };
			HelpIndexesGenerator.main(argsData);
			HtmlHelpCleaner.main(argsData);
			//
			FixMetaTag.main(new String[] { htmlLocation.getAbsolutePath() });
			//

			logger.info("Save index.html");
			// Save copy of index.html
			File indexFile = new File(htmlLocation, "index.html");
			File tempFile = File.createTempFile("index", null);
			tempFile.deleteOnExit();
			FileUtils.copyFile(indexFile, tempFile, true);
			// Deleta index.html
			indexFile.delete();

			// Start jhindexer.jar
			logger.info("Start jhindexer.jar");
			String pathToJhindexer = toolsDir.getAbsolutePath();
			JavaHelpIndexerRunner.main(new String[] { pathToJhindexer,
					htmlLocation.getName(), helpSearchLocation.getName() });

			// restore index.html
			logger.info("Restore index.html");
			FileUtils.copyFile(tempFile, indexFile, true);

			logger.info("Add css style for printing pages");
			File siteCss = new File(htmlLocation, "styles" + File.separatorChar + "site.css");
			List<String> list = FileUtils.readLines(siteCss);
			list.add("");
			list.add("* {");
			list.add("	max-width: 8in;");
			list.add("}");
			list.add("");
			list.add("@media print {");
			list.add("  .bigImage { width: 680px; }");
			list.add("}");

			FileUtils.writeLines(siteCss, list);
			File webContent = null;
			if (args.length == 2) {
				webContent = new File(args[1]);
			} else {
				webContent = new File("src\\main\\webapp");
			} // Copy Pages directory from tools to WebContent

			logger.info("Store location " + webContent.getAbsolutePath());
			File webContentFile = new File(webContent, "Pages");
			deleteRecursively(webContentFile);
			webContentFile.mkdirs();
			logger.info("Copy directory Pages from " + htmlLocation + " to " + webContentFile);
			FileUtils.copyDirectory(htmlLocation, webContentFile);

			// Copy indexes to webcontent
			File fileJavaHelpSearch = new File(webContent, "JavaHelpSearch");
			deleteRecursively(fileJavaHelpSearch);
			fileJavaHelpSearch.mkdirs();
			logger.info("Copy directory JavaHelpSearch from " + helpSearchLocation + " to "
					+ fileJavaHelpSearch);
			FileUtils.copyDirectory(helpSearchLocation, fileJavaHelpSearch);

			File userhelpIndex = new File(rootDir, "userhelpIndex.xml");
			logger.info("Copy userhelpIndex.xml from " + userhelpIndex + " to " + webContent);
			FileUtils.copyFileToDirectory(userhelpIndex, webContent);

			File userhelpMap = new File(rootDir, "userhelpMap.jhm");
			logger.info("Copy userhelpMap.jhm from " + userhelpMap + " to " + webContent);
			FileUtils.copyFileToDirectory(userhelpMap, webContent);

			File userhelpTOC = new File(rootDir, "userhelpTOC.xml");
			logger.info("Copy userhelpTOC.xml from " + userhelpTOC + " to " + webContent);
			FileUtils.copyFileToDirectory(userhelpTOC, webContent);

		} catch (Exception e) {
			logger.error("Error during generation of help! ", e);
		} finally {
			deleteRecursively(rootDir);
		}
		logger.info("Done");
	}

	/**
	 * Recursively deletes a directory or a file immediately.
	 *
	 * @param fileEntry
	 *            is the entry to begin with
	 */
	private static void deleteRecursively(File fileEntry) {
		if (fileEntry.isDirectory()) {
			File[] listFiles = fileEntry.listFiles();
			for (File file : listFiles) {
				deleteRecursively(file);
			}
		}
		if (!fileEntry.delete()) {
			fileEntry.deleteOnExit();
		}

	}
}
