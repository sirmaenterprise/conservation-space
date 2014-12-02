package com.sirma.itt.emf.cls.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import jxl.Sheet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.cls.event.CodeListUploadEvent;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.XLSProcessor;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorException;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.rest.RestServiceConstants;

/**
 * Provides REST service for uploading excel files containing code lists and values.
 * 
 * @author Mihail Radkov
 */
@Path("/codelists/upload")
@Stateless
public class CodeListUploadService extends EmfRestService {

	/** Excel validator. */
	@Inject
	private XLSValidator validator;

	/** Code list persister. */
	@Inject
	private XLSProcessor persister;

	/** Logs actions related to this class. */
	@Inject
	private static final Logger LOGGER = Logger.getLogger(CodeListUploadService.class);

	/** Service used for firing events. */
	@Inject
	private EventService eventService;

	/**
	 * REST service for receiving a http request containing an excel file. Extracts the excel file
	 * from the request, validates and parses it and finally returns a response from the whole
	 * operation. If the request contains correctly formatted excel file, the response will be with
	 * status code 202 (Accepted). If not, then the status code will be 400 (Bad Request) with
	 * message as content why it's not accepted.
	 * 
	 * @param req
	 *            the http request
	 * @return response with status code and message in JSON
	 */
	@POST
	@Consumes("multipart/form-data")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response uploadProcess(@Context HttpServletRequest req) {
		InputStream fileStream = getUploadedFile(req);
		if (fileStream == null) {
			return buildResponse(400, "error", "No correct file was attached.");
		}

		Sheet codelistSheet;
		try {
			codelistSheet = validator.getValidatedCodeListSheet(fileStream);
		} catch (XLSValidatorException e) {
			log.error(e.getMessage(), e);
			return buildResponse(400, "error", e.getMessage());
		} finally {
			// TODO: Quietly or not?
			IOUtils.closeQuietly(fileStream);
		}

		if (codelistSheet != null) {
			try {
				persister.persistSheet(codelistSheet);
			} catch (PersisterException e) {
				log.error(e.getMessage(), e);
				return buildResponse(400, "error", e.getMessage());
			}
			return buildResponse(202, "ok", "Successfully uploaded and stored in the database.");
		}
		return buildResponse(400, "error", "Incorrect file upload.");
	}

	/**
	 * Builds a response with the status code and result as JSON.
	 * 
	 * @param statusCode
	 *            the status code for the response
	 * @param result
	 *            result type (error or ok)
	 * @param resultMessage
	 *            the result message
	 * @return the builded response
	 */
	private Response buildResponse(int statusCode, String result, String resultMessage) {
		// TODO: Use JSONObject ?
		String content = "{\"result\":\"" + result + "\",\"resultMessage\":\"" + resultMessage
				+ "\"}";
		eventService.fire(new CodeListUploadEvent(resultMessage));
		LOGGER.debug(resultMessage);
		return Response.status(statusCode).header("Content-type", "application/json")
				.entity(content).build();
	}

	/**
	 * Iterates through a http request's items and retrieves the uploaded file's input stream (if
	 * there is such file).
	 * 
	 * @param req
	 *            the http request
	 * @return input stream to the uploaded file or null
	 * @see http://ankiewsky.blogspot.com/2008/07/resteasy-tips-part-1.html
	 */
	private InputStream getUploadedFile(HttpServletRequest req) {
		if (req != null) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items;

			try {
				items = upload.parseRequest(req);
				Iterator<FileItem> iter = items.iterator();
				FileItem item = null;

				while (iter.hasNext()) {
					item = iter.next();
					// TODO: Check field's name/type? (datafile)
					if (item != null && !item.isFormField()) {
						return item.getInputStream();
					}
				}
			} catch (FileUploadException e) {
				LOGGER.debug("File upload exception occured while handling the http request.", e);
			} catch (IOException e) {
				LOGGER.debug("IO exception.", e);
			}
		}
		return null;
	}

}
