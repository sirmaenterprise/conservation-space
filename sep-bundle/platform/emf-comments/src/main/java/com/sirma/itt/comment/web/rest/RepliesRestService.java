package com.sirma.itt.comment.web.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.sirma.itt.emf.forum.model.CommentInstance;

/**
 * Rest service responsible for working with topic replies
 * 
 * @author yasko
 */
@Stateless
@Path("/replies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepliesRestService extends CommonCommentRestService {

	/**
	 * Create a new reply
	 * @param jsonString Json string to be used as a reply
 	 * @return OK if create was successful
	 */
	@POST
	public Response create(String jsonString) {
		CommentInstance commentInstance = createCommentInstanceFromJsonString(jsonString);
		commentService.save(commentInstance, null);

		JSONObject json = convertCommentToJson(commentInstance);
		ResponseBuilder builder = Response.ok(json.toString());
		return builder.build();
	}

	/**
	 * Updates a reply
	 * @param id Id of the reply
	 * @param jsonString Json string representing the updated reply
	 * @return OK if update was successful
	 */
	@PUT
	@Path("/{id}")
	public Response updateReply(@PathParam("id") String id, String jsonString) {
		if (StringUtils.isBlank(id)) {
			return Response.status(Status.BAD_REQUEST)
					.entity("{ \"message\": \"Id is requered\" }").build();
		}
		CommentInstance updated = createCommentInstanceFromJsonString(jsonString);
		commentService.save(updated, null);

		JSONObject topicJson = convertCommentToJson(updated);
		ResponseBuilder builder = Response.ok(topicJson.toString());
		return builder.build();
	}

	/**
	 * Deletes a reply
	 * @param id Id of the reply to delete
	 * @return OK if delete was successful
	 */
	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") String id) {
		if (StringUtils.isBlank(id)) {
			return Response.status(Status.BAD_REQUEST)
					.entity("{ \"message\": \"Id is requered\" }").build();
		}

		// commentService.deleteById(id);
		return Response.ok().build();
	}
}
