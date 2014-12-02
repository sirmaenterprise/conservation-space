package com.sirma.itt.comment.web.rest;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Topics rest service.
 *
 * @author yasko
 */
@Stateless
@Path("/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicsRestService extends CommonCommentRestService {

	private static final Pattern SPLIT = Pattern.compile(",|;");

	@Inject
	private ResourceService resourceService;

	@Inject
	private ExpressionsManager expressionsManager;

	/**
	 * Creates a new topic.
	 *
	 * @param topicJsonString
	 *            json string to used as a topic
	 * @return OK if create was successful
	 */
	@POST
	public Response create(String topicJsonString) {
		TopicInstance topicInstance = createTopicInstanceFromJsonString(topicJsonString);
		if (topicInstance == null) {
			return buildBadRequest("Invalid request data");
		}
		if (StringUtils.isBlank(topicInstance.getComment())
				&& StringUtils.isBlank(topicInstance.getTitle())) {
			return buildBadRequest("Cannot create topic without at least a title and/or content");
		}
		// QVISRV-415: temporal fix: if trying to save a comment for invalid section - nothing
		// happens
		if (topicInstance.getSubSectionId() == null && topicInstance.getTopicAbout() == null) {
			// we have invalid topic instance if both are empty
			log.error("Trying to create a topic for unknown instance or section. Nothing will be created!");
			return buildResponse(Status.OK, "{}");
		}
		commentService.save(topicInstance, null);

		JSONObject topicJson = convertTopicToJson(topicInstance);
		return buildResponse(Status.OK, topicJson.toString());
	}

	/**
	 * Post comment.
	 *
	 * @param topicId
	 *            the topic id
	 * @param lastKnownDate
	 *            the last known date
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("/{id}/replies")
	public Response postComment(@PathParam("id") String topicId,
			@QueryParam("date") String lastKnownDate, String data) {

		if (StringUtils.isBlank(topicId)) {
			return buildBadRequest("Topic ID is required");
		}

		CommentInstance commentInstance = createCommentInstanceFromJsonString(data);
		if (commentInstance == null) {
			return buildBadRequest("Invalid comment data");
		}
		if (StringUtils.isBlank(commentInstance.getComment())) {
			return buildBadRequest("Cannot post empty comment");
		}

		Date lastCommentDate = null;
		try {
			lastCommentDate = typeConverter.convert(Date.class, lastKnownDate);
		} catch (TypeConversionException e) {
			return buildBadRequest("Invalid date format " + lastKnownDate
					+ ". Expected full ISO8601 date format!");
		}
		List<CommentInstance> comments = commentService.postComment(topicId, lastCommentDate,
				commentInstance);
		if (comments.isEmpty()) {
			return buildErrorResponse(Status.INTERNAL_SERVER_ERROR,
					"Failed to post comment for topic");
		}

		JSONObject response = new JSONObject();
		// evaluate comment actions before return
		for (CommentInstance instance : comments) {
			JSONObject json = convertCommentToJson(instance);
			Set<Action> actions = authorityService.getAllowedActions(instance, "");
			JsonUtil.addActions(json, actions);
			JsonUtil.append(response, "comments", json);
		}

		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * Load comments.
	 *
	 * @param topicId
	 *            the topic id
	 * @param lastKnownDate
	 *            the last known date
	 * @param limit
	 *            the limit
	 * @return the response
	 */
	@GET
	@Path("/{id}/replies")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response loadComments(@PathParam("id") String topicId,
			@QueryParam("date") String lastKnownDate, @QueryParam("limit") Integer limit) {

		if (StringUtils.isBlank(topicId)) {
			return buildBadRequest("Topic ID is required parameter");
		}

		Date lastCommentDate = typeConverter.convert(Date.class, lastKnownDate);
		int max = limit == null ? -1 : limit.intValue(); // TODO: probably a default page size
		List<CommentInstance> comments = commentService.loadComments(topicId, lastCommentDate, max);

		JSONObject response = new JSONObject();
		for (CommentInstance instance : comments) {
			JSONObject json = convertCommentToJson(instance);
			Set<Action> actions = authorityService.getAllowedActions(instance, "");
			JsonUtil.addActions(json, actions);
			JsonUtil.append(response, "comments", json);
		}

		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * Retrieves a topic by id.
	 *
	 * @param id
	 *            Id of the topic to retrieve
	 * @return The found topic
	 */
	@GET
	@Path("/{id}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response find(@PathParam("id") String id) {
		TopicInstance load = (TopicInstance) commentService.loadByDbId(id);
		JSONObject json = convertTopicToJson(load);
		return buildResponse(Status.OK, json.toString());
	}

	/**
	 * Retrieves all topics for the specified identifiers.
	 * 
	 * @param sectionId
	 *            Id of linked section
	 * @param linkedToId
	 *            Id of linked object
	 * @param linkedToType
	 *            Type of linked object
	 * @param lastKnownDate
	 *            the last known date
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the date to
	 * @param limit
	 *            the limit
	 * @param includeSubInstances
	 *            the include sub instances
	 * @param user
	 *            the user
	 * @param sortByParam
	 *            the sort by parameter
	 * @param sortDirectionParam
	 *            the sort direction parameter
	 * @param userFilter
	 *            the user filter
	 * @param category
	 *            the category
	 * @param tags
	 *            the tags
	 * @param status
	 *            the status
	 * @param level
	 *            the level
	 * @param keyword
	 *            the keyword
	 * @param type
	 *            the type
	 * @return All matching topics
	 */
	@GET
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response findAll(@QueryParam("sectionId") String sectionId,
			@QueryParam("linkedToId") String linkedToId,
			@QueryParam("linkedToType") String linkedToType,
			@QueryParam("date") String lastKnownDate, @QueryParam("dateFrom") String dateFrom,
			@QueryParam("dateTo") String dateTo,
			@DefaultValue("25") @QueryParam("limit") Integer limit,
			@DefaultValue("false") @QueryParam("includeChildren") Boolean includeSubInstances,
			@QueryParam("user") String user,
			@DefaultValue(DefaultProperties.CREATED_ON) @QueryParam("sortBy") String sortByParam,
			@DefaultValue(Sorter.SORT_DESCENDING) @QueryParam("sortDirection") String sortDirectionParam,
			@QueryParam("userFilter") String userFilter, @QueryParam("category") String category,
			@QueryParam("tags") String tags, @QueryParam("status") String status,
			@QueryParam("level") String level, @QueryParam("keyword") String keyword,
			@QueryParam("type") String type) {

		JSONObject result = new JSONObject();

		Sorter sorter = new Sorter(sortByParam, sortDirectionParam);

		Date date = null;
		if (StringUtils.isNotBlank(lastKnownDate)) {
			date = typeConverter.convert(Date.class, lastKnownDate);
		}

		int max = limit.intValue();
		List<TopicInstance> topics = Collections.emptyList();
		Map<String, List<TopicInstance>> tempTopics = null;
		Map<String, Serializable> filterMap = createFilterMap(dateFrom, dateTo, userFilter, tags,
				category, status, level, keyword, type);
		if (StringUtils.isNotBlank(sectionId)) {
			if (sectionId.length() > 60) {
				String[] strings = SPLIT.split(sectionId);
				tempTopics = CollectionUtils.createLinkedHashMap(strings.length);
				for (String string : strings) {
					List<TopicInstance> list = commentService.getTopics(sectionId, date, max,
							false, sorter, filterMap);
					tempTopics.put(string, list);
				}
			} else {
				tempTopics = CollectionUtils.createHashMap(1);
				List<TopicInstance> list = commentService.getTopics(sectionId, date, max, false,
						sorter, filterMap);
				tempTopics.put(sectionId, list);
			}
		} else if (StringUtils.isNotBlank(linkedToId)) {
			if (Boolean.TRUE.equals(includeSubInstances)) {
				topics = commentService.getInstanceSuccessorsTopics(linkedToId, date, max, sorter,
						filterMap);
			} else if (StringUtils.isNotBlank(linkedToType)) {
				tempTopics = CollectionUtils.createHashMap(1);
				InstanceReference reference = getInstanceReferense(linkedToId, linkedToType);
				List<TopicInstance> list = commentService.getTopics(reference, date, max, false,
						sorter, filterMap);
				tempTopics.put(linkedToId, list);
			} else {
				// QVISRV-415: temporal fix:
				// for invalid sections does not display error message but return empty result
				log.error("Invalid arguments configuration");
				return buildResponse(Status.OK, "{}");
			}
		} else if (StringUtils.isNotBlank(user)) {
			User userLocal = loadUser(user);
			topics = commentService.getTopicsByUser(userLocal, date, max, sorter, filterMap);
		} else {
			// QVISRV-415: temporal fix:
			// for invalid sections does not display error message but return empty result
			log.error("Invalid arguments configuration");
			return buildResponse(Status.OK, "{}");
		}

		if (tempTopics != null) {
			for (Entry<String, List<TopicInstance>> entry : tempTopics.entrySet()) {
				for (TopicInstance topicInstance : entry.getValue()) {
					JSONObject topicJson = typeConverter.convert(JSONObject.class, topicInstance);
					JsonUtil.append(result, entry.getKey(), topicJson);
				}
			}
		} else {
			for (TopicInstance topicInstance : topics) {
				JSONObject topicJson = typeConverter.convert(JSONObject.class, topicInstance);
				JsonUtil.append(result, "result", topicJson);
			}
		}

		return buildResponse(Status.OK, result.toString());
	}

	/**
	 * Creates the filter mapping from the provided arguments.
	 * 
	 * @param dateFrom
	 *            the date from
	 * @param dateTo
	 *            the end date
	 * @param userFilter
	 *            the created user filter
	 * @param tags
	 *            the tags tags filter
	 * @param category
	 *            the category filter
	 * @param status
	 *            the status the comment status
	 * @param level
	 *            the image annotation zoom level filter
	 * @param keyword
	 *            the keyword filter search
	 * @param type
	 *            the target object type to filter
	 * @return the map
	 */
	private Map<String, Serializable> createFilterMap(String dateFrom, String dateTo,
			String userFilter, String tags, String category, String status, String level,
			String keyword, String type) {
		Map<String, Serializable> filters = CollectionUtils.createHashMap(9);
		if (StringUtils.isNotBlank(dateFrom)) {
			filters.put("fromDate", typeConverter.convert(Date.class, dateFrom));
		}
		if (StringUtils.isNotBlank(dateTo)) {
			filters.put("toDate", typeConverter.convert(Date.class, dateTo));
		}
		if (!StringUtils.isBlank(userFilter)) {
			filters.put("createdBy", userFilter);
		}
		if (!StringUtils.isBlank(tags)) {
			filters.put("tagFilter", tags);
		}
		if (!StringUtils.isBlank(category)) {
			filters.put("categoryFilter", category);
		}
		if (!StringUtils.isBlank(status)) {
			filters.put("status", status);
		}
		if (!StringUtils.isBlank(level)) {
			filters.put("zoomLevel", level);
		}
		if (!StringUtils.isBlank(type)) {
			DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(type);
			if (typeDefinition != null) {
				filters.put("objectTypeParameter", typeDefinition.getFirstUri());
			}
		}
		if (!StringUtils.isBlank(keyword)) {
			// keyword filtering is disabled for now
			// filters.put("keyword", keyword);
		}
		return filters;
	}

	/**
	 * Gets the actions.
	 *
	 * @param topicId
	 *            the topic id
	 * @return the actions
	 */
	@GET
	@Path("/{id}/actions")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response getActions(@PathParam("id") String topicId) {
		CommentInstance topic = commentService.loadByDbId(topicId);
		if (topic == null) {
			return buildBadRequest("Invalid topic " + topicId);
		}
		Set<Action> actions = authorityService.getAllowedActions(topic, "");

		JSONObject object = new JSONObject();
		JsonUtil.addActions(object, actions);
		return buildResponse(Status.OK, object.toString());
	}

	/**
	 * Count topics for instance.
	 * 
	 * @param id
	 *            the instance id
	 * @param type
	 *            the instance type (optional)
	 * @param userId
	 *            the user id
	 * @return the response will contain the arguments plus the count of the topics found
	 */
	@GET
	@Path("count")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response countTopics(@QueryParam("id") String id, @QueryParam("type") String type,
			@QueryParam("user") String userId) {
		if (StringUtils.isBlank(id) && StringUtils.isBlank(type) && StringUtils.isBlank(userId)) {
			return buildBadRequest("Invalid service arguments");
		}

		String expression = null;
		if (!StringUtils.isBlank(id)) {
			expression = "${query(commentQueries/topicsForInstance objectUri=" + id + ").count}";
		} else if (!StringUtils.isBlank(userId)) {
			expression = "${query(commentQueries/topicsForUser userUri=" + userId + ").count}";
		}
		if (expression == null) {
			return buildBadRequest("Invalid service arguments");
		}
		Integer count = expressionsManager.evaluate(expression, Integer.class);
		if (count == null) {
			count = Integer.valueOf(0);
		}
		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, "id", id);
		JsonUtil.addToJson(result, "type", type);
		JsonUtil.addToJson(result, "count", count);
		return buildResponse(Status.OK, result.toString());
	}

	/**
	 * Load user.
	 *
	 * @param user
	 *            the user
	 * @return the user
	 */
	private User loadUser(String user) {
		Resource resource = resourceService.findResource(user);
		return (User) resource;
	}

	/**
	 * Updates a topic.
	 *
	 * @param id
	 *            Id of the topic to update
	 * @param topicJsonString
	 *            Updated topic as json string
	 * @return OK if update was successful
	 */
	@PUT
	@Path("/{id}")
	public Response updateTopic(@PathParam("id") String id, String topicJsonString) {
		if (StringUtils.isBlank(id)) {
			return buildBadRequest("Id is required parameter");
		}
		TopicInstance updated = createTopicInstanceFromJsonString(topicJsonString);
		if (updated == null) {
			return buildBadRequest("Invalid topic data");
		}
		commentService.save(updated, null);

		JSONObject topicJson = convertTopicToJson(updated);
		return buildResponse(Status.OK, topicJson.toString());
	}

	/**
	 * Updates a topic.
	 * 
	 * @param id
	 *            Id of the topic to update
	 * @param commentId
	 *            the comment id
	 * @param commentJsonString
	 *            Updated topic as json string
	 * @return OK if update was successful
	 */
	@PUT
	@Path("/{id}/replies/{cid}")
	public Response updateReply(@PathParam("id") String id, @PathParam("cid") String commentId,
			String commentJsonString) {
		if (StringUtils.isBlank(id) || StringUtils.isBlank("commentId")) {
			return buildBadRequest("Id is required parameter");
		}

		CommentInstance updated = createCommentInstanceFromJsonString(commentJsonString);
		if (updated == null) {
			return buildBadRequest("Invalid comment data");
		}
		commentService.save(updated, null);

		JSONObject topicJson = convertCommentToJson(updated);
		return buildResponse(Status.OK, topicJson.toString());
	}

	/**
	 * Builds the error response.
	 * 
	 * @param message
	 *            the message
	 * @return the response
	 */
	private Response buildBadRequest(String message) {
		return buildErrorResponse(Status.BAD_REQUEST, message);
	}

	/**
	 * Builds the error response.
	 *
	 * @param status
	 *            the status
	 * @param message
	 *            the message
	 * @return the response
	 */
	private Response buildErrorResponse(Status status, String message) {
		return buildResponse(status, "{ \"message\": \"" + message + "\" }");
	}

	/**
	 * Deletes a topic by id.
	 *
	 * @param id
	 *            Id of the topic to delete
	 * @return OK if delete was successful
	 */
	@DELETE
	@Path("/{id}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response delete(@PathParam("id") String id) {
		if (StringUtils.isBlank(id)) {
			return buildBadRequest("Id is required parameter");
		}
		TopicInstance topicInstance = new TopicInstance();
		topicInstance.setId(id);

		commentService.delete(topicInstance, null, false);
		return Response.ok().build();
	}

	/**
	 * Delete comment.
	 *
	 * @param topicId
	 *            the topic id
	 * @param commentId
	 *            the comment id
	 * @return the response
	 */
	@DELETE
	@Path("/{id}/replies/{cid}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response deleteComment(@PathParam("id") String topicId,
			@PathParam("cid") String commentId) {
		if (StringUtils.isBlank(topicId)) {
			return buildBadRequest("Invalid topic id");
		}
		if (StringUtils.isBlank(commentId)) {
			return buildBadRequest("Invalid comment id");
		}
		TopicInstance topicInstance = new TopicInstance();
		topicInstance.setId(topicId);
		CommentInstance commentInstance = new CommentInstance();
		commentInstance.setId(commentId);
		commentInstance.setTopic(topicInstance);
		commentService.delete(commentInstance, null, false);
		return buildResponse(Status.OK, "{}");
	}

}
