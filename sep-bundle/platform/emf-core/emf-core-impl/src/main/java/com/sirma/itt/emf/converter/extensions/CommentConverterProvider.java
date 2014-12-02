package com.sirma.itt.emf.converter.extensions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.CurrentUser;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.DefaultPrimaryStateTypeImpl;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * {@link TopicInstance} and {@link CommentInstance} <-> {@link JSONObject} converter.
 * 
 * @author yasko
 */
@ApplicationScoped
public class CommentConverterProvider implements TypeConverterProvider {
	private static final String INSTANCE_TYPE = "instanceType";
	private static final String INSTANCE_ID = "instanceId";
	private static final String CHILDREN = "children";
	private static final String ZOOM_LEVEL = "zoomLevel";
	private static final String VIEWBOX = "viewbox";
	private static final String SVG_TAG = "svgTag";
	private static final String SHAPE = "shape";

	private static final PrimaryStateType IN_PROGRESS = new DefaultPrimaryStateTypeImpl(
			PrimaryStateType.IN_PROGRESS);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private ResourceService resourceService;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private Instance<CommentService> commentService;

	@Inject
	@CurrentUser
	private User currentUser;

	@Inject
	private Instance<ContentSanitizer> contentSanitizer;

	private CommentService service;

	@Inject
	private StateService stateService;

	/**
	 * {@link JSONObject} -> {@link CommentInstance}
	 * 
	 * @author yasko
	 */
	public class JsonObjectToCommentInstanceConverter implements
			Converter<JSONObject, CommentInstance> {

		@Override
		public CommentInstance convert(JSONObject source) {
			CommentInstance instance = new CommentInstance();
			if (source.has(ForumProperties.ID)) {
				instance.setId(JsonUtil.getStringValue(source, ForumProperties.ID));
				instance.setFrom(JsonUtil.getStringValue(source, ForumProperties.CREATED_BY));
				instance.setPostedDate(typeConverter.convert(Date.class,
						JsonUtil.getStringValue(source, ForumProperties.CREATED_ON)));
			} else {
				instance.setFrom(currentUser.getIdentifier());
				instance.setPostedDate(new Date());
			}
			instance.getProperties().put(DefaultProperties.IS_DELETED, Boolean.FALSE);
			instance.setComment(sanitize(JsonUtil.getStringValue(source, ForumProperties.CONTENT)));
			String topicId = JsonUtil.getStringValue(source, ForumProperties.REPLY_TO);
			CommentInstance topic = getService().loadByDbId(topicId);
			instance.setTopic((TopicInstance) topic);

			return instance;
		}

	}

	/**
	 * {@link JSONObject} -> {@link TopicInstance}
	 * 
	 * @author yasko
	 */
	public class JsonObjectToTopicInstanceConverter implements Converter<JSONObject, TopicInstance> {

