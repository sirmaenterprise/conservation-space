package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link RevertContentStep}.
 *
 * @author A. Kunchev
 */
public class RevertContentStepTest {

	@InjectMocks
	private RevertContentStep step;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new RevertContentStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("revertContent", step.getName());
	}

	@Test
	public void invoke_noAdditionalContents_importNotCalled() {
		when(instanceContentService.getContentsForInstance(any(Serializable.class), anyCollection()))
				.thenReturn(emptyList());
		step.invoke(RevertContext.create("version-id-v1.5"));
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void invoke_allContentsFiltered_importNotCalled() {
		when(instanceContentService.getContentsForInstance(any(Serializable.class), anyCollection()))
				.thenReturn(Arrays.asList(ContentInfo.DO_NOT_EXIST, ContentInfo.DO_NOT_EXIST));
		step.invoke(RevertContext.create("version-id-v1.5"));
		verify(instanceContentService, never()).importContent(anyList());
	}

	@Test
	public void invoke_withContents_importCalled() {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(true);
		when(instanceContentService.getContentsForInstance(any(Serializable.class), anyCollection()))
				.thenReturn(Arrays.asList(info));
		step.invoke(RevertContext.create("version-id-v1.5"));
		verify(instanceContentService).importContent(anyList());
	}

	@Test
	public void rollback_noImportedContents_deleteNotCalled() {
		step.rollback(RevertContext.create("version-id-v1.5"));
		verifyZeroInteractions(instanceContentService);
	}

	@Test
	public void rollback_withImportedContents_deleteCalled() {
		List<String> contentIds = Arrays.asList("content-id-1", "content-id-2");
		RevertContext context = RevertContext.create("version-id-v1.5");
		context.put("$importedContents$", contentIds);
		step.rollback(context);
		verify(instanceContentService).deleteContent(eq("content-id-1"), eq(null));
		verify(instanceContentService).deleteContent(eq("content-id-2"), eq(null));
	}

}
