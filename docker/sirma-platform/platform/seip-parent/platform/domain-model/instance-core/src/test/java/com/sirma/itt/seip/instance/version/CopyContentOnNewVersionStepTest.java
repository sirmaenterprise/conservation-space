package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link CopyContentOnNewVersionStep}.
 *
 * @author A. Kunchev
 */
public class CopyContentOnNewVersionStepTest {

	@InjectMocks
	private CopyContentOnNewVersionStep step;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new CopyContentOnNewVersionStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("copyContentOnNewVersion", step.getName());
	}

	@Test
	public void execute_nullVersionInstanceId_contentServiceNotCalled() {
		VersionContext context = VersionContext.create(new EmfInstance());
		context.setVersionInstance(new EmfInstance());

		step.execute(context);
		verify(instanceContentService, never()).getContentsForInstance(any(Serializable.class), anySet());
	}

	@Test
	public void execute_emptyVersionInstanceId_contentServiceNotCalled() {
		EmfInstance version = new EmfInstance();
		version.setId("");

		VersionContext context = VersionContext.create(new EmfInstance());
		context.setVersionInstance(version);

		step.execute(context);
		verify(instanceContentService, never()).getContentsForInstance(any(Serializable.class), anySet());
	}

	@Test
	public void execute_noContentsFound_contentServiceImportNotCalled() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");

		EmfInstance version = new EmfInstance();
		version.setId("version-instance-id");

		VersionContext context = VersionContext.create(instance);
		context.setVersionInstance(version);

		when(instanceContentService.getContentsForInstance(eq("instance-id"), anySet())).thenReturn(emptyList());

		step.execute(context);
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void execute_contentDoesNotExist_currentDeletedForVersionButNewNotImported() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");
		EmfInstance version = new EmfInstance();
		version.setId("version-instance-id");

		VersionContext context = VersionContext.create(instance);
		context.setVersionInstance(version);

		ContentInfo primaryContent = createContentInfoWithPurpose(Content.PRIMARY_CONTENT);
		when(primaryContent.exists()).thenReturn(Boolean.FALSE);

		when(instanceContentService.getContentsForInstance(eq("instance-id"), anySet()))
				.thenReturn(Collections.singletonList(primaryContent));
		when(instanceContentService.deleteContent(any(), eq(Content.PRIMARY_CONTENT))).thenReturn(Boolean.TRUE);

		step.execute(context);
		verify(instanceContentService).deleteContent(any(), eq(Content.PRIMARY_CONTENT));
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void execute_contentFound_contentServiceImportCalled() {
		VersionContext context = VersionContext.create(new EmfInstance("instance-id"));
		context.setVersionInstance(new EmfInstance("version-instance-id"));

		ContentInfo primaryContent = createContentInfoWithPurpose(Content.PRIMARY_CONTENT);

		when(instanceContentService.getContentsForInstance(eq("instance-id"), anySet()))
				.thenReturn(Collections.singletonList(primaryContent));

		step.execute(context);
		verify(instanceContentService).deleteContent(eq("version-instance-id"), eq(Content.PRIMARY_CONTENT));
		verify(instanceContentService).importContent(anyList());
	}

	private static ContentInfo createContentInfoWithPurpose(String purpose) {
		ContentInfo info = mock(ContentInfo.class);
		when(info.getContentPurpose()).thenReturn(purpose);
		when(info.exists()).thenReturn(true);
		return info;
	}
}