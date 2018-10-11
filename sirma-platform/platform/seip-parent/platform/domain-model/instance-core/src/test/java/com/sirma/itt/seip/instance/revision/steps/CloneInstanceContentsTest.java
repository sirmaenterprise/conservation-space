package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link CloneInstanceContents}
 *
 * @author BBonev
 */
public class CloneInstanceContentsTest {
	@InjectMocks
	private CloneInstanceContents cloneUploadedDocument;
	@Mock
	private InstanceContentService contentService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldIgnoreNonUploadedInstances() throws Exception {
		PublishInstanceRequest publishRequest = new PublishInstanceRequest(new EmfInstance(), new Operation(), null,
				null);
		PublishContext context = new PublishContext(publishRequest, new EmfInstance());

		cloneUploadedDocument.execute(context);

		verify(contentService, never()).getContent(any(Serializable.class), anyString());
	}

	@Test
	public void shouldDoNothingIfUploadedContentDoNotExist() throws Exception {
		EmfInstance instanceToPublish = new EmfInstance();
		instanceToPublish.add(PRIMARY_CONTENT_ID, "contentId");
		PublishInstanceRequest publishRequest = new PublishInstanceRequest(instanceToPublish, new Operation(), null,
				null);
		PublishContext context = new PublishContext(publishRequest, new EmfInstance());
		when(contentService.getContent(any(Serializable.class), anyString())).thenReturn(ContentInfo.DO_NOT_EXIST);

		cloneUploadedDocument.execute(context);

		verify(contentService, never()).copyContentAsync(any(Serializable.class), anyString());
		verify(contentService, never()).copyContent(any(Serializable.class), anyString());
	}

	@Test
	public void shouldTriggerContentCopyOnValidData() throws Exception {
		EmfInstance instanceToPublish = new EmfInstance();
		instanceToPublish.add(PRIMARY_CONTENT_ID, "contentId");
		PublishInstanceRequest publishRequest = new PublishInstanceRequest(instanceToPublish, new Operation(), null,
				null);
		EmfInstance revision = new EmfInstance();
		revision.setId("emf:instance-r1.0");
		PublishContext context = new PublishContext(publishRequest, revision);

		ContentInfo info = mock(ContentInfo.class);
		when(info.getContentId()).thenReturn("contentId");
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(contentService.getContent(any(Serializable.class), anyString())).thenReturn(info);

		when(contentService.importContent(any(ContentImport.class))).thenReturn("contentId");

		cloneUploadedDocument.execute(context);

		verify(contentService, times(2)).importContent(argThat(CustomMatcher.ofPredicate(
				(ContentImport content) -> nullSafeEquals(content.getInstanceId(), "emf:instance-r1.0"))));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void shouldFailIfContentImportFails() throws Exception {
		EmfInstance instanceToPublish = new EmfInstance();
		instanceToPublish.add(PRIMARY_CONTENT_ID, "contentId");
		PublishInstanceRequest publishRequest = new PublishInstanceRequest(instanceToPublish, new Operation(), null,
				null);
		PublishContext context = new PublishContext(publishRequest, new EmfInstance());

		ContentInfo info = mock(ContentInfo.class);
		when(info.getContentId()).thenReturn("contentId");
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(contentService.getContent(any(Serializable.class), anyString())).thenReturn(info);

		when(contentService.copyContentAsync(any(Serializable.class), anyString())).thenReturn(info);

		cloneUploadedDocument.execute(context);

	}
}
