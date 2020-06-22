package com.sirma.itt.seip.instance.content;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.content.publish.UploadRevisionAction;
import com.sirma.itt.seip.instance.content.publish.UploadRevisionRequest;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test for {@link InstanceContentUploadRest}
 *
 * @author BBonev
 */
public class InstanceContentUploadRestTest {
	@InjectMocks
	private InstanceContentUploadRest uploadRest;
	@Mock
	private ContentUploader contentUploader;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private RevisionService revisionService;
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private Actions actions;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void uploadContentToInstance() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.isVersionable()).thenReturn(Boolean.TRUE);
		when(instanceType.getId()).thenReturn("emf:document");
		when(instanceTypeResolver.resolve("emf:instanceId")).thenReturn(Optional.of(instanceType));

		when(contentUploader.uploadForInstance(eq(request), any(Instance.class), eq(Content.PRIMARY_CONTENT), eq(true)))
				.thenReturn(mock(ContentInfo.class));

		ContentInfo response = uploadRest.uploadContentToInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT);
		assertNotNull(response);
		Consumer<Instance> instanceConsumer = (instance) -> assertEquals("emf:document",
				instance.get(DefaultProperties.SEMANTIC_TYPE));
		verify(contentUploader).uploadForInstance(eq(request), argThat(CustomMatcher.of(instanceConsumer)),
				eq(Content.PRIMARY_CONTENT), eq(true));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void uploadContentToNotFoundInstance() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		when(instanceTypeResolver.resolve("emf:instanceId")).thenReturn(Optional.empty());

		uploadRest.uploadContentToInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT);
	}

	@Test
	public void uploadRevisionToCreatedInstance() throws Exception {
		String instanceId = "emf:instance";
		String contentId = "emf:uploaded";
		UploadRequest request = mockRequest();
		mockActionCall(null);
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn(contentId);

		when(contentUploader.uploadWithoutInstance(eq(request), eq(Content.PRIMARY_CONTENT))).thenReturn(contentInfo);

		uploadRest.uploadRevisionToInstance(request, "emf:instance");

		verify(revisionService).publish(
				argThat(matchesPublishInstanceRequest(instanceId, contentId, null, null, null)));
	}

	private static UploadRequest mockRequest() {
		UploadRequest request = mock(UploadRequest.class);
		when(request.resolveFormField(anyString(), anyString())).then(a -> a.getArgumentAt(1, String.class));
		return request;
	}

	@Test(expected = RuntimeException.class)
	public void uploadRevisionShouldThrowException() throws Exception {
		UploadRequest request = mockRequest();
		mockActionCall("emf:contetentId");
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn("emf:uploaded");
		when(contentUploader.uploadForInstance(any(), any(String.class), any(), eq(true))).thenReturn(contentInfo);
		when(revisionService.publish(any())).thenThrow(new RuntimeException());

		uploadRest.uploadRevisionToInstance(request, "emf:instance");
	}

	@Test
	public void uploadRevisionExceptionShouldDeleteContent() throws Exception {
		UploadRequest request = mockRequest();
		mockActionCall("emf:contetentId");
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn("emf:uploaded");
		when(contentUploader.uploadForInstance(any(), any(String.class), any(), eq(true))).thenReturn(contentInfo);
		when(revisionService.publish(any())).thenThrow(new RuntimeException());

		try {
			uploadRest.uploadRevisionToInstance(request, "emf:instance");
		} catch (RuntimeException e) {
			assertTrue(e instanceof ResourceException);
		}

		verify(contentUploader).uploadForInstance(eq(request), eq("emf:instance"), eq(Content.PRIMARY_CONTENT),
				eq(true));
		verify(instanceContentService).deleteContent(eq(contentInfo.getContentId()), eq(Content.PRIMARY_CONTENT));
	}

	@Test
	public void uploadRevisionToUploadedInstance() throws Exception {
		String instanceId = "emf:instance";
		String fileName = "testFile.txt";
		String mimetype = "text/plain";
		String contentId = "emf:uploaded";
		long fileSize = 22L;

		ContentInfo contentInfo = createContentInfo(contentId, fileName, mimetype, fileSize);
		UploadRequest request = mockRequest();
		mockActionCall("emf:contetentId");

		when(contentUploader.uploadForInstance(request, "emf:instance", Content.PRIMARY_CONTENT, true)).thenReturn(
				contentInfo);

		uploadRest.uploadRevisionToInstance(request, "emf:instance");

		verify(contentUploader).uploadForInstance(eq(request), eq("emf:instance"), eq(Content.PRIMARY_CONTENT),
				eq(true));
		verify(revisionService).publish(
				argThat(matchesPublishInstanceRequest(instanceId, null, fileName, mimetype, fileSize)));
	}

	private static ContentInfo createContentInfo(String contentId, String fileName, String mimetype, long fileSize) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn(contentId);
		when(contentInfo.getName()).thenReturn(fileName);
		when(contentInfo.getMimeType()).thenReturn(mimetype);
		when(contentInfo.getLength()).thenReturn(fileSize);
		return contentInfo;
	}

	private static CustomMatcher<PublishInstanceRequest> matchesPublishInstanceRequest(Serializable instanceId,
			String contentIdToPublish, String filename, String mimetype, Long size) {
		return CustomMatcher.of((PublishInstanceRequest publishInstanceRequest) -> {
			assertFalse(publishInstanceRequest.isAsPdf());
			assertEquals(contentIdToPublish, publishInstanceRequest.getContentIdToPublish());
			assertEquals("uploadRevision", publishInstanceRequest.getTriggerOperation().getUserOperationId());
			Instance instance = publishInstanceRequest.getInstanceToPublish();
			assertEquals(instanceId, instance.getId());
			assertEquals(filename, instance.get(DefaultProperties.NAME));
			assertEquals(mimetype, instance.get(DefaultProperties.MIMETYPE));
			assertEquals(size, instance.get(DefaultProperties.CONTENT_LENGTH));
		});
	}

	private void mockActionCall(String contentId) {
		when(actions.callAction(any())).then(
				a -> {
					UploadRevisionRequest request = a.getArgumentAt(0, UploadRevisionRequest.class);
					assertNotNull(request.getUserOperation());
					Instance instance = new EmfInstance(request.getTargetId());
					instance.addIfNotNull(PRIMARY_CONTENT_ID, contentId);
					request.setTargetReference(new InstanceReferenceImpl(request.getTargetId().toString(), null, null,
							instance));
					return new UploadRevisionAction(contentUploader, revisionService, instanceContentService).perform(
							request);
				});
	}
}
