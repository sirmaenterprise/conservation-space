package com.sirma.sep.content.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.intThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.InstanceContentService;

/**
 * Tests for {@link ContentDownloadRestService}
 *
 * @author BBonev
 */
public class ContentDownloadServiceTest {

	private static final String PARTIAL_RANGE = "bytes=0-19";
	private static final Range BYTE_RANGE = Range.fromString(PARTIAL_RANGE);

	@InjectMocks
	private ContentDownloadService service;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TempFileProvider tempFileProvider;

	private static BaseMatcher<Integer> responseMatcher = new BaseMatcher<Integer>() {

		@Override
		public boolean matches(Object item) {
			if (item instanceof Number) {
				return ((Number) item).intValue() == 200 || ((Number) item).intValue() == 206;
			}
			return false;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("wanted 200 or 206");
		}
	};

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getContentHead() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		Response response = service.getContentHeadResponse("contentId", "purpose");
		verifyOkHeaderResponse(response, contentInfo);
	}

	@Test
	public void getContentHead_unknownSize() throws Exception {
		ContentInfo contentInfo = mockExistingContentUnknownSize();

		Response response = service.getContentHeadResponse("contentId", "purpose");

		assertNotNull(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertNull(response.getHeaderString("Accept-Ranges"));
		assertNull(response.getHeaderString("Content-Length"));
		assertEquals(contentInfo.getMimeType(), response.getHeaderString("Content-Type"));
	}

	@Test
	public void getContentHead_notFound() throws Exception {
		mockNonExistingContent();

		Response response = service.getContentHeadResponse("contentId", "purpose");
		assertNotNull(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void getContentHead_invalidRequest() throws Exception {
		Response response = service.getContentHeadResponse("", "purpose");
		assertNotNull(response);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	private static void verifyOkHeaderResponse(Response response, ContentInfo contentInfo) {
		assertNotNull(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(Range.BYTES, response.getHeaderString("Accept-Ranges"));
		assertEquals("" + contentInfo.getLength(), response.getHeaderString("Content-Length"));
		assertEquals(contentInfo.getMimeType(), response.getHeaderString("Content-Type"));
	}

	@Test
	public void getContent() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.ALL, false, servletResponse, null);
		verifyOkResponse(servletResponse, contentInfo, contentInfo.getLength());
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_unknownSize() throws Exception {
		ContentInfo contentInfo = mockExistingContentUnknownSize();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.ALL, false, servletResponse, null);
		verifyOkResponse(servletResponse, contentInfo, contentInfo.getLength());
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_range_fromStart() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.fromString("bytes=0-49"), false, servletResponse, null);
		verifyOkResponse(servletResponse, contentInfo, 50L);
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_custom_filename() throws IOException {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.ALL, true, servletResponse, "customName");
		verifyOkResponseForDownload(servletResponse, contentInfo, contentInfo.getLength(), "customName");
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_range_remaining() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.fromString("bytes=50-"), false, servletResponse, null);
		verifyOkResponse(servletResponse, contentInfo, contentInfo.getLength() - 50);
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_download() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.ALL, true, servletResponse, null);
		verifyOkResponseForDownload(servletResponse, contentInfo, contentInfo.getLength(), null);
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_download_interruptedWrite() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		try (ServletOutputStream outputStream = mock(ServletOutputStream.class)) {
			doThrow(IOException.class).when(outputStream).write(any(byte[].class), anyInt(), anyInt());
			when(servletResponse.getOutputStream()).thenReturn(outputStream);

			service.sendContent("contentId", "purpose", Range.ALL, true, servletResponse, null);
			verifyOkResponseForDownload(servletResponse, contentInfo, contentInfo.getLength(), null);
			verify(servletResponse).getOutputStream();
		}
	}

	@Test
	public void getContent_download_notFound() throws Exception {
		mockNonExistingContent();

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		service.sendContent("contentId", "purpose", Range.ALL, true, httpServletResponse, null);
		verify(httpServletResponse).setStatus(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void getContent_download_invalidRequest() throws Exception {
		mockNonExistingContent();
		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		service.sendContent("", "purpose", Range.ALL, true, httpServletResponse, null);
		verify(httpServletResponse).setStatus(Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	public void getContent_download_range_moreThanAvailable() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		service.sendContent("contentId", "purpose", new Range(null, contentInfo.getLength(), -1), true,
				httpServletResponse, null);
		verify(httpServletResponse).setStatus(Status.REQUESTED_RANGE_NOT_SATISFIABLE.getStatusCode());
	}

	@Test
	public void getContent_download_range_fromStart() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.fromString("bytes=0-49"), true, servletResponse, null);
		verifyOkResponseForDownload(servletResponse, contentInfo, 50, null);
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContent_download_range_ramainning() throws Exception {
		ContentInfo contentInfo = mockExistingContent();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

		service.sendContent("contentId", "purpose", Range.fromString("bytes=50-"), true, servletResponse, null);
		verifyOkResponseForDownload(servletResponse, contentInfo, contentInfo.getLength() - 50, null);
		verify(servletResponse).getOutputStream();
	}

	@Test
	public void getContentPreview() throws Exception {
		mockExistingContentPreview();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		File file = File.createTempFile("test", ".tmp");
		when(tempFileProvider.createTempFile(anyString(), eq(""))).thenReturn(file);

		service.sendPreview("contentId", "purpose", servletResponse, Range.ALL);
		verifyOkPreviewResponse(servletResponse);
		verify(servletResponse).getOutputStream();
		verify(tempFileProvider).createTempFile(anyString(), eq(""));
		verify(tempFileProvider).deleteFile(any(File.class));
		FileTestUtils.deleteFile(file);
	}

	@Test
	public void getContentPreview_invalidRequest() throws Exception {
		mockNonExistingContentPreview();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		service.sendPreview("", "purpose", servletResponse, Range.ALL);
		verify(servletResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void getContentPreview_NotFound() throws Exception {
		mockNonExistingContentPreview();

		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		service.sendPreview("contentId", "purpose", servletResponse, Range.ALL);
		verify(servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void getContentPreview_Partial() throws Exception {
		mockExistingContentPreview();
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		File file = File.createTempFile("test", ".tmp");
		when(tempFileProvider.createTempFile(anyString(), eq(""))).thenReturn(file);

		service.sendPreview("contentId", "purpose", servletResponse, BYTE_RANGE);
		verifyOkPreviewResponse(servletResponse);
		verify(servletResponse).getOutputStream();
		verify(servletResponse).setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		FileTestUtils.deleteFile(file);
	}

	@Test
	public void sendFile_nullFile_notFoundResponse() {
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		service.sendFile(null, null, true, servletResponse, null, null);
		verify(servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void sendFile_fileDoesNotExist_notFoundResponse() {
		File file = mock(File.class);
		when(file.exists()).thenReturn(false);
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		service.sendFile(file, null, true, servletResponse, null, null);
		verify(servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void sendFile_fileDoesNotExist_okResponse() throws IOException {
		File file = File.createTempFile("test-file", ".tmp");
		HttpServletResponse servletResponse = mock(HttpServletResponse.class);
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		service.sendFile(file, Range.ALL, false, servletResponse, null, MediaType.TEXT_HTML);
		verify(servletResponse).setStatus(intThat(responseMatcher));
		FileTestUtils.deleteFile(file);
	}

	private static void verifyOkResponse(HttpServletResponse servletResponse, ContentInfo contentInfo,
			long expectedLength) {
		verify(servletResponse).setStatus(intThat(responseMatcher));
		if (expectedLength == -1L) {
			verify(servletResponse, never()).setContentLengthLong(expectedLength);
		} else {
			verify(servletResponse).setContentLengthLong(expectedLength);
			verify(servletResponse).setHeader("Accept-Ranges", Range.BYTES);
		}
		verify(servletResponse).setCharacterEncoding("UTF-8");
		verify(servletResponse).setContentType(contentInfo.getMimeType());
		verify(servletResponse, never()).setHeader(eq("Content-Disposition"), anyString());
		if (expectedLength == contentInfo.getLength()) {
			verify(servletResponse, never()).setHeader(eq("Content-Range"), anyString());
		} else {
			verify(servletResponse).setHeader(eq("Content-Range"), anyString());
		}
	}

	private static void verifyOkPreviewResponse(HttpServletResponse servletResponse) {
		verify(servletResponse).setStatus(intThat(responseMatcher));
		verify(servletResponse).setContentLengthLong(anyLong());
		verify(servletResponse).setCharacterEncoding("UTF-8");
		verify(servletResponse).setContentType("application/pdf");
		verify(servletResponse, never()).setHeader(eq("Content-Disposition"), anyString());
	}

	private static void verifyOkResponseForDownload(HttpServletResponse servletResponse, ContentInfo contentInfo,
			long expectedLength, String customName) {
		verify(servletResponse).setStatus(Matchers.intThat(responseMatcher));
		verify(servletResponse).setContentLengthLong(expectedLength);
		verify(servletResponse).setHeader("Accept-Ranges", Range.BYTES);
		verify(servletResponse).setCharacterEncoding("UTF-8");
		verify(servletResponse).setContentType(contentInfo.getMimeType());
		String name = contentInfo.getName();
		if (StringUtils.isNotBlank(customName)) {
			name = customName;
		}
		verify(servletResponse).setHeader("Content-Disposition",
				"attachment; filename=\"" + name + "\"; filename*=utf-8''" + name);
		if (expectedLength == contentInfo.getLength()) {
			verify(servletResponse, never()).setHeader(eq("Content-Range"), anyString());
		} else {
			verify(servletResponse).setHeader(eq("Content-Range"), anyString());
		}
	}

	private ContentInfo mockExistingContentUnknownSize() {
		ContentInfoMock contentInfo = new ContentInfoMock(true, -1L);
		when(instanceContentService.getContent(anyString(), anyString())).thenReturn(contentInfo);
		return contentInfo;
	}

	private ContentInfo mockExistingContent() {
		ContentInfoMock contentInfo = new ContentInfoMock(true);
		when(instanceContentService.getContent(anyString(), anyString())).thenReturn(contentInfo);
		return contentInfo;
	}

	private ContentInfo mockExistingContentPreview() {
		ContentInfoMock contentInfo = new ContentInfoMock(true);
		when(instanceContentService.getContentPreview(anyString(), anyString())).thenReturn(contentInfo);
		return contentInfo;
	}

	private ContentInfo mockNonExistingContentPreview() {
		ContentInfoMock contentInfo = new ContentInfoMock(false);
		when(instanceContentService.getContentPreview(anyString(), anyString())).thenReturn(contentInfo);
		return contentInfo;
	}

	private void mockNonExistingContent() {
		when(instanceContentService.getContent(anyString(), anyString())).thenReturn(new ContentInfoMock(false));
	}

	/**
	 * The {@link ContentInfo} mock
	 *
	 * @author BBonev
	 */
	private static class ContentInfoMock implements ContentInfo {

		private static final long serialVersionUID = -1L;
		private boolean exists;
		private Long length;

		/**
		 * Instantiates a new content info mock.
		 *
		 * @param exists
		 *            the exists
		 */
		public ContentInfoMock(boolean exists) {
			this.exists = exists;
		}

		/**
		 * Instantiates a new content info mock.
		 *
		 * @param exists
		 *            the exists
		 * @param length
		 *            the length
		 */
		public ContentInfoMock(boolean exists, long length) {
			this.exists = exists;
			this.length = length;
		}

		@Override
		public String getId() {
			return "contentId";
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return getClass().getResourceAsStream(getName());
		}

		@Override
		public void close() {
			// nothing
		}

		@Override
		public String getName() {
			return ContentDownloadServiceTest.class.getSimpleName() + ".class";
		}

		@Override
		public String getContentId() {
			return "contentId";
		}

		@Override
		public Serializable getInstanceId() {
			return "instanceId";
		}

		@Override
		public String getContentPurpose() {
			return "purpose";
		}

		@Override
		public boolean exists() {
			return exists;
		}

		@Override
		public String getMimeType() {
			return MediaType.APPLICATION_OCTET_STREAM_TYPE.toString();
		}

		@Override
		public long getLength() {
			if (length == null) {
				try (InputStream stream = getInputStream()) {
					length = Long.valueOf(stream.available());
				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
			return length.longValue();
		}

		@Override
		public boolean isView() {
			return false;
		}

		@Override
		public String getCharset() {
			return "UTF-8";
		}

		@Override
		public String getRemoteId() {
			return "remoteId";
		}

		@Override
		public String getRemoteSourceName() {
			return "remoteSystemName";
		}

		@Override
		public ContentMetadata getMetadata() {
			return ContentMetadata.NO_METADATA;
		}

		@Override
		public boolean isIndexable() {
			return false;
		}

		@Override
		public boolean isReuseable() {
			return false;
		}

		@Override
		public String getChecksum() {
			return "checksum";
		}

	}

}
