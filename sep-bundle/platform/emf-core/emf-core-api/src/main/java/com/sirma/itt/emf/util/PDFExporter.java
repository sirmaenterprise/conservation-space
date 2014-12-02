package com.sirma.itt.emf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Utility class used for exporting specific object from emf to pdf. The class uses EMF bookmarkable
 * URLs and the tool wkhtmltopdf (<a href="http://wkhtmltopdf.org/">http://wkhtmltopdf.org/</a>).
 * 
 * @author Ivo Rusev
 */
public class PDFExporter {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PDFExporter.class);
	/**
	 * The thread execution pool, used to store the wkhtmltopdf processes. Used to terminate the
	 * processes that take too long.
	 */
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	/** The constant token. */
	private static final String TOKEN = "ssoToken";
	/**
	 * Export timeout. After this time the execution of the wkhtmltopdf will be interrupted.
	 */
	private static final long EXPORT_TIMEOUT_IN_SECONDS = 40;

	/** the custom header argument for wkhtmltopdf. */
	private static final String CUSTOM_HEADER_ARG = "--custom-header";

	/** The name of the system user used for export. */
	@Inject
	@Config(name = EmfConfigurationProperties.EXPORT_USERNAME)
	private String exportUsername;
	/** The password of the system user used for export. */
	@Inject
	@Config(name = EmfConfigurationProperties.EXPORT_USERNAME_PASSWORD)
	private String exportPassword;
	/** The pdf file extension. */
	private static final String PDF_FILE_EXTENSION = ".pdf";
	/** The tmp directory path. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR)
	private String tempDirPath;
	/** The wkhtmltopdf tool location. */
	@Inject
	@Config(name = EmfConfigurationProperties.WKHTMLTOPDF_LOCATION)
	private String wkhtmltopdfLocation;
	/** Emf default protocol */
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PROTOCOL)
	private String emfProtocol;

	/**
	 * The path to the OS specific (has to escape right symbols for each OS) js that is executed
	 * after wk has finished.
	 */
	@Inject
	@Config(name = EmfConfigurationProperties.POST_WEB_KIT_JAVASCRIPTS)
	private String jsFilePath;
	/** The system host name. */
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_NAME)
	private String hostName;
	/** The system port. */
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PORT)
	private String port;
	/** Authentication service used to extract current user's token if possible. */
	@Inject
	private AuthenticationService authenticationService;

	/**
	 * Prepare command arguments to the wkhtmltopdf tool.
	 * 
	 * @param inputUrl
	 *            bookmarkable url of the instance
	 * @param outputUrl
	 *            absolute path to the generated pdf
	 * @return the list of command line parameters
	 */
	private List<String> prepareCommandArguments(String inputUrl, String outputUrl) {
		List<String> arguments = new ArrayList<>(15);
		// absolute path to the wkhtmltopdf executable
		arguments.add(wkhtmltopdfLocation);
		// Wait until window.status is equal to this string before rendering
		// page
		arguments.add("--window-status");
		arguments.add("export-ready");
		// Set the default text encoding, for input
		arguments.add("--encoding");
		arguments.add("UTF-8");

		EmfUser currentUser = null;
		try {
			currentUser = (EmfUser) authenticationService.getCurrentUser();
		} catch (ContextNotActiveException e) {
			LOGGER.debug("If the @SessionScoped context is not active swallow the exception and continue.");
		}
		// get user from context. If such is present then use its token to
		// authenticate. Else use admin username and password
		if ((currentUser != null) && (StringUtils.isNotNullOrEmpty(currentUser.getTicket()))) {
			final String ticket = currentUser.getTicket();
			arguments.add("--custom-header");
			arguments.add(TOKEN);
			arguments.add(ticket.replaceAll(System.lineSeparator(), "lineSeparator"));
		} else {
			// Set an additional HTTP header for system username
			arguments.add(CUSTOM_HEADER_ARG);
			arguments.add("username");
			arguments.add(exportUsername);
			// Set an additional HTTP header for system user password
			arguments.add(CUSTOM_HEADER_ARG);
			arguments.add("password");
			arguments.add(exportPassword);
		}

		// Run this additional javascript after the page is done loading
		// Used to remove irrelevant divisions and spanning of
		// the html page, leaving only the print preview of the document
		arguments.add("--run-script");
		arguments.add(getScriptFromFile(jsFilePath));

		// Bookmarkable url of the document
		arguments.add(inputUrl);

		// Path to the generated pdf
		arguments.add(outputUrl);

		LOGGER.debug("wkhtmltopdf prepared command line arguments: {}", arguments);

		return arguments;
	}

	/**
	 * Exports html to a PDF file and streams it back to the client.
	 * 
	 * @param bookmarkableUrl
	 *            the bookmarkable url
	 * @return the string
	 */
	public String exportToPdf(final String bookmarkableUrl) {
		String uuid = UUID.randomUUID().toString();
		final String fullUrl = emfProtocol + "://" + hostName + ":" + port + bookmarkableUrl;
		LOGGER.debug("The constructed instance url is: {} ", fullUrl);
		// .html extension at the end is very important - wkhtmltopdf won't read
		// the file if not there
		String generatedPdfPath = tempDirPath + "/EMF/" + uuid;
		try {
			final ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(prepareCommandArguments(fullUrl, generatedPdfPath
					+ PDF_FILE_EXTENSION));

			ProcessCaller processCaller = new ProcessCaller(processBuilder);
			try {
				// call the process timed and terminate if not finieshed within 40 seconds.
				timedCall(processCaller, EXPORT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				processCaller.terminate();
				// Handle timeout here
				LOGGER.error("Export, using wkhmltopdf, didn't manage to "
						+ "finish within the timeout period of " + EXPORT_TIMEOUT_IN_SECONDS
						+ " seconds. Process was successfully terminated.", e);
			} catch (ExecutionException e) {
				LOGGER.error("Unexpected error during wkhmltopdf process execution.", e);
			}
			LOGGER.debug("PDF file with name : {} was saved and ready for sending to client!",
					generatedPdfPath);
			// delete the html file
		} catch (InterruptedException e) {
			throw new RuntimeException("Error while generating PDF", e);
		}
		return generatedPdfPath;
	}

	/**
	 * Gets the script from file.
	 * 
	 * @param filePath
	 *            the file path
	 * @return the script from file
	 */
	private String getScriptFromFile(final String filePath) {
		LOGGER.debug("Reading javascript for execution from: {}", filePath);

		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}
		int length = (int) file.length();
		if (length <= 0) {
			length = 16;
		}
		StringBuilder stringBuilder = new StringBuilder(length);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line.trim());
				stringBuilder.append(" ");
			}
		} catch (IOException e) {
			LOGGER.error("Unable to find or read js file: ", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error("Unable to close stream: ", e);
				}
			}
		}
		stringBuilder.setLength(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}

	/**
	 * Timed call.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param c
	 *            the {@link Callable}
	 * @param timeout
	 *            the timeout
	 * @param timeUnit
	 *            the time unit of the timeout (i. e. seconds)
	 * @return the t
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 * @throws TimeoutException
	 *             the timeout exception
	 */
	private <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
			throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<T> task = new FutureTask<T>(c);
		THREAD_POOL.execute(task);
		return task.get(timeout, timeUnit);
	}

}
