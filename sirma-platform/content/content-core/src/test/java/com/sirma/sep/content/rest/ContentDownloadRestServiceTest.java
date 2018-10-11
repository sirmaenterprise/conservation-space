package com.sirma.sep.content.rest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.Range;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.rest.ContentDownloadRestService;
import com.sirma.sep.content.rest.ContentDownloadService;

/**
 * Tests for {@link ContentDownloadRestService}
 *
 * @author BBonev
 */
public class ContentDownloadRestServiceTest {

	private static final String INSTANCE_ID = "instanceId";
	private static final String PURPOSE = "purpose";
	private static final Range RANGE = Range.ALL;

	@InjectMocks
	private ContentDownloadRestService service;

	@Mock
	private ContentDownloadService downloadService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(downloadService.getContentHeadResponse(anyString(), anyString())).thenReturn(mock(Response.class));
	}

	@Test
	public void getContentHead() throws Exception {
		Response response = service.getContentHead(INSTANCE_ID, Content.PRIMARY_CONTENT);
		assertNotNull(response);
		verify(downloadService).getContentHeadResponse(INSTANCE_ID, Content.PRIMARY_CONTENT);
	}

	@Test
	public void getContentHead_customPurpose() throws Exception {
		Response response = service.getContentHead(INSTANCE_ID, PURPOSE);
		assertNotNull(response);
		verify(downloadService).getContentHeadResponse(INSTANCE_ID, PURPOSE);
	}

	@Test
	public void streamContent() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, Content.PRIMARY_CONTENT, null, null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, false, response, null);
	}

	@Test
	public void streamContentForDownload() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, Content.PRIMARY_CONTENT, "", null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, true, response, null);
	}

	@Test
	public void streamStaticContent() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamStaticContent(INSTANCE_ID, Content.PRIMARY_CONTENT, "", null, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, true, response, null);
		verify(response).addHeader(HttpHeaders.CACHE_CONTROL, "max-age=86400");
	}

	@Test
	public void streamContentCustomPurpose() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, PURPOSE, null, null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, PURPOSE, RANGE, false, response, null);
	}

	@Test
	public void streamPreview() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamPreview(INSTANCE_ID, PURPOSE, null, response);
		verify(downloadService).sendPreview(INSTANCE_ID, PURPOSE, response, null);
	}
	
	@Test
	public void streamPreviewPartial() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		Range range = Range.fromString("bytes=0-21");
		service.streamPreview(INSTANCE_ID, PURPOSE, range, response);
		verify(downloadService).sendPreview(INSTANCE_ID, PURPOSE, response, range);
	}

}
