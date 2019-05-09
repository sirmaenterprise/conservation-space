package com.sirma.sep.content.upload;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.sep.content.ContentConfigurations;

/**
 * Test for {@link UploadRequestReader}
 *
 * @author BBonev
 */
public class UploadRequestReaderTest {

	@InjectMocks
	private UploadRequestReader requestReader;
	@Mock
	private RepositoryFileItemFactory repositoryFileItemFactory;
	@Spy
	private InstanceProxyMock<RepositoryFileItemFactory> fileItemFactory = new InstanceProxyMock<>(null);
	@Mock
	private ContentConfigurations contentConfigurations;
	@Mock
	private HttpServletRequest request;
	@Spy
	private Statistics statistics = NoOpStatistics.INSTANCE;

	@Before
	@SuppressWarnings("boxing")
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		fileItemFactory.set(repositoryFileItemFactory);
		when(contentConfigurations.getMaxFileSize()).thenReturn(new ConfigurationPropertyMock<>(Long.MAX_VALUE));
	}

	@Test
	public void isReadable() throws Exception {
		assertTrue(requestReader.isReadable(UploadRequest.class, null, null, MediaType.MULTIPART_FORM_DATA_TYPE));
		assertFalse(requestReader.isReadable(UploadRequestReaderTest.class, null, null,
				MediaType.MULTIPART_FORM_DATA_TYPE));
		assertFalse(requestReader.isReadable(UploadRequest.class, null, null, MediaType.APPLICATION_JSON_TYPE));
		assertFalse(
				requestReader.isReadable(UploadRequestReaderTest.class, null, null, MediaType.APPLICATION_JSON_TYPE));
	}

	@Test
	public void readFrom() throws Exception {
		when(request.getContentType()).thenReturn(MediaType.MULTIPART_FORM_DATA + ";boundary=-----");
		when(request.getInputStream()).thenAnswer(a -> buildServletInputStream());

		UploadRequest uploadRequest = requestReader.readFrom(null, null, null, null, null, null);
		assertNotNull(uploadRequest);
		assertNotNull(uploadRequest.getRequestItems());
		assertTrue(uploadRequest.getRequestItems().isEmpty());
		assertNotNull(uploadRequest.getFileItemFactory());
	}

	@Test(expected = WebApplicationException.class)
	public void readFrom_invalidRequest() throws Exception {
		requestReader.readFrom(null, null, null, null, null, null);
	}

	@SuppressWarnings("boxing")
	private static Object buildServletInputStream() throws IOException {
		ServletInputStream inputStream = mock(ServletInputStream.class);
		when(inputStream.available()).thenReturn(-1);
		when(inputStream.read()).thenReturn(-1);
		when(inputStream.isFinished()).thenReturn(Boolean.TRUE);
		when(inputStream.read(any())).thenReturn(-1);
		when(inputStream.read(any(), anyInt(), anyInt())).thenReturn(-1);

		return inputStream;
	}

}