		@Override
		public TopicInstance convert(JSONObject source) {
			TopicInstance instance = null;
			String id = JsonUtil.getStringValue(source, ForumProperties.ID);
			if (StringUtils.isNotBlank(id)) {
				instance = (TopicInstance) getService().loadByDbId(id);
			} else {
				instance = new TopicInstance();
				instance.getProperties().put(DefaultProperties.IS_DELETED, Boolean.FALSE);
			}

			String sectionId = JsonUtil.getStringValue(source, ForumProperties.TOPIC_ABOUT_SECTION);
			if (StringUtils.isNotBlank(sectionId)) {
				instance.setSubSectionId(sectionId);
			}

			JSONObject about = JsonUtil.getJsonObject(source, ForumProperties.TOPIC_ABOUT);
			if (about != null) {
				String aboutId = JsonUtil.getStringValue(about, INSTANCE_ID);
				String aboutType = JsonUtil.getStringValue(about, INSTANCE_TYPE);
				InstanceReference reference = typeConverter.convert(InstanceReference.class,
						aboutType);
				reference.setIdentifier(aboutId);
				instance.setTopicAbout(reference);
			}

			String content = JsonUtil.getStringValue(source, ForumProperties.CONTENT);
			if (StringUtils.isNotBlank(content)) {
				instance.setComment(sanitize(content));
			}

			String tags = JsonUtil.getStringValue(source, ForumProperties.TAGS);
			instance.setTags(tags);

			String category = JsonUtil.getStringValue(source, ForumProperties.TYPE);
			if (StringUtils.isNotBlank(category)) {
				instance.getProperties().put(ForumProperties.TYPE, category);
			}

			String from = JsonUtil.getStringValue(source, ForumProperties.CREATED_BY);
			if (StringUtils.isBlank(instance.getFrom())) {
				if (!StringUtils.isBlank(from)) {
					Resource resource = resourceService.findResource(from);
					if ((resource != null) && (resource.getId() != null)) {
						instance.setFrom(resource.getId().toString());
					}
				}
				// if the user was not found
				if (StringUtils.isBlank(instance.getFrom())) {
					instance.setFrom(currentUser.getId().toString());
				}
			}

			// update the posted date if not set already
			if (instance.getPostedDate() == null) {
				instance.setPostedDate(new Date());
			}
			// get value from generated constant, definition default
			String status = JsonUtil.getStringValue(source, ForumProperties.STATUS);
			if (StringUtils.isNotBlank(status)) {
				instance.getProperties().put(ForumProperties.STATUS, status);
			} else {
				String state = stateService.getState(IN_PROGRESS, TopicInstance.class);
				instance.getProperties().put(ForumProperties.STATUS, state);
			}

			String title = JsonUtil.getStringValue(source, ForumProperties.TITLE);
			if (StringUtils.isNotBlank(title)) {
				instance.setTitle(title);
			}

			JSONObject shapeJson = JsonUtil.getJsonObject(source, SHAPE);
			if (shapeJson != null) {
				ImageAnnotation shape = instance.getImageAnnotation();
				if (shape == null) {
					shape = new ImageAnnotation();
					SequenceEntityGenerator.generateStringId(shape, false);
					instance.setImageAnnotation(shape);
				}
				shape.setSvgValue(JsonUtil.getStringValue(shapeJson, SVG_TAG));
				shape.setViewBox(JsonUtil.getStringValue(shapeJson, VIEWBOX));
				String zoomLevel = JsonUtil.getStringValue(shapeJson, ZOOM_LEVEL);
				if (StringUtils.isNotBlank(zoomLevel)) {
					shape.setZoomLevel(Integer.valueOf(zoomLevel).intValue());
				}
			}

			JSONArray children = JsonUtil.getJsonArray(source, CHILDREN);
			if ((children != null) && (children.length() > 0)) {
				// Should we iterate for more children on create?!
				JSONObject topicContentComment = (JSONObject) JsonUtil.getFromArray(children, 0);

				CommentInstance topicContentCommentInstance = typeConverter.convert(
						CommentInstance.class, topicContentComment);
				instance.getComments().add(topicContentCommentInstance);
			}
			instance.initBidirection();
			return instance;
		}

	}

	/**
	 * {@link CommentInstance} -> {@link JSONObject}
	 * 
	 * @author yasko
	 */
	public class CommentInstanceToJsonConverter implements Converter<CommentInstance, JSONObject> {

		@Override
		public JSONObject convert(CommentInstance source) {
			if (source instanceof TopicInstance) {
				return convertTopic((TopicInstance) source);
			}
			return convertComment(source);
		}

