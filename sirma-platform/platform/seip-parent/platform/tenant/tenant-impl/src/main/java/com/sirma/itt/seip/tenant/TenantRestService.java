package com.sirma.itt.seip.tenant;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationExternalModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder.Builder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelRetriever;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Administrative rest service for tenant management.
 *
 * @author bbanchev
 */
@Path("/tenant")
@ApplicationScoped
public class TenantRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TENANT_MODEL_KEY = "tenantmodel";
	private static final String SEMANTICDB_UPDATE_KEY = "SemanticDbUpdate_attachment_patches";
	private static final String DMS_INITIALIZATION_KEY = "DMSInitialization_attachment_definitions";

	@Inject
	private TenantManagementService tenantService;

	@Inject
	private TenantInitializationModelBuilder modelBuilder;

	@Inject
	private TenantInitializationModelRetriever modelRetriever;

	@Inject
	private TenantInitializationStatusService statusService;

	@Inject
	private TaskExecutor executor;

	/**
	 * Creates a new tenant using the model provided
	 *
	 * @param form is the multipart form model for tenant initialization
	 * @return the response from the execution step
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response create(MultipartFormDataInput form) {
		try {
			Builder builder = provideBuilder(form);
			TenantInitializationModel model = builder.build();
			String tenantId = model.get("TenantInitialization").getPropertyValue("tenantid", true);
			if (statusService.isInProgress(tenantId)) {
				return getTenantOperationInProgressResponse(tenantId, "create");
			}
			statusService.setStatus(tenantId, TenantInitializationStatusService.Status.IN_PROGRESS,
					"Tenant creation is starting...");
			executor.executeAsync(() -> tenantService.create(model));
		} catch (Exception e) {
			LOGGER.trace("Tenant creation failed with error", e);
			String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(message).build();
		}
		return Response.ok().build();
	}

	/**
	 * Delete the tenant.
	 *
	 * @param tenantId the tenant id
	 * @return the response from the execution step
	 */
	@DELETE
	@Path("{tenantId}")
	public Response delete(@PathParam("tenantId") String tenantId) {
		try {
			if (statusService.isInProgress(tenantId)) {
				return getTenantOperationInProgressResponse(tenantId, "delete");
			}
			statusService.setStatus(tenantId, TenantInitializationStatusService.Status.IN_PROGRESS,
					"Tenant deletion is starting...");
			tenantService.delete(tenantId);
		} catch (Exception e) {
			LOGGER.trace("Tenant deletion failed with error", e);
			String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			if (!statusService.hasFailed(tenantId)) {
				statusService.setStatus(tenantId, TenantInitializationStatusService.Status.FAILED, message);
			}
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(message).build();
		}
		return Response.ok().build();
	}

	/**
	 * Updates tenant with given form data
	 *
	 * @param tenantId ID of the tenant
	 * @param form is the multipart form model for tenant initialization
	 * @return the response from the execution step
	 */
	@Path("{tenantId}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response update(@PathParam("tenantId") String tenantId, MultipartFormDataInput form) {
		try {
			if (statusService.isInProgress(tenantId)) {
				return getTenantOperationInProgressResponse(tenantId, "update");
			}
			statusService.setStatus(tenantId, TenantInitializationStatusService.Status.IN_PROGRESS,
					"Tenant update is starting...");
			Builder builder = provideBuilder(form);
			tenantService.update(builder.build(), tenantId);
		} catch (Exception e) {
			LOGGER.trace("Tenant update failed with error", e);
			if (!statusService.hasFailed(tenantId)) {
				statusService.setStatus(tenantId, TenantInitializationStatusService.Status.FAILED, e.getMessage());
			}
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(e).build();
		}
		return Response.ok().build();
	}

	/**
	 * Uploads an ontology model for a given tenant. The ontology model might be represented as an archive or a list of
	 * individual files to be uploaded.
	 *
	 * @param tenantId the tenant id for which to upload the ontology model
	 * @param req the upload request containing a list of {@link FileItem}s
	 * @return the response from the execution step
	 */
	@POST
	@Path("upload/ontology/{tenantId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadOntologyModel(@PathParam("tenantId") String tenantId, UploadRequest req) {
		try {
			if (statusService.isInProgress(tenantId)) {
				return getTenantOperationInProgressResponse(tenantId, "update");
			}
			statusService.setStatus(tenantId, TenantInitializationStatusService.Status.IN_PROGRESS,
					"Tenant update is starting...");

			// parse file items from the provided request
			List<FileItem> fileItems = req.getRequestItems();

			// create tenant initialization model builder
			Builder builder = modelBuilder.getBuilder();
			builder.setModel(getDefaultTenantModel());
			fileItems.forEach(item -> appendModel(builder, SEMANTICDB_UPDATE_KEY, item));

			// update on the specified tenant with a model
			tenantService.update(builder.build(), tenantId);
		} catch (Exception e) {
			LOGGER.trace("Tenant model update failed with error", e);
			if (!statusService.hasFailed(tenantId)) {
				statusService.setStatus(tenantId, TenantInitializationStatusService.Status.FAILED, e.getMessage());
			}
			return Response.serverError().type(MediaType.TEXT_PLAIN).entity(e.getMessage()).build();
		}
		return Response.ok().build();
	}

	/**
	 * Returns list of ids of active tenants
	 *
	 * @return list of ids of active tenants
	 */
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Tenant> getTenants() {
		return tenantService.getTenants();
	}

	/**
	 * Retrieve the current tenant creation process' status.
	 *
	 * @param tenantId ID of the tenant
	 * @return the status.
	 */
	@GET
	@Path("status/{tenantId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus(@PathParam("tenantId") String tenantId) {
		Pair<TenantInitializationStatusService.Status, String> status = statusService.getStatus(tenantId);
		if (status == null) {
			return Response.ok().entity("No available status for tenant creation process for that tenant!").build();
		}
		if (statusService.isCompleted(tenantId)) {
			return Response.ok().entity(status.getSecond()).build();
		} else if (statusService.hasFailed(tenantId)) {
			return Response.serverError().entity(status.getSecond()).build();
		}
		return Response.accepted().entity("Tenant creation is currently at step " + status.getSecond()).build();
	}

	@GET
	@Path("models")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<TenantInitializationExternalModel> getModels() {
		try {
			return modelRetriever.getModels();
		} catch (TenantCreationException e) {
			LOGGER.warn("No tenant creation models could be loaded");
			LOGGER.trace("No tenant creation models could be loaded", e);
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Provides the blank model for new tenant
	 *
	 * @return the json response entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBlankModel() {
		TenantInitializationModel model = tenantService.provideModel();
		return Response.ok(model.toJSONObject().toString()).build();
	}

	/**
	 * Create a tenant operation already in progress response.
	 *
	 * @param tenantId the tenant id
	 * @param operation the operation
	 */
	private Response getTenantOperationInProgressResponse(String tenantId, String operation) {
		return Response
				.serverError()
				.entity("Another operation is being performed on this tenant ("
						+ statusService.getStatus(tenantId).getSecond() + "). You can't " + operation
						+ " this tenant right now!")
				.build();
	}

	private Builder provideBuilder(MultipartFormDataInput form) throws IOException {
		try {
			// get the base JSON request
			Map<String, List<InputPart>> formDataMap = form.getFormDataMap();

			// try to fetch the provided tenant model from the form
			List<InputPart> data = formDataMap.remove(TENANT_MODEL_KEY);

			// initialize the builder with default model
			Builder builder = modelBuilder.getBuilder();
			builder.setModel(getTenantModel(data));

			// extract the external model ids.
			String models = readPart(formDataMap.remove("DMSInitialization_attachment_path"), String.class);

			if (StringUtils.isNotBlank(models)) {
				String[] modelIds = models.split(",");
				for (String modelId : modelIds) {
					TenantInitializationExternalModel model = modelRetriever.getModel(modelId);
					appendModel(builder, SEMANTICDB_UPDATE_KEY, model.getSemanticPath());
					appendModel(builder, DMS_INITIALIZATION_KEY, model.getDefinitionsPath());
				}
			} else {
				for (Entry<String, List<InputPart>> entry : formDataMap.entrySet()) {
					InputStream input = readPart(entry.getValue(), InputStream.class);
					builder.appendModelFile(entry.getKey(), getFileName(entry.getValue().get(0).getHeaders()), input);
				}
			}
			return builder;
		} catch (Exception e) {
			throw new TenantCreationException("Tenant creation failure!", e);
		}
	}

	/**
	 * Creates a default and empty tenant model
	 *
	 * @return the default tenant model as a {@link JSONObject}
	 */
	private static JSONObject getDefaultTenantModel() {
		return getTenantModel(null);
	}

	/**
	 * Creates a JSON tenant model representation. The model content is parsed from the input part When no data is
	 * provided a default model is instead created.
	 *
	 * @return the created tenant model as a {@link JSONObject}
	 */
	private static JSONObject getTenantModel(List<InputPart> data) {
		if (data == null) {
			try {
				JSONObject model = new JSONObject();
				return model.put("data", new JSONArray());
			} catch (JSONException e) {
				LOGGER.trace("", e);
				LOGGER.warn("Unable to create empty tenant default model");
			}
		} else {
			try {
				String rawData = readPart(data, String.class);
				String decoded = URLDecoder.decode(rawData, StandardCharsets.UTF_8.name());
				return JsonUtil.createObjectFromString(decoded);
			} catch (IOException e) {
				LOGGER.trace("", e);
				LOGGER.warn("Unable to create tenant model from specified data");
			}
		}
		return null;
	}

	private static void appendModel(Builder builder, String key, FileItem item) {
		try {
			builder.appendModelFile(key, getFileName(item), item.getInputStream());
		} catch (IOException e) {
			throw new EmfRuntimeException("Provided file item is invalid or corrupted: " + item.getName(), e);
		}
	}

	private void appendModel(Builder builder, String key, String path) {
		if (StringUtils.isNotBlank(path)) {
			builder.appendModelFile(key, FilenameUtils.getName(path), modelRetriever.getPathInputStream(path));
		}
	}

	private static <R> R readPart(List<InputPart> entries, Class<R> clazz) throws IOException {
		if (CollectionUtils.isEmpty(entries)) {
			return null;
		}
		if (entries.size() != 1) {
			throw new EmfRuntimeException(
					"Tenant model currently supports only single form entry but is provided: " + entries);
		}
		InputPart part = entries.get(0);
		return part.getBody(clazz, Class.class);
	}

	private static String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if (filename.trim().startsWith("filename")) {
				String[] name = filename.split("=");
				return name[1].trim().replaceAll("\"", "");
			}
		}
		return null;
	}

	private static String getFileName(FileItem file) {
		return FilenameUtils.getName(file.getName());
	}
}
