package com.sirma.itt.idoc.web.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.util.PDFExporter;

/**
 * Rest API for exporting html to different file formats.
 * 
 * @author yasko
 * @author Ivo Rusev
 */
@Path("/export")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ExportDocumentRestService {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ExportDocumentRestService.class);
	/** The pdf file extension. */
	private static final String PDF_FILE_EXTENSION = ".pdf";
	/** The tmp directory path. */
	@Inject
	@Config(name = EmfConfigurationProperties.TEMP_DIR)
	private String tempDirPath;

	@Inject
	private PDFExporter pdfExporter;

	/**
	 * Exports html to a PDF file and streams it back to the client.
	 * 
	 * @param req
	 *            the {@link HttpServletRequest}
	 * @param filename
	 *            Filename of the generated PDF (optional)
	 * @param url
	 *            the url
	 * @return {@link Response}
	 */
	@POST
	public String exportToPdf(@Context HttpServletRequest req, @QueryParam("fileName") String filename,
			String url) {
		String fileLocation = pdfExporter.exportToPdf(url);
		String[] temp = fileLocation.split("/");
		String fileId = temp[temp.length - 1];
		return "{\"fileName\":\"" + fileId + "\"}";
	}

	/**
	 * Sends the file to the browser to upload.
	 * 
	 * @param fileName
	 *            the file name
	 * @return {@link Response}
	 */
	@GET
	@Path("/{fileName}")
	@Produces("application/pdf")
	public Response download(@PathParam("fileName") final String fileName) {
		LOGGER.debug("Calling the download GET service to send the file to the browser.");
		final String generatedPdfPath = tempDirPath + "/EMF/" + fileName + PDF_FILE_EXTENSION;

		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				File input = new File(generatedPdfPath);
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(input);
					IOUtils.copy(fis, os);
				} finally {
					if (fis != null) {
						fis.close();
					}
					input.delete();
				}
			}
		};

		ResponseBuilder response = Response.ok(stream);
		response.header("Content-Disposition", "attachment; filename=\"" + fileName + ".pdf\"");

		return response.build();
	}
}
