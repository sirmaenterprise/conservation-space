package com.sirma.sep.content.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.rest.ContentUploadRest;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test for {@link ContentUploadRest}
 *
 * @author BBonev
 */
public class ContentUploadRestTest {

	@InjectMocks
	private ContentUploadRest uploadRest;
	@Mock
	private ContentUploader contentUploader;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void uploadFromMultipart() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT)).thenReturn(info);

		ContentInfo response = uploadRest.addContent(request, Content.PRIMARY_CONTENT);
		assertNotNull(response);
		verify(contentUploader).uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		assertEquals(info, response);
	}
}
