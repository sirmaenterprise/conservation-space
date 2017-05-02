package com.sirma.itt.seip.tenant;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationExternalModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModel;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder.Builder;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelRetriever;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * @author bbanchev
 */
@Path("/tenant")
@ApplicationScoped
public class TenantRestService {
	private static final String KEY_TENANTMODEL_FORMID = "tenantmodel";
	@Inject
	private TenantManagementService tenantService;

	@Inject
	private TenantInitializationModelBuilder modelBuilder;

	@Inject
	private TenantInitializationModelRetriever modelRetriever;

	/**
	 * Creates a new tenant using the model provided
	 *
	 * @param form
	 *            is the multipart form model for tenant initialization
	 * @return the response from the execution step
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional(TxType.REQUIRED)
	public Response create(MultipartFormDataInput form) {
		try {
			Builder builder = provideBuilder(form);
			tenantService.create(builder.build());
		} catch (Exception e) {
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
						.type(MediaType.TEXT_PLAIN)
						.entity(e.getCause().getMessage()).build();
		}
		return Response.ok().build();
	}

	/**
	 * Updates tenant with given form data
	 *
	 * @param tenantId
	 *            ID of the tenant
	 * @param form
	 *            is the multipart form model for tenant initialization
	 * @return the response from the execution step
	 */
	@Path("{tenantId}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional(TxType.REQUIRED)
	public Response update(@PathParam("tenantId") String tenantId, MultipartFormDataInput form) {
		try {
			Builder builder = provideBuilder(form);
			tenantService.update(builder.build(), tenantId);
		} catch (Exception e) {
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
						.type(MediaType.TEXT_PLAIN)
						.entity(e)
						.build();
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
	public List<String> getTenants() {
		return tenantService.getTenantIds();
	}

	@GET
	@Path("models")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<TenantInitializationExternalModel> getModels() {
		return modelRetriever.getModels();
	}

	private Builder provideBuilder(MultipartFormDataInput form) throws IOException {
		try {
			// get the base json request
			Map<String, List<InputPart>> formDataMap = form.getFormDataMap();
			String rawData = readPart(formDataMap.remove(KEY_TENANTMODEL_FORMID), String.class);

			Builder builder = modelBuilder.getBuilder();
			builder.setModel(URLDecoder.decode(rawData, StandardCharsets.UTF_8.name()));

			// extract the external model ids.
			String models = readPart(formDataMap.remove("DMSInitialization_attachment_path"), String.class);

			if (StringUtils.isNotNullOrEmpty(models)) {
				String[] modelIds = models.split(",");
				for (String modelId : modelIds) {
					TenantInitializationExternalModel model = modelRetriever.getModel(modelId);
					appendModel(builder, "DMSInitialization_attachment_definitions", model.getDefinitionsPath());
					appendModel(builder, "SemanticDbInitialization_attachment_patches", model.getSemanticPath());
				}
			} else {
				// extract the multipart files
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

	private void appendModel(Builder builder,String key, String path) {
		if (StringUtils.isNotNullOrEmpty(path)) {
			builder.appendModelFile(key, FilenameUtils.getName(path),
					modelRetriever.getPathInputStream(path));
		}
	}

	/**
	 * Provides the blank model for new tenant
	 *
	 * @return the json response entity
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		TenantInitializationModel model = tenantService.provideModel();
		return Response.ok(model.toJSONObject().toString()).build();
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
}
