/**
 * Copyright (c) 2012 23.01.2012 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.help.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.help.exception.HelpGenerationException;
import com.sirma.itt.seip.util.file.ArchiveUtil;

/**
 * Main invoker of logic.
 *
 * @author Borislav Banchev
 */
public class GenerateHelpExecutor {
	private static final Logger LOGGER = Logger.getLogger(GenerateHelpExecutor.class);

	private GenerateHelpExecutor() {
	}

	/**
	 * Generate.
	 *
	 * @param zipFile
	 *            the zip file location
	 * @param helpFilesDestination
	 *            the storage location
	 * @param customCss
	 *            is list of custom css rules to apply to the help pages
	 */
	public static void generate(File zipFile, File helpFilesDestination, String... customCss) {
		LOGGER.info("Start generating...");
		File tempRootFolder = helpFilesDestination;
		try {
			// prepare storage
			delete(tempRootFolder);

			ArchiveUtil.unZip(new FileInputStream(zipFile), tempRootFolder);

			File[] helpFiles = tempRootFolder.listFiles();
			if (helpFiles == null || helpFiles.length == 0) {
				throw new HelpGenerationException("Invalid storage location provided : " + tempRootFolder);
			}
			// the folder CMF
			File projectSubSpace = helpFiles[0];
			File pagesLocation = new File(tempRootFolder, "Pages");
			FileUtils.moveDirectory(projectSubSpace, pagesLocation);

			// // the main args
			// // do add exported confluence pages in tools dir as Pages dir
			// start
			HelpGenerationPreprocessor preprocessor = new HelpGenerationPreprocessor();
			Map<String, StringPair> filesMapping = preprocessor.process(pagesLocation);
			// end
			// start
			HelpIndexGenerator helpIndexesGenerator = new HelpIndexGenerator();
			helpIndexesGenerator.process(pagesLocation, filesMapping);
			// end
			// start
			HelpHtmlCleaner cleaner = new HelpHtmlCleaner();
			cleaner.cleanUp(pagesLocation, filesMapping);
			// end
			// start
			HelpHtmlMetaTagFixer.run(pagesLocation);
			// end
			// start
			LOGGER.info("Add css style for printing pages");
			HelpCSSFixer.fix(pagesLocation, customCss);
			// end

			// Save copy of index.html and move it before fts analysis
			File indexFile = new File(pagesLocation, "index.html");
			File tempFile = File.createTempFile("index", null);
			delete(tempFile);
			FileUtils.moveFile(indexFile, tempFile);

			// prepare jhindexer
			File toolsDir = tempRootFolder;
			ArchiveUtil.unZip(GenerateHelpExecutor.class.getResourceAsStream("tools.zip"), toolsDir);

			// Start jhindexer.jar
			LOGGER.info("Start jhindexer.jar");
			String pathToJhindexer = toolsDir.getAbsolutePath();
			File helpSearchLocation = new File(tempRootFolder, "JavaHelpSearch");
			HelpIndexer.run(new String[] { pathToJhindexer, pagesLocation.getName(), helpSearchLocation.getName() });

			// restore index.html
			LOGGER.info("Restore index.html");
			FileUtils.moveFile(tempFile, indexFile);

			delete(new File(toolsDir, "jhindexer.bat"));
			delete(new File(toolsDir, "jhindexer.jar"));
		} catch (IOException e) {
			LOGGER.error("Error during generation of help! ", e);
		} catch (Exception e) {
			LOGGER.error("Error during generation of help! ", e);
		}
		LOGGER.info("Done");
	}

	/**
	 * Recursively deletes a directory or a file immediately.
	 *
	 * @param fileEntry
	 *            is the entry to begin with
	 */
	private static void delete(File fileEntry) {
		FileUtils.deleteQuietly(fileEntry);
	}
}