		/**
		 * {@link TopicInstance} -> {@link JSONObject}
		 * 
		 * @param instance
		 *            Instance to convert
		 * @return converted json object
		 */
		private JSONObject convertTopic(TopicInstance instance) {
			JSONObject topic = convertCommon(instance);

			Date modifiedOn = (Date) instance.getProperties().get(ForumProperties.MODIFIED_ON);
			if (modifiedOn != null) {
				JsonUtil.addToJson(topic, ForumProperties.MODIFIED_ON,
						typeConverter.convert(String.class, modifiedOn));
			}
			JsonUtil.addToJson(topic, DefaultProperties.STATUS,
					instance.getProperties().get(DefaultProperties.STATUS));
			JsonUtil.addToJson(topic, ForumProperties.TITLE,
					instance.getProperties().get(ForumProperties.TITLE));
			JsonUtil.addToJson(topic, ForumProperties.TYPE,
					instance.getProperties().get(ForumProperties.TYPE));
			JsonUtil.addToJson(topic, ForumProperties.TAGS, instance.getTags());
			JsonUtil.addToJson(topic, ForumProperties.TOPIC_ABOUT_SECTION,
					instance.getSubSectionId());

			JSONObject about = new JSONObject();
			JsonUtil.addToJson(about, INSTANCE_ID, instance.getTopicAbout().getIdentifier());
			JsonUtil.addToJson(about, INSTANCE_TYPE, instance.getTopicAbout().getReferenceType()
					.getName().toLowerCase());
			JsonUtil.addToJson(topic, ForumProperties.TOPIC_ABOUT, about);

			JSONArray children = new JSONArray();
			if (instance.getComments() != null) {
				for (CommentInstance child : instance.getComments()) {
					children.put(convertComment(child));
				}
			}
			// we have to put an entry no mater if we have or no children
			JsonUtil.addToJson(topic, CHILDREN, children);

			if (instance.getImageAnnotation() != null) {
				ImageAnnotation annotation = instance.getImageAnnotation();
				JSONObject shape = new JSONObject();

				JsonUtil.addToJson(shape, "imageId", annotation.getIdentifier());
				JsonUtil.addToJson(shape, "imageUri", annotation.getIdentifier());
				JsonUtil.addToJson(shape, ZOOM_LEVEL, annotation.getZoomLevel());
				JsonUtil.addToJson(shape, SVG_TAG, annotation.getSvgValue());
				JsonUtil.addToJson(shape, VIEWBOX, annotation.getViewBox());

				Collection<Action> actions = authorityService.getAllowedActions(
						currentUser.getIdentifier(), annotation, "");
				JsonUtil.addActions(shape, actions);

				JsonUtil.addToJson(topic, SHAPE, shape);
			}

			return topic;
		}

		/**
		 * Convert common.
		 * 
		 * @param instance
		 *            the instance
		 * @return the JSON object
		 */
		private JSONObject convertCommon(CommentInstance instance) {
			JSONObject object = new JSONObject();
			JsonUtil.addToJson(object, ForumProperties.ID, instance.getId());

			Resource resource = getCreatorResource(instance.getFrom());
			JsonUtil.addToJson(object, ForumProperties.CREATED_BY, resource.getId());
			JsonUtil.addToJson(object, ForumProperties.CREATED_BY_LABEL, resource.getDisplayName());

			JsonUtil.addToJson(object, ForumProperties.CREATED_ON,
					typeConverter.convert(String.class, instance.getPostedDate()));

			String comment = instance.getComment();
			if (StringUtils.isNotBlank(comment)) {
				JsonUtil.addToJson(object, ForumProperties.CONTENT, comment);
			}

			return object;
		}

		/**
		 * Comment converter
		 * 
		 * @param instance
		 *            {@link CommentInstance}
		 * @return {@link JSONObject} representation of instance
		 */
		private JSONObject convertComment(CommentInstance instance) {
			JSONObject object = convertCommon(instance);

			TopicInstance parent = instance.getTopic();
			if (parent != null) {
				JsonUtil.addToJson(object, ForumProperties.REPLY_TO, parent.getId());
			}

			return object;
		}

		/**
		 * Retrieves creator resource by id
		 * 
		 * @param creator
		 *            Creator id
		 * @return Creator {@link Resource}
		 */
		private Resource getCreatorResource(Serializable creator) {
			if (creator instanceof Resource) {
				return (Resource) creator;
			}
			return resourceService.findResource(creator);
		}
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(JSONObject.class, CommentInstance.class,
				new JsonObjectToCommentInstanceConverter());
		converter.addConverter(JSONObject.class, TopicInstance.class,
				new JsonObjectToTopicInstanceConverter());
		converter.addConverter(CommentInstance.class, JSONObject.class,
				new CommentInstanceToJsonConverter());
		converter.addConverter(CommentInstance.class, JSONObject.class,
				new CommentInstanceToJsonConverter());
	}

	/**
	 * Sanitize.
	 * 
	 * @param sanitize
	 *            the sanitize
	 * @return the string
	 */
	protected String sanitize(String sanitize) {
		if (contentSanitizer.isUnsatisfied()) {
			// no sanitizer present
			return sanitize;
		}
		return contentSanitizer.get().sanitize(sanitize);
	}

	/**
	 * Gets the service.
	 * 
	 * @return the service
	 */
	protected CommentService getService() {
		if (service == null) {
			service = commentService.get();
		}
		return service;
	}

}
