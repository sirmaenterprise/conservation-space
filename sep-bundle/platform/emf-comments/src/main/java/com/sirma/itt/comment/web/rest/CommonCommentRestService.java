package com.sirma.itt.comment.web.rest;

import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Common loginc for the topic and reply rest services
 * @author yasko
 *
 */
public class CommonCommentRestService extends EmfRestService {

	@Inject
	protected CommentService commentService;
	@Inject
	protected AuthorityService authorityService;

	/**
	 * Converts topic to json object
	 * @param topicInstance Topic to convert
	 * @return json object
	 */
	protected JSONObject convertTopicToJson(TopicInstance topicInstance) {
		JSONObject json = typeConverter.convert(JSONObject.class, topicInstance);
		return json;
	}

	/**
	 * Converts comment to json object
	 * @param commentInstance {@link CommentInstance} to convert
	 * @return json object
	 */
	protected JSONObject convertCommentToJson(CommentInstance commentInstance) {
		JSONObject json = typeConverter.convert(JSONObject.class, commentInstance);
		return json;
	}

	/**
	 * Creates a {@link TopicInstance} from json string
	 * @param json Topic json string
	 * @return {@link TopicInstance}
	 */
	protected TopicInstance createTopicInstanceFromJsonString(String json) {
		JSONObject topicJson = JsonUtil.createObjectFromString(json);
		TopicInstance topicInstance = convertJsonToTopic(topicJson);
		return topicInstance;
	}

	/**
	 * Creates a {@link CommentInstance} from json string
	 * @param jsonString Comment json string
	 * @return {@link CommentInstance}
	 */
	protected CommentInstance createCommentInstanceFromJsonString(String jsonString) {
		JSONObject json = JsonUtil.createObjectFromString(jsonString);
		CommentInstance commentInstance = convertJsonToComment(json);
		return commentInstance;
	}

	/**
	 * {@link JSONObject} -> {@link TopicInstance}
	 * @param json Json object to convert
	 * @return {@link TopicInstance}
	 */
	protected TopicInstance convertJsonToTopic(JSONObject json) {
		TopicInstance instance = typeConverter.convert(TopicInstance.class, json);
		return instance;
	}

	/**
	 * {@link JSONObject} -> {@link CommentInstance}
	 * @param json Json object to convert
	 * @return {@link CommentInstance}
	 */
	protected CommentInstance convertJsonToComment(JSONObject json) {
		CommentInstance instance = typeConverter.convert(CommentInstance.class, json);
		return instance;
	}
}
