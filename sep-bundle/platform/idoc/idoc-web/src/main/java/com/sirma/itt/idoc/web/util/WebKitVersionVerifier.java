package com.sirma.itt.idoc.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;

/**
 * Checks for the installed and configured version of wkhtmltopdf.
 * 
 * @author Ivo Rusev
 */
public class WebKitVersionVerifier {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WebKitVersionVerifier.class);

	/** The tmp directory path. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR)
	private String tempDirPath;

	/** The wkhtmltopdf tool location. */
	@Inject
	@Config(name = "wkhtmltopdf.location")
	private String wkhtmltopdfLocation;

	/**
	 * Called on startup. Checks if the version of wkhtml is correct.
	 * 
	 * @param event
	 *            ServletContextEvent
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		try {
			if (StringUtils.isNullOrEmpty(wkhtmltopdfLocation)) {
				LOGGER.error("Missing wkhtmltopdf configuration. "
						+ "Please add wkhtmltopdf.location = ... to the system config file.");
				return;
			}
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(wkhtmltopdfLocation, "--version");

			Process process = processBuilder.start();
			InputStream inputStream = process.getInputStream();
			String result = getResult(inputStream);
			process.waitFor();
			if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(result)
					|| !result.contains("0.12.0")) {
				LOGGER.error("Wrong version of the wkhtmltopdf tool(the server needs version 0.12.0) "
						+ "or it's not installed at all.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("The version of the wkhtmltopdf library, installed on the server is not compatible",
					e);
		}
	}

	/**
	 * Gets the result.
	 * 
	 * @param inputStream
	 *            the input stream
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getResult(InputStream inputStream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		String result = builder.toString();
		return result;
	}

}
