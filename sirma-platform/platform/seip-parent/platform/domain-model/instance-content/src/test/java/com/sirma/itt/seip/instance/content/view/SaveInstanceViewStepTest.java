package com.sirma.itt.seip.instance.content.view;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static com.sirma.itt.seip.instance.relation.LinkConstants.HAS_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link SaveInstanceViewStep}.
 *
 * @author A. Kunchev
 */
public class SaveInstanceViewStepTest {

	@InjectMocks
	private SaveInstanceViewStep step;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TemplateService templateService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Mock
	private SchedulerService schedulerService;

	@Spy
	private InstancePropertyNameResolver nameResolver = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		step = new SaveInstanceViewStep();
		MockitoAnnotations.initMocks(this);

		when(instanceContentService.getContent(any(Instance.class), eq(Content.PRIMARY_VIEW)))
				.thenReturn(ContentInfo.DO_NOT_EXIST);
	}

	@Test
	public void beforeSave_viewContentSendWithInstnace_newInstanceContentRollbackNotScheduled() {
		Instance instance = new EmfInstance();
		String viewContent = "instance-view-content";
		instance.add(TEMP_CONTENT_VIEW, viewContent);
		mockInstanceType(instance, true, true);
		mockSaveContent("instance-view-content-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		step.beforeSave(context);

		verifyContent(context.getInstance(), content -> assertEquals(content, viewContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
		verifyZeroInteractions(schedulerService, transactionSupport);
	}

	@Test
	public void beforeSave_viewContentSendWithInstnace_linkSetToTemplate_contentRollbackScheduled() throws IOException {
		Instance instance = new EmfInstance("instance-id");
		String viewContent = "instance-view-content";
		instance.add(TEMP_CONTENT_VIEW, viewContent);
		instance.add(HAS_TEMPLATE, "template-instance-id");
		mockInstanceType(instance, true, true);
		mockSaveContent("instance-view-content-id");
		String contentId = stubContentRollbackSchedule(instance);
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		step.beforeSave(context);

		verifyContent(context.getInstance(), content -> assertEquals(content, viewContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
		assertEquals("template-instance-id", context.getInstance().getString(LinkConstants.HAS_TEMPLATE));
		verifyContentRollbackSchedule(instance, contentId);
	}

	private String stubContentRollbackSchedule(Instance instance) throws IOException {
		String contentId = "content-id";
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getContentId()).thenReturn(contentId);
		when(info.asString(eq(StandardCharsets.UTF_8))).thenReturn("content");
		when(instanceContentService.getContent(instance, Content.PRIMARY_VIEW)).thenReturn(info);
		return contentId;
	}

	private void verifyContentRollbackSchedule(Instance instance, String contentId) {
		verify(transactionSupport).invokeOnFailedTransactionInTx(any());
		verify(schedulerService).schedule(eq("saveInstanceView"), argThat(configurationMatcher()),
				argThat(contextMatcher(instance, contentId)));
	}

	private static CustomMatcher<SchedulerConfiguration> configurationMatcher() {
		return CustomMatcher.of((SchedulerConfiguration configuration) -> {
			assertEquals(SchedulerEntryType.TIMED, configuration.getType());
			assertTrue(configuration.isRemoveOnSuccess());
			assertTrue(configuration.isPersistent());
			assertEquals(TransactionMode.REQUIRED, configuration.getTransactionMode());
			assertEquals(2, configuration.getMaxRetryCount());
			assertEquals(2, configuration.getRetryDelay().longValue());
			assertTrue(configuration.isIncrementalDelay());
		});
	}

	private static CustomMatcher<SchedulerContext> contextMatcher(Instance instance, String contentId) {
		return CustomMatcher.of((SchedulerContext context) -> {
			assertEquals(instance.getId(), context.get("instanceId"));
			assertEquals(contentId, context.getIfSameType("contentId", String.class));
			assertEquals("content", context.getIfSameType("oldViewContent", String.class));
		});
	}

	@Test
	public void beforeSave_viewContentSendWithInstnace_withDefaultTemplate_previousLinkToTemplateRemoved() {
		Instance instance = new EmfInstance();
		String viewContent = "instance-view-content";
		instance.add(TEMP_CONTENT_VIEW, viewContent);
		instance.add(HAS_TEMPLATE, "emf:defaultTemplate");
		mockInstanceType(instance, true, true);
		mockSaveContent("instance-view-content-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		step.beforeSave(context);

		verifyContent(context.getInstance(), content -> assertEquals(content, viewContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
		assertNull(context.getInstance().getString(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void beforeSave_viewContentSendWithInstnace_linkToTemplateRemoved() {
		Instance instance = new EmfInstance();
		String viewContent = "instance-view-content";
		instance.add(TEMP_CONTENT_VIEW, viewContent);
		instance.add(HAS_TEMPLATE, (Serializable) Arrays.asList((String) null));
		mockInstanceType(instance, true, true);
		mockSaveContent("instance-view-content-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());

		step.beforeSave(context);

		verifyContent(context.getInstance(), content -> assertEquals(content, viewContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
		assertNull(context.getInstance().getString(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void beforeSave_withoutContent_defaultTemplateSetAsTemplate() {
		Instance instance = new EmfInstance();
		mockInstanceType(instance, false, true);
		instance.add(HAS_TEMPLATE, "emf:defaultTemplate");
		when(templateService.getContent("emf:defaultTemplate")).thenReturn("blank content");
		mockGetContent();
		mockSaveContent("content-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);

		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertNull(instance.getString(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void should_setViewBasedOnHasTemplateRelationShip() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("definition-id");

		final String TEMPLATE_INSTANCE_ID = "test_template";
		final String TEMPLATE_CONTENT = "test content";

		instance.add(HAS_TEMPLATE, TEMPLATE_INSTANCE_ID);

		stubTemplates(TEMPLATE_INSTANCE_ID, TEMPLATE_CONTENT);

		mockGetContent();
		// There is a default template so return it instead of the actual template.
		mockInstanceType(instance, false, true);
		mockSaveContent(TEMPLATE_CONTENT);

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);

		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals(TEMPLATE_CONTENT, viewId.get());

		assertEquals(TEMPLATE_INSTANCE_ID, instance.getString(LinkConstants.HAS_TEMPLATE));
	}

	@Test
	public void should_setViewBasedOnATemplate() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("definition-id");
		instance.setProperties(new HashMap<String, Serializable>());

		stubTemplates(null, null);

		mockGetContent();
		// There is a default template so return it instead of the actual template.
		mockInstanceType(instance, false, true);
		mockSaveContent("blank content");

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);

		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("blank content", viewId.get());

		assertEquals("blank", instance.getString(LinkConstants.HAS_TEMPLATE));
	}

	private void stubTemplates(String templateInstanceId, String content) {
		List<Template> templates = new ArrayList<>();

		Template template1 = new Template();
		template1.setId("template1");
		template1.setCorrespondingInstance("blank");
		template1.setContent("blank content");

		when(templateService.getContent("blank")).thenReturn("blank content");

		templates.add(template1);

		Template template2 = new Template();
		template2.setId(templateInstanceId);
		template2.setCorrespondingInstance("template2");
		template2.setContent(content);

		when(templateService.getContent(templateInstanceId)).thenReturn(content);

		templates.add(template2);

		when(templateService.getTemplate(any(TemplateSearchCriteria.class))).thenReturn(template1);
	}

	private static void mockInstanceType(Instance instance, boolean creatable, boolean uploadable) {
		InstanceType type = mock(InstanceType.class);
		when(type.isUploadable()).thenReturn(creatable);
		when(type.isCreatable()).thenReturn(uploadable);
		instance.setType(type);
	}

	private void mockGetContent() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent(any(Serializable.class), anyString())).thenReturn(contentInfo);
	}

	private void verifyContent(Instance instance, Consumer<String> consumer) {
		verify(instanceContentService).saveContent(eq(instance), contentMatcher(consumer));
	}

	private static Content contentMatcher(Consumer<String> consumer) {
		return argThat(CustomMatcher.of((Content content) -> {
			assertNotNull(content.getName());
			assertTrue(content.isView());
			assertEquals(MediaType.TEXT_HTML, content.getMimeType());
			try {
				consumer.accept(content.getContent().asString());
			} catch (Exception e) {
				fail(e.getMessage());
			}
			assertEquals(Content.PRIMARY_VIEW, content.getPurpose());
		}));
	}

	private void mockSaveContent(String contentId) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn(contentId);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).thenReturn(contentInfo);
	}

	@Test
	public void getName() {
		assertEquals("saveInstanceView", step.getName());
	}

	/* ***************** ROLLBACK ********************************/

	@Test
	public void execute_contentUpdateCalled() throws Exception {
		SchedulerContext context = new SchedulerContext(2);
		context.put("instanceId", "instance-id");
		context.put("contentId", "content-id");
		context.put("name", "Arkham Files - The Riddler");
		context.put("versionable", true);
		context.put("oldViewContent", "Ha-Ha-Ha-!");
		step.execute(context);
		verify(instanceContentService).updateContent(eq("content-id"), eq(new EmfInstance("instance-id")),
				contentMatcher(content -> assertEquals("Ha-Ha-Ha-!", content)));
	}
}
