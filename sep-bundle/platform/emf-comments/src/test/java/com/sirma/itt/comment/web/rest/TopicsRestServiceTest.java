package com.sirma.itt.comment.web.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.extensions.CommentConverterProvider;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.ForumProperties;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.DefaultPrimaryStateTypeImpl;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.InstanceProxyMock;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * The Class TopicsRestServiceTest.
 *
 * @author BBonev
 */
@Test
public class TopicsRestServiceTest extends EmfTest {

	protected static final Object Serializable = null;

	/** The comment service. */
	private CommentService commentService;

	/** The type converter. */
	private TypeConverter typeConverter;

	/** The authority service. */
	private AuthorityService authorityService;

	/** The resource service. */
	private ResourceService resourceService;

	/** The service. */
	private TopicsRestService service;

	/** The current user. */
	private User currentUser;

	private StateService stateService;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		service = new TopicsRestService();

		commentService = mock(CommentService.class);
		authorityService = mock(AuthorityService.class);
		resourceService = mock(ResourceService.class);
		stateService = mock(StateService.class);

		when(resourceService.findResource(anyString())).then(new Answer<Resource>() {

			@Override
			public Resource answer(InvocationOnMock invocation) throws Throwable {
				EmfUser user = new EmfUser();
				String id = (String) invocation.getArguments()[0];
				user.setIdentifier(id.substring(id.indexOf(":") + 1));
				user.setId((Serializable) invocation.getArguments()[0]);
				user.getProperties().put(ResourceProperties.LAST_NAME, "last name");
				user.getProperties().put(ResourceProperties.FIRST_NAME,
						(Serializable) invocation.getArguments()[0]);
				return user;
			}
		});
		currentUser = new EmfUser("admin");
		currentUser.setId("emf:admin");

		typeConverter = createTypeConverter();

		ReflectionUtils.setField(service, "authorityService", authorityService);
		ReflectionUtils.setField(service, "commentService", commentService);
		ReflectionUtils.setField(service, "resourceService", resourceService);
		ReflectionUtils.setField(service, "typeConverter", typeConverter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter converter = super.createTypeConverter();

		ContentSanitizer sanitizer = Mockito.mock(ContentSanitizer.class);
		CommentConverterProvider converterProvider = new CommentConverterProvider();
		ReflectionUtils.setField(converterProvider, "authorityService", authorityService);
		ReflectionUtils.setField(converterProvider, "commentService",
				new InstanceProxyMock<CommentService>(commentService));
		ReflectionUtils.setField(converterProvider, "contentSanitizer",
				new InstanceProxyMock<ContentSanitizer>((ContentSanitizer) null));
		ReflectionUtils.setField(converterProvider, "resourceService", resourceService);
		ReflectionUtils.setField(converterProvider, "typeConverter", converter);
		ReflectionUtils.setField(converterProvider, "currentUser", currentUser);
		ReflectionUtils.setField(converterProvider, "stateService", stateService);
		converterProvider.register(converter);

		when(
				stateService.getState(
						new DefaultPrimaryStateTypeImpl(PrimaryStateType.IN_PROGRESS),
						TopicInstance.class)).thenReturn(PrimaryStateType.IN_PROGRESS);

		converter.addConverter(String.class, InstanceReference.class,
				new Converter<String, InstanceReference>() {

					@Override
					public InstanceReference convert(String source) {
						DataType type = new DataType();
						if (source.contains(".")) {
							type.setJavaClassName(source);
							type.setName(type.getJavaClass().getSimpleName().toLowerCase());
						} else {
							type.setName(source);
						}
						return new LinkSourceId(null, type);
					}
				});
		return converter;
	}

	/** The create topic. */
	String createTopic = "{" + " aboutSection : \"aboutSection\","
			+ " about : { instanceId : \"aboutId\", instanceType : \"aboutType\" },"
			+ " content : \"some content\"," + " tags : \"tags\"," + " type : \"type\","
			+ " createdBy : \"emf\\:user\"," + " createdOn : null," + " status : null,"
			+ " title : \"some title\"" + "}";

