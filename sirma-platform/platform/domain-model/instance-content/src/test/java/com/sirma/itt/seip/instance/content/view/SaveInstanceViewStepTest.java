package com.sirma.itt.seip.instance.content.view;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirmaenterprise.sep.content.idoc.sanitizer.IdocSanitizer;

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
	private TemplateService documentTemplateService;

	@Mock
	private IdocSanitizer idocSanitizer;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Before
	public void setup() {
		step = new SaveInstanceViewStep();
		MockitoAnnotations.initMocks(this);

		when(idocSanitizer.sanitize(anyString())).then(AdditionalAnswers.returnsFirstArg());
	}

	@Test
	public void beforeSave_viewContentSendWithInstnace() {
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
	}

	@Test
	public void beforeSave_contentFromTemplate_defaultTemplate() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("definition-id");

		mockGetContent();
		String viewContent = "default-template-content";
		// There is a default template so return it instead of the actual template.
		when(documentTemplateService.getDefaultTemplateContent()).thenReturn(viewContent);
		mockInstanceType(instance, false, true);
		mockSaveContent("instance-view-content-id");

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);
		verifyContent(context.getInstance(), content -> assertEquals(content, viewContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
	}

	@Test
	public void beforeSave_contentFromTemplate_uploadableTemplate() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("definition-id");
		instance.add(PRIMARY_CONTENT_ID, "content-id");

		mockInstanceType(instance, true, true);
		mockGetContent();
		mockSaveContent("instance-view-content-id");

		String uploadableContent = "uploadable-content";
		TemplateInstance template = new TemplateInstance();
		template.add(CONTENT, uploadableContent);
		when(documentTemplateService.getPrimaryTemplate(instance.getIdentifier(), TemplatePurposes.UPLOADABLE))
				.thenReturn(template);
		when(documentTemplateService.loadContent(any())).thenReturn(template);

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);
		verifyContent(context.getInstance(), expected -> assertEquals(expected, uploadableContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
	}

	@Test
	public void beforeSave_contentFromTemplate_creatableTemplate() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("definition-id");

		mockGetContent();
		String creatableContent = "creatable content";
		TemplateInstance template = new TemplateInstance();
		template.add(CONTENT, creatableContent);
		when(documentTemplateService.getPrimaryTemplate(instance.getIdentifier(), TemplatePurposes.CREATABLE))
				.thenReturn(template);
		when(documentTemplateService.loadContent(any())).thenReturn(template);
		mockInstanceType(instance, true, true);
		mockSaveContent("instance-view-content-id");

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);
		verifyContent(context.getInstance(), expected -> assertEquals(expected, creatableContent));
		Optional<String> viewId = context.getViewId();
		assertTrue(viewId.isPresent());
		assertEquals("instance-view-content-id", viewId.get());
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
		verify(instanceContentService).saveContent(eq(instance), argThat(CustomMatcher.of((Content content) -> {
			assertNotNull(content.getName());
			assertTrue(content.isView());
			assertEquals(MediaType.TEXT_HTML, content.getMimeType());
			try {
				consumer.accept(content.getContent().asString());
			} catch (Exception e) {
				fail(e.getMessage());
			}
			assertEquals(Content.PRIMARY_VIEW, content.getPurpose());
		})));
	}

	private void mockSaveContent(String contentId) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn(contentId);
		when(instanceContentService.saveContent(any(Serializable.class), any(Content.class))).thenReturn(contentInfo);
	}

	@Test
	public void rollback_viewIdNotPresent_deleteNotCalled() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setViewId(Optional.empty());
		step.rollbackBeforeSave(context, new RuntimeException());
		verify(instanceContentService, never()).deleteContent(anyString(), eq(null));
	}

	@Test
	public void rollback_viewIdPresent_deleteCalled() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
					.setViewId(Optional.of("instance-view-content-id"));
		step.rollbackBeforeSave(context, new RuntimeException());
		verify(instanceContentService).deleteContent(eq("instance-view-content-id"), eq(null));
	}

	@Test
	public void getName() {
		assertEquals("saveInstanceView", step.getName());
	}

}
