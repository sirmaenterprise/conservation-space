package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Test for {@link IiifThumbnailProvider}
 *
 * @author BBonev
 */
public class IiifThumbnailProviderTest {

	@InjectMocks
	private IiifThumbnailProvider provider;
	@Spy
	private ImageServerConfigurationsMock imageServerConfigurations = new ImageServerConfigurationsMock();
	@Mock
	private InstanceContentService contentService;
	@Mock
	private HTTPClient httpClient;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		imageServerConfigurations.setEnabled(true);
	}

	@Test
	public void createThumbnailEndPoint_notEnabled() throws Exception {
		imageServerConfigurations.setEnabled(false);
		assertNull(provider.createThumbnailEndPoint(null));
	}

	@Test
	public void createThumbnailEndPoint_notValidContent() throws Exception {
		ContentInfo contentInfo = mock(ContentInfo.class);
		EmfInstance instance = new EmfInstance();
		instance.add(MIMETYPE, "image/jpg");
		when(contentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

		assertNull(provider.createThumbnailEndPoint(instance));

		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getRemoteSourceName()).thenReturn("alfresco4");

		assertNull(provider.createThumbnailEndPoint(instance));
	}

	@Test
	public void createThumbnailEndPoint() throws Exception {
		ContentInfo contentInfo = mock(ContentInfo.class);
		EmfInstance instance = new EmfInstance();
		instance.add(MIMETYPE, "image/jpg");

		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getRemoteSourceName()).thenReturn(ImageServerConfigurations.IIIF);
		when(contentInfo.getRemoteId()).thenReturn("testImage.jpg");
		when(contentService.getContent(instance, Content.PRIMARY_CONTENT)).thenReturn(contentInfo);

		assertEquals("testImage", provider.createThumbnailEndPoint(instance));
	}

	@Test
	public void getThumbnail_notEnabled() throws Exception {
		imageServerConfigurations.setEnabled(false);

		String thumbnail = provider.getThumbnail("testImageName");
		assertNull(thumbnail);
	}

	@Test
	public void getThumbnail() throws Exception {
		imageServerConfigurations.setIiifServerAddress("http://localhost/");

		mockHttpClientResponse();

		String thumbnail = provider.getThumbnail("testImageName");
		assertNotNull(thumbnail);
		assertTrue(thumbnail.startsWith("data:image/jpg;base64,"));
		String content = thumbnail.substring("data:image/jpg;base64,".length());
		byte[] decode = Base64.getDecoder().decode(content);
		assertEquals("test", new String(decode));
	}

	@Test
	public void getThumbnail_noData() throws Exception {
		imageServerConfigurations.setIiifServerAddress("http://localhost/");

		String thumbnail = provider.getThumbnail("testImageName");
		assertNull(thumbnail);
	}

	@Test
	public void getThumbnail_couldNotReadFromRemoteService() throws Exception {
		imageServerConfigurations.setIiifServerAddress("http://localhost/");

		when(httpClient.execute(any(URI.class), any(ResponseHandler.class))).thenThrow(EmfRuntimeException.class);

		String thumbnail = provider.getThumbnail("testImageName");
		assertNull(thumbnail);
	}

	@Test
	public void getThumbnail_couldNotReadResponse() throws Exception {
		imageServerConfigurations.setIiifServerAddress("http://localhost/");

		when(httpClient.execute(any(URI.class), any(ResponseHandler.class))).thenThrow(IOException.class);

		String thumbnail = provider.getThumbnail("testImageName");
		assertNull(thumbnail);
	}

	@Test
	public void getThumbnail_noResponse() throws Exception {
		imageServerConfigurations.setIiifServerAddress("http://localhost/");

		String thumbnail = provider.getThumbnail("testImageName");
		assertNull(thumbnail);
	}

	private void mockHttpClientResponse() {
		doAnswer(invocation -> {
			ResponseHandler responseReader = invocation.getArgumentAt(1, ResponseHandler.class);
			HttpResponse httpResponse = mock(HttpResponse.class);
			HttpEntity httpEntity = mock(HttpEntity.class);
			StatusLine statusLine = mock(StatusLine.class);
			when(statusLine.getStatusCode()).thenReturn(200);
			when(httpResponse.getStatusLine()).thenReturn(statusLine);
			when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
			when(httpResponse.getEntity()).thenReturn(httpEntity);
			return responseReader.handleResponse(httpResponse);
		}).when(httpClient).execute(any(URI.class), any(ResponseHandler.class));
	}

}
