package com.sirma.itt.seip.annotations.rest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jose4j.json.JsonUtil;

import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.mention.AnnotationMentionService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Rest service for accessing and modifying {@link Annotation} data.
 *
 * @author kirq4e
 * @author BBonev
 */
@Transactional
@Path("/annotations")
@ApplicationScoped
@Consumes(MediaType.WILDCARD)
@Produces({ MediaType.APPLICATION_JSON, AnnotationsRestService.APPLICATION_LD_JSON })
public class AnnotationsRestService {

	public static final String APPLICATION_LD_JSON = "application/ld+json";
	public static final String EMPTY_JSON = "{ }";

	@Inject
	private AnnotationService annotationService;

	@Inject
	private AnnotationMentionService annotationMentionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private SecurityContext securityContext;

	/**
	 * Checks for mentioned users and saves single annotation.
	 *
	 * @param annotationData
	 * 		The data of the annotation that will be persisted
	 * @return JSON representation of the annotation with generated ID
	 */
	@POST
	@Path("/create")
	public Annotation createAnnotation(Annotation annotationData) {
		if (annotationData.isSomeoneMentioned()) {
			sendNotifications(annotationData);
		}
		return annotationService.saveAnnotation(annotationData);
	}

	/**
	 * Saves one or more annotations.
	 *
	 * @param annotationData
	 * 		The data of the annotation that will be persisted
	 * @return a collection of updated annotation data.
	 */
	@POST
	public Collection<Annotation> createAnnotation(Collection<Annotation> annotationData) {
		return annotationService.saveAnnotation(annotationData);
	}

	/**
	 * Updates the given annotation with the new annotation data.
	 *
	 * @param annotationId
	 * 		Id of the annotation that will be updated
	 * @param annotationData
	 * 		The new annotation data
	 * @return JSON representation of the updated annotation
	 * @see #update(String, Annotation)
	 */
	@POST
	@Path("/update/{id}")
	public Annotation updateAnnotation(@PathParam(JsonKeys.ID) String annotationId, Annotation annotationData) {
		return update(annotationId, annotationData);
	}

	/**
	 * Checks for mentioned users and updates the given annotation with the new annotation data.
	 *
	 * @param annotationId
	 * 		Id of the annotation that will be updated
	 * @param annotationData
	 * 		The new annotation data
	 * @return JSON representation of the updated annotation
	 */
	@PUT
	@Path("/{id}")
	public Annotation update(@PathParam(JsonKeys.ID) String annotationId, Annotation annotationData) {
		if (annotationData.getId() == null) {
			annotationData.setId(org.apache.commons.lang3.StringUtils.trimToNull(annotationId));
		}
		if (annotationData.isSomeoneMentioned()) {
			sendNotifications(annotationData);
		}
		return annotationService.saveAnnotation(annotationData);
	}

	/**
	 * Deletes the annotation
	 *
	 * @param annotationId
	 * 		ID of the annotation that will be deleted
	 * @return Empty JSON if the operation is successful or an exception if it is not
	 * @see #delete(String)
	 */
	@DELETE
	@Path("/destroy")
	public Response deleteAnnotation(@QueryParam(JsonKeys.ID) String annotationId) {
		return delete(annotationId);
	}

