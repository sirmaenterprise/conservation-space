package com.sirma.itt.objects.web.object;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for adding primary images for objects.
 *
 * @author sdjulgerova
 */
@Secure
@Path("/primaryImage")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PrimaryImageRestService extends EmfRestService {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * Adds the primary image to selected object. Expected parameters are: <br>
	 * objectId - the object id to which an image should be linked <br>
	 * documentId - document id of the document with mimetype image/*
	 *
	 * @param data
	 *            Request data.
	 * @return the response
	 */
	@POST
	public Response addPrimaryImage(String data) {
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"Missing request parameters for add primary image operation!");
		}

		// first check for logged in user
		Resource currentUser = getCurrentUser();

		if (currentUser == null) {
			return buildResponse(Response.Status.UNAUTHORIZED,
					"No logged in user to add primary image!");
		}

		JSONObject jsonData = JsonUtil.createObjectFromString(data);

		String instanceId = JsonUtil.getStringValue(jsonData, "instanceId");
		String instanceType = JsonUtil.getStringValue(jsonData, "instanceType");
		String images = JsonUtil.getStringValue(jsonData, "images");

		if (StringUtils.isNullOrEmpty(instanceType) || StringUtils.isNullOrEmpty(instanceId)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"Missing required arguments: instanceId[" + instanceId + "] or instanceType["
							+ instanceType + "] ");
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		if (instance == null) {
			log.debug("Can't find target {} with id={}", instanceType, instanceId);
			return buildResponse(Response.Status.NOT_FOUND, "Can't find "
					+ instanceType + " with id=" + instanceId);
		}
		Map<String, Serializable> properties = CollectionUtils.createHashMap(2);
		properties.put(DefaultProperties.CREATED_BY, currentUser);
		properties.put(DefaultProperties.CREATED_ON, new Date());

		JSONArray imagesArray = JsonUtil.createArrayFromString(images);
		for (int i = 0; i < imagesArray.length(); i++) {
			try {

				String documentId = (String) imagesArray.get(i);

				if (StringUtils.isNullOrEmpty(documentId)) {
					return buildResponse(Response.Status.BAD_REQUEST,
							"Missing required arguments: instanceId[" + instanceId
									+ "] or instanceType[" + instanceType + "] or documentId["
									+ documentId + "]");
				}
				DocumentInstance documentInstance = loadInstanceInternal(DocumentInstance.class,
						documentId);

				if (documentInstance == null) {
					log.debug("Can't find document with id={}", documentId);
					return buildResponse(Response.Status.NOT_FOUND,
							"Can't find document with id=" + documentId);
				}
				// add primary image
				Pair<Serializable, Serializable> linkIds = linkService.link(instance.toReference(),
						documentInstance.toReference(), LinkConstants.HAS_PRIMARY_IMAGE,
						LinkConstants.IS_PRIMARY_IMAGE_OF, properties);

				if ((linkIds.getFirst() == null) || (linkIds.getSecond() == null)) {
					log.debug(
							"Can not create relation of type [{}] between object[{}] and document[{}]",
							LinkConstants.HAS_PRIMARY_IMAGE, instanceId, documentId);
					return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
							"Can't create link between selected object and image");

				}

			} catch (Exception e) {
				log.error("Can not convert given string to json object", e);
				return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
						"Can not convert given string to json object");
			}
		}

		return Response.ok().build();
	}
}
