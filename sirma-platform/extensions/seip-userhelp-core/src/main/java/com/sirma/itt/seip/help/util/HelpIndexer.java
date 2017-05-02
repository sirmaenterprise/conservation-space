package com.sirma.itt.seip.help.util;

import static com.sirma.itt.seip.help.util.HelpGeneratorUtil.UTF_8;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.seip.help.exception.UserhelpException;

/**
 * Start jhindexer.jar
 *
 * @author Boyan Tonchev
 */
public class HelpIndexer {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	private static final String NAME_DIR_HTML_FILES = "{name_dir_html_files}";
	private static final String NAME_DESTINATION = "{name_destination}";
	private static final String COMMAND = "java -jar jhindexer.jar " + NAME_DIR_HTML_FILES + " -db " + NAME_DESTINATION;

	private HelpIndexer() {
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            <br/>
	 *            0. path to jhindexer.jar <br/>
	 *            1. name of dir with html files.<br/>
	 *            2. name of destination directory
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static void run(String[] args) {

		String command = COMMAND;

		File rootDir = null;
		try {

			if (args.length == 3) {
				rootDir = new File(args[0]);
				command = command.replace(NAME_DIR_HTML_FILES, args[1]);
				command = command.replace(NAME_DESTINATION, args[2]);
			} else {
				throw new UserhelpException("Invalid arguments provided! " + Arrays.toString(args));
			}

			Process p = Runtime.getRuntime().exec(command, null, rootDir);
			p.waitFor();
			if (p.exitValue() != 0) {
				throw new UserhelpException(new String(IOUtils.toByteArray(p.getErrorStream()), UTF_8));
			} else {
				String info = new String(IOUtils.toByteArray(p.getInputStream()), UTF_8);
				if (info.trim().length() > 0) {
					LOGGER.info(info);
				}
			}
		} catch (UserhelpException e) {
			throw e;
		} catch (Exception e) {
			throw new UserhelpException("Failed to generated search index.", e);
		}
	}
}
