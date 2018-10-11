package com.sirma.itt.seip.instance.content;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.rest.ContentDownloadService;

/**
 * Tests for {@link InstanceContentDownloadRestService}
 *
 * @author BBonev
 */
public class InstanceContentDownloadRestServiceTest {

	private static final String INSTANCE_ID = "instanceId";
	private static final String PURPOSE = "purpose";
	private static final String PARTIAL_RANGE = "bytes=0-2222880";
	private static final Range BYTE_RANGE = Range.fromString(PARTIAL_RANGE);
	private static final Range RANGE = Range.ALL;

	@InjectMocks
	private InstanceContentDownloadRestService service;

	@Mock
	private ContentDownloadService downloadService;

	@Mock
	private InstanceAccessEvaluator accessEvaluator;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(downloadService.getContentHeadResponse(anyString(), anyString())).thenReturn(mock(Response.class));
	}

	@Test
	public void getContentHead() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		Response response = service.getContentHead(INSTANCE_ID, "");
		assertNotNull(response);
		verify(downloadService).getContentHeadResponse(INSTANCE_ID, Content.PRIMARY_CONTENT);
	}

	@Test
	public void getContentHead_customPurpose() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		Response response = service.getContentHead(INSTANCE_ID, PURPOSE);
		assertNotNull(response);
		verify(downloadService).getContentHeadResponse(INSTANCE_ID, PURPOSE);
	}

	@Test
	public void streamContent() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, "", null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, false, response, null);
	}

	@Test
	public void streamContentForDownload() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, "", "", RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, true, response, null);
	}

	@Test
	public void streamContentCustomPurpose() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, PURPOSE, null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, PURPOSE, RANGE, false, response, null);
	}

	@Test
	public void streamView() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamView(INSTANCE_ID, "", null, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_VIEW, RANGE, false, response, null);
	}

	@Test
	public void streamViewForDownload() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamView(INSTANCE_ID, "", "", response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_VIEW, RANGE, true, response, null);
	}

	@Test
	public void streamViewCustomPurpose() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamView(INSTANCE_ID, PURPOSE, null, response);
		verify(downloadService).sendContent(INSTANCE_ID, PURPOSE, RANGE, false, response, null);
	}

	@Test
	public void streamPreview() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamPreview(INSTANCE_ID, "", null,response);
		verify(downloadService).sendPreview(INSTANCE_ID, Content.PRIMARY_CONTENT,response, null);
	}
	
	@Test
	public void streamPreviewPartial() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(true);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamPreview(INSTANCE_ID, "", BYTE_RANGE, response);
		verify(downloadService).sendPreview(INSTANCE_ID, Content.PRIMARY_CONTENT, response, BYTE_RANGE);
	}

	@Test(expected = NoPermissionsException.class)
	public void getContentHeadNoPermissions() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(false);
		Response response = service.getContentHead(INSTANCE_ID, "");
		assertNotNull(response);
		verify(downloadService).getContentHeadResponse(INSTANCE_ID, Content.PRIMARY_CONTENT);
	}

	@Test(expected = NoPermissionsException.class)
	public void streamContentNoPermissions() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamContent(INSTANCE_ID, "", null, RANGE, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_CONTENT, RANGE, false, response, null);
	}

	@Test(expected = NoPermissionsException.class)
	public void streamViewNoPermissions() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamView(INSTANCE_ID, "", null, response);
		verify(downloadService).sendContent(INSTANCE_ID, Content.PRIMARY_VIEW, RANGE, false, response, null);
	}

	@Test(expected = NoPermissionsException.class)
	public void streamPreviewNoPermissions() throws Exception {
		when(accessEvaluator.canRead(anyString())).thenReturn(false);
		HttpServletResponse response = mock(HttpServletResponse.class);
		service.streamPreview(INSTANCE_ID, "", null, response);
		verify(downloadService).sendPreview(INSTANCE_ID, Content.PRIMARY_CONTENT, response, null);
	}
}