	/**
	 * Test create invalid topic.
	 */
	public void testCreateInvalidTopic() {
		JSONObject object = JsonUtil.createObjectFromString(createTopic);
		object.remove("content");
		object.remove("title");
		Response response = service.create(object.toString());
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Test topic create.
	 */
	@Test public void testTopicCreate() {
		Response response = service.create(createTopic);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		verify(commentService, atLeastOnce()).save(new CommentInstance(), null);
		Object entity = response.getEntity();
		Assert.assertNotNull(entity);
		JSONObject object = JsonUtil.createObjectFromString(entity.toString());
		Assert.assertNotNull(object);
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.CREATED_BY), "emf:user");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TOPIC_ABOUT_SECTION),
				"aboutSection");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.CONTENT),
				"some content");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TAGS), "tags");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TYPE), "type");
		Assert.assertTrue(typeConverter.convert(Date.class,
				JsonUtil.getStringValue(object, ForumProperties.CREATED_ON)) instanceof Date);
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.STATUS), "IN_PROGRESS");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TITLE), "some title");
	}

	/** The update topic. */
	String updateTopic = "{ " + " Id : \"emf\\:topicId\"," + " aboutSection : \"aboutSection\","
			+ " about : { instanceId : \"aboutId\", instanceType : \"aboutType\" },"
			+ " content : \"some content\"," + " tags : \"tags\"," + " type : \"type\","
			+ " createdBy : \"emf\\:user\"," + " createdOn : null," + " status : null,"
			+ " title : \"some title\"" + "}";

	/**
	 * Test update comment.
	 */
	@Test public void testUpdateComment() {
		Response response = service.updateTopic(null, updateTopic);
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		TopicInstance topicInstance = new TopicInstance();
		topicInstance.setId("emf:topicId");
		when(commentService.loadByDbId(eq("emf:topicId")))
				.thenReturn(topicInstance);

		response = service.updateTopic("emf:topicId", updateTopic);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		verify(commentService, atLeastOnce()).save(topicInstance, null);
		Object entity = response.getEntity();
		Assert.assertNotNull(entity);
		JSONObject object = JsonUtil.createObjectFromString(entity.toString());
		Assert.assertNotNull(object);
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.CREATED_BY), "emf:user");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TOPIC_ABOUT_SECTION),
				"aboutSection");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.CONTENT),
				"some content");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TAGS), "tags");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TYPE), "type");
		Assert.assertTrue(typeConverter.convert(Date.class,
				JsonUtil.getStringValue(object, ForumProperties.CREATED_ON)) instanceof Date);
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.STATUS), "IN_PROGRESS");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.TITLE), "some title");
		Assert.assertEquals(JsonUtil.getStringValue(object, ForumProperties.ID), "emf:topicId");
	}

	/**
	 * Topic delete.
	 */
	public void topicDelete() {
		Response response = service.delete(null);
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.delete("");
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.delete("emf:topicId");
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		TopicInstance instance = new TopicInstance();
		instance.setId("emf:topicId");
		verify(commentService, atLeastOnce()).delete(instance, null, false);
	}

	/**
	 * Comment delete.
	 */
	public void commentDelete() {
		Response response = service.deleteComment(null, null);
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.deleteComment("", "");
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.deleteComment("emf:topicId", "emf:commentId");
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		CommentInstance instance = new CommentInstance();
		instance.setId("emf:commentId");
		verify(commentService, atLeastOnce()).delete(instance, null, false);
	}

	/**
	 * Load comments.
	 * 
	 * @throws JSONException
	 */
	public void loadComments() throws JSONException {
		Response response = service.loadComments(null, null, null);
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		response = service.loadComments("emf:topicId", null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertEquals(response.getEntity(), "{}");

		List<CommentInstance> comments = new ArrayList<CommentInstance>();
		CommentInstance commentInstance = new CommentInstance();
		commentInstance.setId("emf:commentId");
		commentInstance.setComment("comment content");
		commentInstance.setFrom("emf:admin");
		commentInstance.setPostedDate(new Date());
		TopicInstance topic = new TopicInstance();
		topic.setId("emf:topicId");
		commentInstance.setTopic(topic);
		topic.getComments().add(commentInstance);
		comments.add(commentInstance);

		when(commentService.loadComments(eq("emf:topicId"), any(Date.class), eq(-1))).thenReturn(
				comments);

		response = service.loadComments("emf:topicId", null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		JSONObject object = JsonUtil.createObjectFromString(response.getEntity().toString());
		Assert.assertNotNull(object);
		JSONArray commentsJson = JsonUtil.getJsonArray(object, "comments");
		Assert.assertNotNull(commentsJson, "Comment was not found");
		Assert.assertEquals(commentsJson.length(), 1);
		JSONObject comment = commentsJson.getJSONObject(0);
		Assert.assertNotNull(comment);

		Assert.assertEquals(JsonUtil.getStringValue(comment, ForumProperties.ID), "emf:commentId");
		Assert.assertEquals(JsonUtil.getStringValue(comment, ForumProperties.CONTENT),
				"comment content");
		Assert.assertNotNull(JsonUtil.getStringValue(comment, ForumProperties.CREATED_ON));
		Assert.assertTrue(typeConverter.convert(Date.class,
				JsonUtil.getStringValue(comment, ForumProperties.CREATED_ON)) instanceof Date);
		Assert.assertEquals(JsonUtil.getStringValue(comment, ForumProperties.CREATED_BY),
				"emf:admin");
		Assert.assertEquals(JsonUtil.getStringValue(comment, ForumProperties.CREATED_BY_LABEL),
				"emf:admin last name");
	}

	/**
	 * Test invalid find all.
	 */
	@Test
	public void testInvalidFindAll() {
		Response response = service.findAll(null, null, null, null, null, null, 25, null, null,
				null, null, null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		response = service.findAll(null, "test", null, null, null, null, 25, null, null, null,
				null, null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Test valid find all.
	 */
	@Test
	public void testValidFindAll() {
		Sorter sort = new Sorter("sortBy", "asc");

		Response response = service.findAll("section", null, null, null, null, null, 20, null,
				null, "sortBy", "asc", null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		verify(commentService, atLeastOnce()).getTopics(eq("section"), any(Date.class), eq(20),
				eq(false), eq(sort), Mockito.anyMap());

		service.findAll(null, "id", "type", null, null, null, 20, null, null, "sortBy", "asc",
				null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		verify(commentService, atLeastOnce()).getTopics(any(InstanceReference.class),
				any(Date.class), eq(20), eq(false), eq(sort), Mockito.anyMap());

		service.findAll(null, "id", "type", null, null, null, 20, true, null, "sortBy", "asc",
				null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		verify(commentService, atLeastOnce()).getInstanceSuccessorsTopics(eq("id"),
				any(Date.class), eq(20), eq(sort), Mockito.anyMap());

		service.findAll(null, null, null, null, null, null, 20, null, "emf:admin", "sortBy", "asc",
				null, null, null, null, null, null, null);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		verify(commentService, atLeastOnce()).getTopicsByUser(eq(currentUser), any(Date.class),
				eq(20), eq(sort), Mockito.anyMap());
	}

}
