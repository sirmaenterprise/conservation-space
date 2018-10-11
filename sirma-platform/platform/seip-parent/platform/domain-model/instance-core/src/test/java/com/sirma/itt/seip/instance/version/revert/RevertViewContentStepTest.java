package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link RevertViewContentStep}.
 *
 * @author A. Kunchev
 */
public class RevertViewContentStepTest {

	@InjectMocks
	public RevertViewContentStep step;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new RevertViewContentStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("revertViewContent", step.getName());
	}

	@Test(expected = ContentNotFoundRuntimeException.class)
	public void invoke_noVersionContent() {
		ContentInfo contentInfo = ContentInfo.DO_NOT_EXIST;
		when(instanceContentService.getContent("version-id-v1.5", Content.PRIMARY_VIEW)).thenReturn(contentInfo);

		step.invoke(RevertContext.create("version-id-v1.5"));
	}

	@Test
	public void invoke_contentStored() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(contentInfo.getInputStream()).thenReturn(new ByteArrayInputStream("idoc-content".getBytes()));
		when(instanceContentService.getContent("version-id-v1.5", Content.PRIMARY_VIEW)).thenReturn(contentInfo);

		Instance reverted = new EmfInstance();
		step.invoke(RevertContext.create("version-id-v1.5").setRevertResultInstance(reverted));

		assertFalse(reverted.getString(TEMP_CONTENT_VIEW).isEmpty());
	}

}
