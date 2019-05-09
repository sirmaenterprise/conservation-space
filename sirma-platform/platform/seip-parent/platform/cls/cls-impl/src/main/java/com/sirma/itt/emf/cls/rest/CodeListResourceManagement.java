package com.sirma.itt.emf.cls.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.util.JxlUtils;
import com.sirma.itt.emf.cls.validator.SheetValidator;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.parser.CodeListSheet;
import com.sirma.sep.content.upload.UploadRequest;

import jxl.Sheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Provides REST endpoints for managing and retrieving codelists.
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
@AdminResource
@Path("/codelists")
@Transactional
public class CodeListResourceManagement {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SheetValidator sheetValidator;

	@Inject
	private CodeListPersister persister;

	@Inject
	private SheetParser sheetParser;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private CodeListService codeListService;

	@Inject
	private EventService eventService;

	/**
	 * Retrieves a single {@link CodeList} along with their {@link com.sirma.sep.cls.model.CodeValue}.
	 *
	 * @return a response with the code list
	 */
	@GET
	@Path("/{codelist}")
	@Produces(MediaType.APPLICATION_JSON)
	public CodeList getCodeList(@PathParam("codelist") String codelist) {
		Optional<CodeList> codeList = codeListService.getCodeList(codelist, true);
		return codeList.orElseThrow(() -> new ResourceNotFoundException("Requested code list does not exist"));
	}

	/**
	 * Retrieves the available {@link CodeList} along with their {@link com.sirma.sep.cls.model.CodeValue}.
	 *
	 * @return a response with the code lists
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<CodeList> getCodeLists() {
		return codeListService.getCodeLists(true);
	}

	/**
	 * Rest service providing ability to export code lists as an excel file contained inside the payload of the response
	 * The response contains the Base64 encoded contents of the excel workbook file. This service exports all code lists
	 * contained in the system unconditionally.
	 *
	 * @return response with status code and message in JSON
	 */
	@GET
	@Path("export")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportCodeLists() {
		try {
			List<CodeList> results = codeListService.getCodeLists(true);
			CodeListSheet codeListSheet = new CodeListSheet();
			codeListSheet.setCodeLists(results);

			File codeListsFile = tempFileProvider.createTempFile("codes", null);
			WritableWorkbook workbook = JxlUtils.createWorkbook(codeListsFile);
			sheetParser.parseFromList(codeListSheet, workbook);
			workbook.close();

			return Response.ok(codeListsFile, MediaType.APPLICATION_OCTET_STREAM).build();
		} catch (IOException | WriteException e) {
			LOGGER.error(e.getMessage(), e);
			return buildResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * Rest service for receiving a http request containing an excel file. Extracts the excel file from the request,
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
	@Path("upload/overwrite")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadCodelists(UploadRequest req) {
		Sheet sheet;
		try (InputStream fileStream = req.getRequestItems().get(0).getInputStream()) {
			sheet = sheetValidator.getValidatedCodeListSheet(fileStream);
			CodeListSheet codeLists = sheetParser.parseFromSheet(sheet);

			persister.override(codeLists);
			eventService.fire(new ResetCodelistEvent());

			return buildResponse(HttpStatus.SC_OK, "Code lists uploaded successfully");
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return buildResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage());
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
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addCodelists(UploadRequest req) {
		Sheet sheet;
		try (InputStream fileStream = req.getRequestItems().get(0).getInputStream()) {
			sheet = sheetValidator.getValidatedCodeListSheet(fileStream);
			CodeListSheet codeListSheet = sheetParser.parseFromSheet(sheet);

			this.persister.persist(codeListSheet.getCodeLists());
			eventService.fire(new ResetCodelistEvent());

			return buildResponse(HttpStatus.SC_OK, "Code lists uploaded successfully");
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return buildResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * Updates the data for the given code list and its values.
	 *
	 * @param codeList
	 *            is the request JSON object of the updated code list
	 * @return response with status code and message in JSON
	 */
	@POST
	@Path("/{codeListID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCodelist(CodeList codeList) {
		persister.persist(codeList);
		eventService.fire(new ResetCodelistEvent());
		return buildResponse(HttpStatus.SC_OK, "Code list updated successfully");
	}

	/**
	 * Builds a response with the status code and result as JSON.
	 *
	 * @param statusCode
	 *            the status code for the response
	 * @param message
	 *            the result message
	 * @return the built response
	 */
	private static Response buildResponse(int statusCode, String message) {
		JsonObject content = getBaseBuilder(statusCode).add("message", message).build();
		return buildResponse(statusCode, content);
	}

	/**
	 * Builds a response with the status code and JSON object as a result message
	 *
	 * @param statusCode
	 *            the status code for the response
	 * @param content
	 *            the JSON content object
	 * @return the built response
	 */
	private static Response buildResponse(int statusCode, JsonObject content) {
		return Response.status(statusCode)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.entity(content.toString())
				.build();
	}

	/**
	 * Gets the basic JSON object builder with a built specific status code
	 * 
	 * @param statusCode
	 *            the status code
	 * @return the JSON object builder
	 */
	private static JsonObjectBuilder getBaseBuilder(int statusCode) {
		return Json.createObjectBuilder().add("result", HttpStatus.getStatusText(statusCode));
	}
}
