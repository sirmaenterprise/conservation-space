package com.sirma.itt.seip.instance.actions.clone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link InstanceCloneAction}.
 *
 * @author A. Kunchev
 */
public class InstanceCloneActionTest {

	@InjectMocks
	private InstanceCloneAction action;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void setUp() {
		action = new InstanceCloneAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("clone", action.getName());
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullClonedInstance() {
		InstanceCloneRequest request = new InstanceCloneRequest();
		request.setClonedInstance(null);
		action.perform(request);
	}

	@Test
	public void perform_noPrimatyContentAndViewInInstance() throws IOException {
		InstanceCloneRequest request = new InstanceCloneRequest();

		request.setTargetId("target-instance-id");
		when(instanceContentService.getContent("target-instance-id", Content.PRIMARY_CONTENT))
				.thenReturn(ContentInfo.DO_NOT_EXIST);

		EmfInstance clonedInstance = new EmfInstance();
		clonedInstance.setId("cloned-instance-id");

		try (InputStream stream = getClass()
				.getClassLoader()
					.getResourceAsStream("clone-action-view-content-test.html")) {

			clonedInstance.add(DefaultProperties.TEMP_CONTENT_VIEW, IOUtils.toString(stream));
			request.setClonedInstance(clonedInstance);
			action.perform(request);

			verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
				return "clone".equals(context.getOperation().getOperation());
			})));
		}
	}

	@Test
	public void perform_withPrimatyContentAndViewNotInInstance() throws IOException {
		InstanceCloneRequest request = new InstanceCloneRequest();

		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(true);
		when(info.asString(StandardCharsets.UTF_8)).thenReturn("target-primary-content");
		request.setTargetId("target-instance-id");
		when(instanceContentService.getContent("target-instance-id", Content.PRIMARY_CONTENT)).thenReturn(info);

		EmfInstance clonedInstance = new EmfInstance();
		clonedInstance.setId("cloned-instance-id");
		try (InputStream stream = getClass()
				.getClassLoader()
					.getResourceAsStream("clone-action-view-content-test.html")) {

			ContentInfo contentInfo = mock(ContentInfo.class);
			when(contentInfo.asString(StandardCharsets.UTF_8)).thenReturn(IOUtils.toString(stream));
			when(instanceContentService.getContent("target-instance-id", Content.PRIMARY_VIEW)).thenReturn(contentInfo);

			ContentInfo clonedPrimaryContent = mock(ContentInfo.class);
			when(clonedPrimaryContent.getContentId()).thenReturn("primary-content-id");
			when(instanceContentService.saveContent(any(Instance.class), any(Content.class)))
					.thenReturn(clonedPrimaryContent);
			request.setClonedInstance(clonedInstance);
			action.perform(request);

			verify(domainInstanceService).save(any(InstanceSaveContext.class));
		}
	}

}
