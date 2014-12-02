package com.sirma.itt.objects.web.object;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.rendition.ThumbnailService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for manipulating and managing thumbnail images for objects.
 * 
 * @author svelikov
 */
@Secure
@Path("/thumbnail")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ThumbnailRestService extends EmfRestService {

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The rendition service. */
	@Inject
	private RenditionService renditionService;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The synchronization service. */
	@Inject
	private ThumbnailService synchronizationService;

	/**
	 * Adds the thumbnail to selected object. Expected parameters are: <br>
	 * objectId - the object id to which an image should be linked as primary <br>
	 * documentId - document id of the document with mimetype image/*
	 * 
	 * @param data
	 *            Request data.
	 * @return the response
	 */
	@POST
	@Path("/")
	public Response addThumbnail(String data) {
		if (debug) {
			log.debug("ObjectsWeb: ThumbnailRestService.addThumbnail: data: " + data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"Missing request parameters for add thumbnail operation!");
		}

		// first check for logged in user
		Resource currentUser = getCurrentUser();
		if (currentUser == null) {
			return buildResponse(Response.Status.UNAUTHORIZED,
					"No logged in user to add thumbnail!");
		}

		JSONObject jsonData = JsonUtil.createObjectFromString(data);

		String instanceId = JsonUtil.getStringValue(jsonData, "instanceId");
		String instanceType = JsonUtil.getStringValue(jsonData, "instanceType");
		String documentId = JsonUtil.getStringValue(jsonData, "documentId");

		if (StringUtils.isNullOrEmpty(instanceType) || StringUtils.isNullOrEmpty(instanceId)
				|| StringUtils.isNullOrEmpty(documentId)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"Missing required arguments: instanceId[" + instanceId + "] or instanceType["
							+ instanceType + "] or documentId[" + documentId + "]");
		}

		Instance instance = fetchInstance(instanceId, instanceType);
		
		if (instance == null) {
			log.debug("Can't find target {} with id={}", instanceType, instanceId);
			return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Can't find "
					+ instanceType + " with id=" + instanceId);
		}

		DocumentInstance documentInstance = loadInstanceInternal(DocumentInstance.class, documentId);
		if (documentInstance == null) {
			log.debug("Can't find document with id={}", documentId);
			return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
					"Can't find document with id=" + documentId);
		}

		// If there is existing thumbnail image,
		// then deactivate the old relation and create a new one
		LinkReference hasThumbnailRelation = linkExists(instance.toReference(),
				LinkConstants.HAS_THUMBNAIL);
		System.out.println(hasThumbnailRelation);
		if (hasThumbnailRelation != null) {
			log.debug("Removing existing primary image link");
			linkService.unlink(instance.toReference(), hasThumbnailRelation.getTo(),
					LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF);
		}

		// changed the thumbnail image link to system: requested with CMF-6449
		Pair<Serializable, Serializable> linkIds = linkService.link(instance.toReference(),
				documentInstance.toReference(), LinkConstants.HAS_THUMBNAIL,
				LinkConstants.IS_THUMBNAIL_OF, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);

		if ((linkIds.getFirst() != null) && (linkIds.getSecond() != null)) {

			// register that the given instance now have a thumbnail represented by the given
			// document
			synchronizationService.register(instance, documentInstance);

			log.debug("Created relation of type [{}] between object[{}] and document[{}]",
					LinkConstants.HAS_THUMBNAIL, instanceId, documentId);
			return Response.ok().build();
		}

		return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
				"Can't create link between selected object and image");
	}

	/**
	 * Gets the thumbnail.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param checkOnly
	 *            the check only
	 * @param purpose
	 *            the purpose
	 * @return the thumbnail
	 */
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getThumbnail(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType,
			@QueryParam("checkOnly") @DefaultValue("false") Boolean checkOnly,
			@QueryParam("purpose") @DefaultValue(RenditionService.DEFAULT_PURPOSE) String purpose) {

		if (StringUtils.isNullOrEmpty(instanceId)) {
			String msg = "Missing required argument: instanceId[" + instanceId + "]";
			log.debug(msg);
			return buildResponse(Response.Status.BAD_REQUEST, msg);
		}

		String thumbnail = renditionService.getThumbnail(instanceId, purpose);
		if (Boolean.TRUE.equals(checkOnly)) {
			return buildResponse(Response.Status.OK,
					String.valueOf(StringUtils.isNotNullOrEmpty(thumbnail)));
		}

		return buildResponse(Response.Status.OK, thumbnail);
	}

	/**
	 * Check if a relation exists of given type for provided object. If there is one, the first one
	 * is returned or null otherwise.
	 * 
	 * @param instance
	 *            the instance
	 * @param linkType
	 *            the link type
	 * @return the link reference
	 */
	protected LinkReference linkExists(InstanceReference instance, String linkType) {
		List<LinkReference> linkReferences = linkService.getLinks(instance, linkType);
		if (!linkReferences.isEmpty()) {
			return linkReferences.get(0);
		}
		return null;
	}

}
