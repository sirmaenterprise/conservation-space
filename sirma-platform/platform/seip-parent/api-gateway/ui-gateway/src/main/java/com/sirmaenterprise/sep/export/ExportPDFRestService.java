package com.sirmaenterprise.sep.export;

import java.io.File;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.content.rest.ContentDownloadService;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.pdf.PDFExportRequest;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;

/**
 * Web service to route requests to export server.
 *
 * @deprecated this functionality should go through action implementation, like before, because now it skips for example
 *             audit logging, etc..
 * @author iborisov
 * @author bbanchev
 */
@Deprecated
@Path("/export")
@ApplicationScoped
@Produces(Versions.V2_JSON)
public class ExportPDFRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ExportService exportService;

	@Inject
	private ContentDownloadService contentDownloadService;

	/**
	 * Route export pdf request to export server. Streams the file into the servlet response
	 *
	 * @param response
	 *            the http servlet response context
	 * @param content
	 *            export pdf request content. JSON object with URL to be exported and name for the exported file
	 */
	@POST
	@Path("/pdf")
	@Consumes(Versions.V2_JSON)
	public void exportPDF(@Context HttpServletResponse response, String content) {
		try {
			PDFExportRequest request = transformJsonToExporterRequest(content);
			File exported = exportService.export(request);
			contentDownloadService.sendFile(exported, Range.ALL, false, response, exported.getName(),
					"application/pdf");
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new InternalServerErrorException("There was a problem with the PDF export due to " + e.getMessage());
		}
	}

	private static PDFExportRequest transformJsonToExporterRequest(String content) {
		try (JsonReader reader = Json.createReader(new StringReader(content))) {
			JsonObject json = reader.readObject();
			return new PDFExportRequestBuilder()
					.setInstanceId(json.getString("instanceId", ""))
						.setFileName(json.getString("fileName"))
						.setInstanceURI(URI.create(json.getString("url")))
						.buildRequest();
		}
	}

}