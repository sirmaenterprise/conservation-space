package com.sirma.itt.emf.cls.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.persister.XLSProcessor;
import com.sirma.itt.emf.cls.retriever.CodeListSearchCriteria;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorException;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.content.upload.UploadRequest;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;

import jxl.Sheet;

/**
 * Provides REST service for uploading excel files containing code lists and values.
 *
 * @author Mihail Radkov
 */
@AdminResource
@Path("/codelists/upload")
public class CodeListUploadService extends EmfRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodeListUploadService.class);

	@Inject
	private XLSValidator validator;

	@Inject
	private XLSProcessor processor;

	@Inject
	private SheetParser sheetParser;

	@Inject
	private CodeListService codeListService;

	/**
	 * REST service for receiving a http request containing an excel file. Extracts the excel file from the request,
	 * validates and parses it and finally returns a response from the whole operation. If the request contains
	 * correctly formatted excel file, the response will be with status code 202 (Accepted). If not, then the status
	 * code will be 400 (Bad Request) with message as content why it's not accepted. The old codelists, if any will be
	 * deleted before persisting the new ones.
	 *
	 * @param req
	 *            the http request
	 * @return response with status code and message in JSON
	 */
	@POST
	@Path("/overwrite")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadCodelists(UploadRequest req) {
		Sheet sheet;
		try (InputStream fileStream = req.getRequestItems().get(0).getInputStream()) {
			sheet = validator.getValidatedCodeListSheet(fileStream);
			processor.persistSheet(sheetParser.parseXLS(sheet));
			return buildResponse(202, "ok", "Successfully uploaded and stored in the database.");
		} catch (IOException | XLSValidatorException | PersisterException e) {
			LOGGER.error(e.getMessage(), e);
			return buildResponse(400, "error", e.getMessage());
		}
	}

	/**
	 * Add new codelists to the already-existing ones, in other words merging the new ones with the old codelists.
	 *
	 * @param req
	 *            the http request
	 * @return response with status code and message in JSON
	 */
	@POST
	@SuppressWarnings("unchecked")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addCodelists(UploadRequest req) {
		Sheet sheet;
		try (InputStream fileStream = req.getRequestItems().get(0).getInputStream()) {
			sheet = validator.getValidatedCodeListSheet(fileStream);
			CodeListSheet existingCodeLists = new CodeListSheet();
			existingCodeLists.setCodeLists(
					(List<CodeList>) codeListService.getCodeLists(new CodeListSearchCriteria()).getResults());
			CodeListSheet codeListSheet = sheetParser.parseXLS(sheet);

			CodeListSheet mergedSheets = processor.mergeSheets(Arrays.asList(existingCodeLists, codeListSheet));
			processor.persistSheet(mergedSheets);
			return buildResponse(202, "ok", "Successfully uploaded and stored in the database.");
		} catch (IOException | XLSValidatorException | PersisterException e) {
			LOGGER.error(e.getMessage(), e);
			return buildResponse(400, "error", e.getMessage());
		}
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
	 * @return the built response
	 */
	private static Response buildResponse(int statusCode, String result, String resultMessage) {
		JsonObject content = Json
				.createObjectBuilder()
					.add("result", result)
					.add("resultMessage", resultMessage)
					.build();

		LOGGER.debug(resultMessage);
		return Response.status(statusCode).header("Content-type", "application/json").entity(content.toString()).build();
	}

}
