package com.sirma.itt.seip.mail.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mail.extensions.MailAttachmentsScriptProvider.MailAttachments;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link MailAttachmentsScriptProvider}.
 *
 * @author A. Kunchev
 */
public class MailAttachmentsScriptProviderTest {

	@InjectMocks
	private MailAttachmentsScriptProvider provider;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TempFileProvider tempFileProvider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getBindings() {
		assertNotNull(provider.getBindings());
		assertEquals(1, provider.getBindings().size());
		assertNotNull(provider.getBindings().get("attachments"));
	}

	@Test
	public void getScripts() {
		assertEquals(Collections.emptyList(), provider.getScripts());
	}

	@Test
	public void createMailAttachments() {
		assertNotNull(provider.createMailAttachments());
	}

	@Test
	public void createMailAttachments_mailAttachments_collect_noAttachments() {
		assertEquals(0, provider.createMailAttachments().collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromPath_nullPath() {
		MailAttachments attachments = provider.createMailAttachments();
		attachments.addFromPath(null, "");
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromPath_noPaths() {
		MailAttachments attachments = provider.createMailAttachments();
		attachments.addFromPath();
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromPath_fakeFile() {
		MailAttachments attachments = provider.createMailAttachments();
		attachments.addFromPath("file/temp/file");
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromPath_withFile() throws IOException {
		MailAttachments attachments = provider.createMailAttachments();
		File file = Files.createTempFile(null, null).toFile();
		attachments.addFromPath(file.getAbsolutePath());
		when(instanceContentService.saveContent(any(Instance.class), anyListOf(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.getContentId()).thenReturn("contentId");
			return Collections.singletonList(info);
		});
		assertEquals(1, attachments.collect().length);
		file.delete();
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_nullInstance() {
		MailAttachments attachments = provider.createMailAttachments();
		attachments.addFromInstance(null, null);
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_noInstances() {
		MailAttachments attachments = provider.createMailAttachments();
		attachments.addFromInstance();
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_withRealInstance_noTargetInstance() {
		MailAttachments attachments = provider.createMailAttachments();
		ScriptInstance scriptInstance = mock(ScriptInstance.class);
		attachments.addFromInstance(scriptInstance);
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_withRealInstance_noContent() {
		MailAttachments attachments = provider.createMailAttachments();
		ScriptInstance scriptInstance = mock(ScriptInstance.class);
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.NAME, "fileName");
		instance.setId("targetId");
		when(scriptInstance.getTarget()).thenReturn(instance);
		when(instanceContentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT)))
				.thenReturn(mock(ContentInfo.class));
		attachments.addFromInstance(scriptInstance);
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_withRealInstance_contentFromContentService() {
		MailAttachments attachments = provider.createMailAttachments();
		ScriptInstance scriptInstance = mock(ScriptInstance.class);
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.NAME, "fileName");
		instance.setId("targetId");
		when(scriptInstance.getTarget()).thenReturn(instance);
		when(instanceContentService.getContent("targetId", Content.PRIMARY_CONTENT)).thenReturn(mock(ContentInfo.class));
		when(tempFileProvider.createTempFile(anyString(), eq(null))).thenReturn(mock(File.class));
		attachments.addFromInstance(scriptInstance);
		when(instanceContentService.saveContent(any(Instance.class), anyListOf(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.getContentId()).thenReturn("contentId");
			return Collections.singletonList(info);
		});
		assertEquals(1, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_withRealInstance_contentFromInstanceContentService_notExists() {
		MailAttachments attachments = provider.createMailAttachments();
		ScriptInstance scriptInstance = mock(ScriptInstance.class);
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.NAME, "fileName");
		instance.setId("targetId");
		when(scriptInstance.getTarget()).thenReturn(instance);
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT))).thenReturn(contentInfo);
		attachments.addFromInstance(scriptInstance);
		assertEquals(0, attachments.collect().length);
	}

	@Test
	public void createMailAttachments_mailAttachments_addFromInstance_withRealInstance_contentFromInstanceContentService_exists() {
		MailAttachments attachments = provider.createMailAttachments();
		ScriptInstance scriptInstance = mock(ScriptInstance.class);
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.NAME, "fileName");
		instance.setId("targetId");
		when(scriptInstance.getTarget()).thenReturn(instance);
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(tempFileProvider.createTempFile(anyString(), eq(null))).thenReturn(mock(File.class));
		when(instanceContentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT))).thenReturn(contentInfo);
		attachments.addFromInstance(scriptInstance);
		when(instanceContentService.saveContent(any(Instance.class), anyListOf(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.getContentId()).thenReturn("contentId");
			return Collections.singletonList(info);
		});
		assertEquals(1, attachments.collect().length);
	}

}