	/**
	 * Deletes the annotation
	 *
	 * @param annotationId
	 * 		ID of the annotation that will be deleted
	 * @return Empty JSON if the operation is successful or an exception if it is not
	 */
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam(JsonKeys.ID) String annotationId) {
		annotationService.deleteAnnotation(annotationId);
		return RestUtil.buildOkResponse(EMPTY_JSON);
	}

	/**
	 * Deletes all annotations for the given target
	 *
	 * @param targetId
	 * 		id of the current instance
	 * @param tabId
	 * 		id of the current instance tab which we will search for annotations
	 * @return Empty JSON if the operation is successful or an exception if it is not
	 */
	@DELETE
	public Response deleteAllForTarget(@QueryParam(JsonKeys.ID) String targetId, @QueryParam("tabId") String tabId) {
		annotationService.deleteAllAnnotations(targetId, tabId);
		return RestUtil.buildOkResponse(EMPTY_JSON);
	}

	/**
	 * Searches for annotations that are created on the given instance tab.
	 *
	 * @param targetId
	 * 		id of the current instance for which we will search for annotations
	 * @param tabId
	 * 		id of the current instance tab which we will search for annotations
	 * @param limit
	 * 		Limit of the annotations
	 * @return JSON String with array of annotations that are created for the given Image
	 */
	@GET
	@Path("/search")
	public Collection<Annotation> performSearch(@QueryParam(JsonKeys.ID) String targetId,
			@QueryParam("tabId") String tabId, @DefaultValue("0") @QueryParam("limit") Integer limit) {
		return annotationService.searchAnnotation(targetId, tabId, limit);
	}

	/**
	 * Load annotation replies. The result will contain the top level annotation and it's replies
	 *
	 * @param annotationId
	 * 		the if of the annotation to load it's replies
	 * @return the annotation and it's replies
	 */
	@GET
	@Path("/{id}")
	public Annotation loadReplies(@PathParam(JsonKeys.ID) String annotationId) {
		return annotationService.loadAnnotation(annotationId).orElseGet(Annotation::new);
	}

	/**
	 * Load all annotations and their replies.
	 *
	 * @param targetId
	 *            the id of the image
	 * @param limit
	 *            the limit of the annotations
	 * @return the annotations
	 */
	@GET
	@Path("/search/all")
	public Collection<Annotation> loadAllAnnotations(@QueryParam(JsonKeys.ID) String targetId,
			@DefaultValue("0") @QueryParam("limit") Integer limit) {
		return annotationService.loadAnnotations(targetId, limit);
	}
	
	/**
	 * Counts the annotations for the given target URI
	 *
	 * @param targetId
	 * 		id of the object for which we will search for annotations
	 * @param tabId
	 * 		id of the current instance tab which we will search for annotations
	 * @return JSON String containing the annotation count
	 */
	@GET
	@Path("/count")
	public String count(@QueryParam(JsonKeys.ID) String targetId, @QueryParam("tabId") String tabId) {
		int count = annotationService.countAnnotations(targetId, tabId);
		return Json.createObjectBuilder().add("count", count).build().toString();
	}

	/**
	 * Counts the replies of all annotations for the given target URI. The returned json object consists of annotation
	 * ids as keys and reply count as values.
	 *
	 * @param targetId
	 * 		id of the object for which we will search for annotations
	 * @return JSON String containing the annotation count
	 */
	@GET
	@Path("/replyCount")
	public String countReplies(@QueryParam(JsonKeys.ID) String targetId) {
		Map<String, Integer> replies = annotationService.countAnnotationReplies(targetId);
		return JsonUtil.toJson(replies);
	}

	/**
	 * Invokes annotation mention service.
	 *
	 * @param annotation
	 * 		the annotation for which to send notification.
	 */
	private void sendNotifications(Annotation annotation) {
		Collection<Serializable> mentionedUsers = getShortUris(annotation.getMentionedUsers());

		if (!annotation.isNew()) {
			mentionedUsers.removeAll(annotationMentionService.loadMentionedUsers(annotation.getId().toString()));
		}

		if (!mentionedUsers.isEmpty()) {
			annotationMentionService.sendNotifications(mentionedUsers, annotation.getTargetId().toString(),
													   annotation.getCommentsOn(),
													   securityContext.getAuthenticated().getSystemId().toString());
		}
	}

	/**
	 * Long Uris shortener.
	 *
	 * @param longUris
	 * 		long Uris
	 * @return short Uris collection
	 */
	private Collection<Serializable> getShortUris(Collection<Serializable> longUris) {
		return longUris.stream()
				.map(uri -> namespaceRegistryService.getShortUri(uri.toString()))
				.collect(Collectors.toSet());
	}

}
