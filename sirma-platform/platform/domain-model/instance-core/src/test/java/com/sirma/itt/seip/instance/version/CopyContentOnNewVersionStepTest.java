package com.sirma.itt.seip.instance.version;

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
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.EmfInstance;

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
		verify(instanceContentService, never()).getAllContent(any(Serializable.class));
	}

	@Test
	public void execute_emptyVersionInstanceId_contentServiceNotCalled() {
		EmfInstance version = new EmfInstance();
		version.setId("");

		VersionContext context = VersionContext.create(new EmfInstance());
		context.setVersionInstance(version);

		step.execute(context);
		verify(instanceContentService, never()).getAllContent(any(Serializable.class));
	}

	@Test
	public void execute_noContentsFound_contentServiceImportNotCalled() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");

		EmfInstance version = new EmfInstance();
		version.setId("version-instance-id");

		VersionContext context = VersionContext.create(instance);
		context.setVersionInstance(version);

		when(instanceContentService.getAllContent("instance-id")).thenReturn(new ArrayList<>());

		step.execute(context);
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void execute_onlyNotAllowedContentFound_contentServiceImportNotCalled() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");
		EmfInstance version = new EmfInstance();
		version.setId("version-instance-id");

		VersionContext context = VersionContext.create(instance);
		context.setVersionInstance(version);

		ContentInfo draft = createContentInfoWithPurpose("draft-emf:user-id");
		ContentInfo queriesResult = createContentInfoWithPurpose("queriesResult");
		ContentInfo exportPdf = createContentInfoWithPurpose("f39a51b2-c22b-4f21-bd8e-bdad8129dcd7-export-pdf");
		ContentInfo exportWord = createContentInfoWithPurpose("87ec2d44-76fe-4eb5-84f4-5a8d25449c77-export-word");
		ContentInfo exportXlsx = createContentInfoWithPurpose("7de1f4ff-4338-4583-9acf-fe642ee9b34f-export-xlsx");

		when(instanceContentService.getAllContent("instance-id"))
				.thenReturn(Arrays.asList(draft, queriesResult, exportPdf, exportWord, exportXlsx));

		step.execute(context);
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void execute_contentFound_contentServiceImportCalled() {
		EmfInstance instance = new EmfInstance();
		instance.setId("instance-id");
		EmfInstance version = new EmfInstance();
		version.setId("version-instance-id");

		VersionContext context = VersionContext.create(instance);
		context.setVersionInstance(version);

		ContentInfo primaryContent = createContentInfoWithPurpose(Content.PRIMARY_CONTENT);
		ContentInfo primaryView = createContentInfoWithPurpose(Content.PRIMARY_VIEW);

		when(instanceContentService.getContentsForInstance(eq("instance-id"), anySet()))
				.thenReturn(Arrays.asList(primaryContent, primaryView));

		step.execute(context);
		verify(instanceContentService).importContent(anyList());
	}

	private static ContentInfo createContentInfoWithPurpose(String purpose) {
		ContentInfo info = mock(ContentInfo.class);
		when(info.getContentPurpose()).thenReturn(purpose);
		when(info.exists()).thenReturn(true);
		return info;
	}

}
