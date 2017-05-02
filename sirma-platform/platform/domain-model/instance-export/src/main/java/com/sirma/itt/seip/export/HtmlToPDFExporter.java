package com.sirma.itt.seip.export;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;

/**
 * Utility class used for exporting specific object to PDF format.
 *
 * @author Ivo Rusev
 */
@ApplicationScoped
public class HtmlToPDFExporter implements PDFExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlToPDFExporter.class);

	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	private static final String EXPORT = "export";

	private static final String PDF_FILE_EXTENSION = ".pdf";

	private File exportDir;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "export.pdf.timeout", type = Integer.class, defaultValue = "300", label = "Specify the export timeout in seconds. After this time the execution of the export process will be interrupted.")
	private ConfigurationProperty<Integer> timeout;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "phantomjs.location", system = true, sensitive = true, shared = false, label = "Specifies the full path to the phantomjs executable file used to convert html documents to PDF.")
	private ConfigurationProperty<String> phantomjsLocation;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "phantomjs.arguments", type = Set.class, system = true, sensitive = true, shared = false, label = "Specifies command line arguments to be passed to phantomjs process.")
	private ConfigurationProperty<Set<String>> phantomjsArguments;

	@Inject
	private SystemConfiguration systemConfiguration;

	@PostConstruct
	protected void initialize() {
		exportDir = tempFileProvider.createLongLifeTempDir(EXPORT);
	}

	@Override
	public File export(final String bookmarkableUrl, StringPair... cookies) throws TimeoutException {
		String uuid = UUID.randomUUID().toString();
		String ui2Url = systemConfiguration.getUi2Url().get();
		if (ui2Url.endsWith("/")) {
			ui2Url = ui2Url.substring(0, ui2Url.length() - 1);
		}
		final String fullUrl = ui2Url + bookmarkableUrl;
		LOGGER.debug("The constructed instance url is: {} ", fullUrl);

		File generatedPdf = getWorkingFileById(uuid);
		File exportPDFTempFile = getExportPDFJSTempFile();

		ProcessCaller processCaller = null;
		try {
			final ProcessBuilder processBuilder = new ProcessBuilder();
			List<String> arguments = prepareCommandArguments(exportPDFTempFile.getAbsolutePath(), fullUrl,
					generatedPdf, cookies);
			if (arguments.isEmpty()) {
				return null;
			}
			processBuilder.command(arguments);

			processCaller = new ProcessCaller(processBuilder);
			// call the process timed and terminate if not finieshed within 40
			// seconds.
			timedCall(processCaller, timeout.get(), TimeUnit.SECONDS);
			LOGGER.debug("PDF file with name : {} was saved and ready for sending to client!", generatedPdf);
			// delete the html file
		} catch (InterruptedException e) {
			LOGGER.error("Error while generating PDF", e);
		} catch (ExecutionException e) {
			LOGGER.error("Unexpected error during process execution.", e);
		} catch (TimeoutException e) {
			processCaller.terminate();
			// Handle timeout here
			LOGGER.error("Export didn't manage to finish within the timeout period of " + timeout.get()
					+ " seconds. Process was successfully terminated.", e);
			throw e;
		} finally {
			if (exportPDFTempFile.exists()) {
				exportPDFTempFile.delete();
			}
		}
		return generatedPdf;
	}

	/**
	 * Constructs working file name by given unique name of the file.
	 *
	 * @param uuid
	 *            the file name of the generated file, without extension
	 * @return the working file location for this file name
	 */
	public File getWorkingFileById(String uuid) {
		if (StringUtils.isNullOrEmpty(uuid)) {
			return null;
		}

		if (!exportDir.exists() && !exportDir.mkdirs()) {
			throw new EmfRuntimeException(
					exportDir + " directory does not exists and could not be created! Export could not continue!");
		}
		return new File(exportDir, uuid + PDF_FILE_EXTENSION);
	}

	/**
	 * Prepare command arguments for exporting to PDF tool.
	 *
	 * @param exportPDFJsFile
	 *            absolute path to exportPDF.js (temp) file
	 * @param inputUrl
	 *            bookmarkable url of the instance
	 * @param cookies
	 *            are the current cookies
	 * @param outputFile
	 *            generated pdf file
	 * @return the list of command line parameters
	 */
	private List<String> prepareCommandArguments(String exportPDFJsFile, String inputUrl, File outputFile,
			StringPair... cookies) {
		if (phantomjsLocation.isNotSet()) {
			LOGGER.warn("{} not configured!", phantomjsLocation.getName());
			return Collections.emptyList();
		}
		List<String> arguments = new ArrayList<>(8);
		arguments.add(phantomjsLocation.get());

		if (phantomjsArguments.isSet()) {
			phantomjsArguments.get().forEach(argument -> arguments.add(argument));
		}

		arguments.add("--local-storage-path=" + outputFile.getAbsoluteFile().getParentFile().getAbsolutePath());

		arguments.add(exportPDFJsFile);
		arguments.add(inputUrl);
		arguments.add(outputFile.getAbsolutePath());

		try {
			URL ui2URL = new URL(systemConfiguration.getUi2Url().get());
			arguments.add("--domain");
			arguments.add(ui2URL.getHost());
		} catch (MalformedURLException e) {
			LOGGER.error("Error obtaining UI2 host.", e);
		}

		arguments.addAll(prepareAuthenticationArguments(cookies));

		LOGGER.debug("Prepared command line arguments for exporting to PDF: {}", arguments);

		return arguments;
	}

	/**
	 * Exports exportPDF.js file from system resource to temp file to be used outside of the application.
	 *
	 * @return exportPDF.js temp file
	 */
	private File getExportPDFJSTempFile() {
		File exportPDFTempFile = tempFileProvider.createTempFile("exportPDF", ".js");
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("/export/exportPDF.js");
				OutputStream out = FileUtils.openOutputStream(exportPDFTempFile);) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			LOGGER.error("Error loading exportPDF.js file", e);
		}
		return exportPDFTempFile;
	}

	/**
	 * Get phantomjs location from configuration
	 *
	 * @return phantomjs location
	 */
	public ConfigurationProperty<String> getPhantomjsLocation() {
		return phantomjsLocation;
	}

	/**
	 * Prepares the authentication. If an already existing cookie is provided (which typically happens when you call
	 * export from the web UI and not a script), then we add the cookies to the command arguments.
	 *
	 * @param cookies
	 *            the cookies, which will be used for the authentication
	 * @return list of strings, which elements are used in authentication for the request
	 */
	private static List<String> prepareAuthenticationArguments(StringPair... cookies) {
		List<String> authArguments = new ArrayList<>();
		if (cookies != null && cookies.length > 0) {
			for (StringPair cookie : cookies) {
				authArguments.add("--cookie");
				authArguments.add(cookie.getFirst());
				authArguments.add(cookie.getSecond());
			}
		}
		return authArguments;
	}

	/**
	 * Timed call.
	 *
	 * @param <T>
	 *            the generic type
	 * @param callable
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
	private static <T> T timedCall(Callable<T> callable, long timeout, TimeUnit timeUnit)
			throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<T> task = new FutureTask<>(callable);
		THREAD_POOL.execute(task);
		return task.get(timeout, timeUnit);
	}

}
