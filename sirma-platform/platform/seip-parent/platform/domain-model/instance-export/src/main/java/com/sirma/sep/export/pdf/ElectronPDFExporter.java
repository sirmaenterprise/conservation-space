package com.sirma.sep.export.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.rest.client.URIBuilderWrapper;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.export.ContentExportException;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.FileExporter;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Provides means for exporting HTML content to PDF file. The implementation uses Electron to export the in PDF file.
 * Electron is used as remote service, to which are build and send specified requests. Those requests contains required
 * information for the export process. <br>
 * This implementation is extension to {@link FileExporter} plug-in and should be used through specified service for the
 * export - {@link ExportService}. <br>
 * The process for exporting is using thread pool and task with configurable timeout, which is used to prevent stacking
 * of the process, if the remote service for the Electron is not available or in case of other problems that may cause
 * slow export. If the specified object/instance could not be exported for the given time, the export will fail. <br>
 * The URL to the remote Electron service is configurable as well, so that it could be easily changed or redirected, if
 * such thing is required.
 *
 * @author Ivo Rusev
 * @author bbanchev
 */
@Extension(target = FileExporter.PLUGIN_NAME, order = 10)
public class ElectronPDFExporter implements FileExporter<PDFExportRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
	private static final ContentType REQUEST_CONTENT_TYPE = ContentType.create(Versions.V2_JSON,
			StandardCharsets.UTF_8);

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "export.server.url", shared = false, label = "Base address of the export service <scheme>://<host>:<port>.")
	private ConfigurationProperty<String> exportServerURL;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "export.pdf.timeout", type = Integer.class, defaultValue = "300", label = "Specify the export timeout in seconds. After this time the execution of the export process will be interrupted. Maximum timeout interval is 120 seconds!")
	private ConfigurationProperty<Integer> timeout;

	private File exportDir;

	@PostConstruct
	protected void initialize() {
		exportDir = tempFileProvider.createLongLifeTempDir("export");
	}

	@Override
	public Optional<File> export(final PDFExportRequest request) throws ContentExportException {
		if (exportServerURL.isNotSet()) {
			LOGGER.error("{} is not configured!", exportServerURL.getName());
			return Optional.empty();
		}

		URI exportServiceAddress = getExportAddress();

		Integer configuredTimeout = timeout.get();
		// TODO: A temporary timeout restriction which is necessary because of the fact that the export service which
		// this service invokes, timeouts in 2min for unknown reason. That's why we don't allow bigger timeout until the
		// reason is found and fixed.
		// https://git.sirmaplatform.com/stash/projects/SEIP/repos/export/pull-requests/24/overview
		if (configuredTimeout > 120) {
			configuredTimeout = 120;
		}
		Integer exportPdfTimeout = configuredTimeout;

		return timedCall(() -> executeRequest(exportServiceAddress, request, file(), exportPdfTimeout), exportPdfTimeout, TimeUnit.SECONDS);
	}

	private <T> Optional<T> executeRequest(URI exportUrl, final PDFExportRequest request, BiFunction<Integer, HttpResponse, T> reader, Integer exportPdfTimeout)
			throws ContentExportException {
		HttpEntityEnclosingRequestBase post = new HttpPost(exportUrl);

		String payload = requestToJson(request, TimeUnit.SECONDS.toMillis(exportPdfTimeout));
		post.setEntity(new StringEntity(payload, REQUEST_CONTENT_TYPE));
		HttpHost httpHost = new HttpHost(exportUrl.getHost(), exportUrl.getPort(), exportUrl.getScheme());
		try {
			return Optional.of(new HTTPClient().execute(post, null, httpHost, reader, logError(exportUrl)));
		} catch (Exception e) {
			// get the reader/error function exception cause
			throw new ContentExportException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}
	}

	private URI getExportAddress() {
		return URIBuilderWrapper.createURIByPaths(exportServerURL.get(), "/export/pdf");
	}

	/**
	 * Creates {@link JsonObject} used as request to the remote Electron service.
	 *
	 * @param request
	 *            is the source request
	 *
	 * @param timeoutMillis maximum time in milliseconds that the export service has to export the document
	 *
	 * @return json object containing at the keys
	 *         <ul>
	 *         <li>url - the full uri to download view (specific tab, idoc, etc)</li>
	 *         <li>timeout - timeout in milliseconds for the service to export the document</li>
	 *         <li>file-name - the file name to export pdf as (optional)</li>
	 *         </ul>
	 */
	private static String requestToJson(PDFExportRequest request, long timeoutMillis) {
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("url", request.getInstanceURI().toASCIIString())
				.add("timeout", timeoutMillis);

		String fileName = request.getFileName();
		if (StringUtils.isNotBlank(fileName)) {
			builder.add("file-name", fileName);
		}

		return builder.build().toString();
	}

	private static <T> Function<IOException, T> logError(final URI targetURI) {
		return error -> {
			LOGGER.debug("Could not complete pdf export to {}!", targetURI, error);
			throw new EmfRuntimeException("Could not complete pdf export. See log for more details!");
		};
	}

	private static HttpEntity readResponse(Integer code, HttpResponse response) {
		if (code.intValue() == HttpStatus.SC_OK) {
			return response.getEntity();
		}
		throw new EmfRuntimeException(
				"Pdf export encountered error - remote server reported " + response.getStatusLine());
	}

	private BiFunction<Integer, HttpResponse, File> file() {
		return (code, response) -> {
			HttpEntity responseStream = readResponse(code, response);
			File createdTempFile = getWorkingFileById();
			try (FileOutputStream output = new FileOutputStream(createdTempFile)) {
				IOUtils.copy(responseStream.getContent(), output);
			} catch (Exception e) {
				throw new EmfRuntimeException("Failed to download and store pdf as local file!", e);
			}
			return createdTempFile;
		};
	}

	/**
	 * Constructs working file name by given unique name of the file.
	 *
	 * @return the working file location for this file name
	 */
	private File getWorkingFileById() {
		if (!exportDir.exists() && !exportDir.mkdirs()) {
			throw new EmfRuntimeException(
					exportDir + " directory does not exists and could not be created! Export could not continue!");
		}

		String name = UUID.randomUUID().toString() + ".pdf";
		return new File(exportDir, name);
	}

	private static <T> T timedCall(Callable<T> callable, long timeout, TimeUnit timeUnit)
			throws ContentExportException {
		try {
			FutureTask<T> task = new FutureTask<>(callable);
			THREAD_POOL.execute(task);
			return task.get(timeout, timeUnit);
		} catch (TimeoutException e) {
			throw new ContentExportException("Failed to receive export result after specified timeout!", e);
		} catch (Exception e) {
			throw new ContentExportException("Failed to execute export!", e);
		}
	}

	@Override
	public String getName() {
		return SupportedExportFormats.PDF.getFormat();
	}

}
