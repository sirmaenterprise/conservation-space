package com.sirma.sep.model.management.rest;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.soap.MTOM;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.serialization.xstream.XStreamConvertableWrapper;

/**
 * Web service for remote administration of the application.
 *
 * @author BBonev
 */
@ApplicationScoped
@Path("/administration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@MTOM
@AdminResource
public class DefinitionsAdministrationWS {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String MESSAGE = "message";
	private static final String EXIST = "exist";

	@Inject
	private EventService eventService;

	@Inject
	private TypeConverter converter;

	@Inject
	private LabelService labelService;

	@Inject
	private DefinitionService definitionService;

	/**
	 * Fires an event that causes all semantic definitions to be reloaded.
	 */
	@GET
	@Path("reloadSemanticDefinitions")
	public void reloadSemanticDefinitions() {
		LOGGER.info("Called reloadSemanticDefinitions from WS port");
		eventService.fire(new LoadSemanticDefinitions());
	}

	/**
	 * Clear labels cache.
	 */
	@GET
	@Path("clear-label-cache")
	public void clearLabelCache() {
		labelService.clearCache();
	}

	/**
	 * Retrieves definition by id in xml format.
	 *
	 * @param id
	 *            of the definition
	 * @return xml format of definition
	 */
	@GET
	@Path("definition")
	@Produces({ "application/xml" })
	public String getDefinition(@QueryParam("id") String id) {
		DefinitionModel model = null;
		if (StringUtils.isBlank(id)) {
			return definitionService
					.getAllDefinitions()
						.map(def -> "\n\t<definition revision=\"" + def.getRevision() + "\" >" + def.getIdentifier()
								+ "</definition>")
						.collect(Collectors.joining("", "<definitions>", "</definitions>"));
		}

		model = definitionService.find(id);
		if (model == null) {
			return "<error>Definition \"" + id + "\" not found</error>";
		}
		return converter.convert(String.class, new XStreamConvertableWrapper(model));
	}

	/**
	 * Check definitions.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("definition")
	public Response checkDefinitions(String data) {
		JSONObject object = JsonUtil.createObjectFromString(data);
		JSONArray array = JsonUtil.getJsonArray(object, "data");
		for (int i = 0; i < array.length(); i++) {
			try {
				Object element = array.get(i);
				if (element instanceof JSONObject) {
					JSONObject jsonObject = (JSONObject) element;
					String id = JsonUtil.getStringValue(jsonObject, "id");
					if (id == null) {
						JsonUtil.addToJson(jsonObject, MESSAGE, "Missing definition id");
						JsonUtil.addToJson(jsonObject, EXIST, Boolean.FALSE);
						continue;
					}
					DefinitionModel definition = definitionService.find(id);
					JsonUtil.addToJson(jsonObject, EXIST, definition != null);
				}
			} catch (JSONException e) {
				return Response.serverError().entity(e).build();
			}
		}

		return Response.ok(object.toString()).build();
	}
}