package com.sirma.itt.seip.instance.content;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.upload.ContentUploader;
import com.sirma.itt.seip.content.upload.UploadRequest;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;

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

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void uploadContentToInstance() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.isVersionable()).thenReturn(Boolean.TRUE);
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.getType()).thenReturn(instanceType);

		when(instanceTypeResolver.resolveReference("emf:instanceId")).thenReturn(Optional.of(reference));

		when(contentUploader.uploadForInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT, true))
				.thenReturn(mock(ContentInfo.class));

		ContentInfo response = uploadRest.uploadContentToInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT);
		assertNotNull(response);
		verify(contentUploader).uploadForInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT, true);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void uploadContentToNotFoundInstance() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		when(instanceTypeResolver.resolveReference("emf:instanceId")).thenReturn(Optional.empty());

		uploadRest.uploadContentToInstance(request, "emf:instanceId", Content.PRIMARY_CONTENT);
	}
}
