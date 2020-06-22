package com.sirma.sep.content.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Test for {@link ContentUploadRest}
 *
 * @author BBonev
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentUploadRestTest {

	@Mock
	ContentUploader contentUploader;

	@Mock
	Statistics stats;

	@InjectMocks
	ContentUploadRest uploadRest;

	@Test
	public void uploadFromMultipart() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.length()).thenReturn(10L);
		when(contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT)).thenReturn(info);

		ContentInfo response = uploadRest.addContent(request, Content.PRIMARY_CONTENT);
		assertNotNull(response);
		verify(contentUploader).uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		assertEquals(info, response);

		verify(stats).value("content_upload_size_bytes", 10L);
	}

	@Test
	public void uploadFromMultipartUnknownLength() throws Exception {
		UploadRequest request = mock(UploadRequest.class);
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.length()).thenReturn(-1L);
		when(contentUploader.uploadWithoutInstance(request, Content.PRIMARY_CONTENT)).thenReturn(info);

		ContentInfo response = uploadRest.addContent(request, Content.PRIMARY_CONTENT);
		assertNotNull(response);
		verify(contentUploader).uploadWithoutInstance(request, Content.PRIMARY_CONTENT);
		assertEquals(info, response);

		verify(stats, Mockito.never()).value(Mockito.anyString(), Mockito.anyLong());
	}
}
