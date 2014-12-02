package com.sirma.itt.emf.help.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Start jhindexer.jar
 *
 * @author Boyan Tonchev
 */
public class JavaHelpIndexerRunner {

	private static Logger logger = Logger.getLogger(JavaHelpIndexerRunner.class);
	private static final String NAME_DIR_HTML_FILES = "{name_dir_html_files}";
	private static final String NAME_DESTINATION = "{name_destination}";
	private static final String COMMAND = "java -jar jhindexer.jar " + NAME_DIR_HTML_FILES
			+ " -db " + NAME_DESTINATION;

	/**
	 * @param args
	 * <br/>
	 *            0. path to jhindexer.jar <br/>
	 *            1. name of dir with html files.<br/>
	 *            2. name of destination directory
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		String command = COMMAND;

		File rootDir = null;

		if (args.length == 3) {
			rootDir = new File(args[0]);
			command = command.replace(NAME_DIR_HTML_FILES, args[1]);
			command = command.replace(NAME_DESTINATION, args[2]);
		} else {
			throw new RuntimeException("Invalid arguments provided!");
		}

		Process p = Runtime.getRuntime().exec(command, null, rootDir);
		p.waitFor();
		if (p.exitValue() != 0) {
			throw new RuntimeException(new String(IOUtils.toByteArray(p.getErrorStream())));
		} else {
			String info = new String(IOUtils.toByteArray(p.getInputStream()));
			if (info.trim().length() > 0) {
				logger.info(info);
			}
		}
	}
}
