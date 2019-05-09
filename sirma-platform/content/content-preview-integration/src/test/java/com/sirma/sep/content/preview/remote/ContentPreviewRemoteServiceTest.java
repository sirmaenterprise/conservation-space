package com.sirma.sep.content.preview.remote;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import javax.json.Json;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Tests the logic for determining mime type support in {@link ContentPreviewRemoteService} from the remote preview
 * service.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewRemoteServiceTest {

	private static final String APP_JSON = ContentType.APPLICATION_JSON.getMimeType();

	@Mock
	private ContentPreviewConfigurations previewConfigurations;

	@Mock
	private HTTPClient httpClient;

	@InjectMocks
	private ContentPreviewRemoteService previewRemoteService;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
		stubPreviewConfigurations();
		previewRemoteService.initialize();
	}

	@Test
	public void getSupport_shouldExecuteProvidedRequest() {
		ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
		ArgumentCaptor<HttpHost> hostCaptor = ArgumentCaptor.forClass(HttpHost.class);

		previewRemoteService.getMimeTypeSupport(APP_JSON);
		Mockito.verify(httpClient)
				.execute(requestCaptor.capture(), Matchers.any(), hostCaptor.capture(), Matchers.any(), Matchers.any
						());

		HttpUriRequest request = requestCaptor.getValue();
		Assert.assertEquals(HttpGet.METHOD_NAME, request.getMethod());
		Assert.assertTrue(request.getURI().getQuery().contains(APP_JSON));

		HttpHost host = hostCaptor.getValue();
		Assert.assertEquals("http://localhost:8300", host.toString());
	}

	@Test
	public void getSupport_shouldProperlyReadJson() {
		Mockito.when(httpClient.execute(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any
				()))
				.then(invocation -> {
					HttpResponse response = Mockito.mock(HttpResponse.class);
					HttpEntity entity = Mockito.mock(HttpEntity.class);
					Mockito.when(response.getEntity()).thenReturn(entity);
					Mockito.when(entity.getContent())
							.thenReturn(new ByteArrayInputStream(getTestJson(APP_JSON, true, false, true)));
					return applyResponseHandler(invocation, 200, response);
				});

		MimeTypeSupport support = previewRemoteService.getMimeTypeSupport(APP_JSON);
		assertMimeTypeSupport(support, true, false, true);
	}

	@Test
	public void getSupport_shouldHandleUnsuccessfulRequests() {
		Mockito.when(httpClient.execute(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any
				()))
				.then(invocation -> applyResponseHandler(invocation, 404, Mockito.mock(HttpResponse.class)));

		MimeTypeSupport support = previewRemoteService.getMimeTypeSupport(APP_JSON);
		assertMimeTypeSupport(support, false, false, false);
	}

	@Test(expected = EmfRuntimeException.class)
	public void getSupport_shouldBlowOnUnreachableRemoteService() {
		Mockito.when(httpClient.execute(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any
				()))
				.then(this::applyErrorHandler);
		previewRemoteService.getMimeTypeSupport(APP_JSON);
	}

	@Test(expected = EmfRuntimeException.class)
	public void getSupport_shouldNotSupportImproperJson() {
		Mockito.when(httpClient.execute(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any
				()))
				.then(invocation -> {
					HttpResponse response = Mockito.mock(HttpResponse.class);
					HttpEntity entity = Mockito.mock(HttpEntity.class);
					Mockito.when(response.getEntity()).thenReturn(entity);
					Mockito.when(entity.getContent()).thenThrow(new IOException());
					return applyResponseHandler(invocation, 200, response);
				});
		previewRemoteService.getMimeTypeSupport(APP_JSON);
	}

	private MimeTypeSupport applyResponseHandler(InvocationOnMock invocation, int statusCode, HttpResponse response)
			throws IOException {
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(statusCode);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		ResponseHandler<MimeTypeSupport> responseHandler = invocation.getArgumentAt(3, ResponseHandler.class);
		return responseHandler.handleResponse(response);
	}

	private Object applyErrorHandler(InvocationOnMock invocation) {
		Function errorHandler = invocation.getArgumentAt(4, Function.class);
		return errorHandler.apply(new IOException());
	}

	private void assertMimeTypeSupport(MimeTypeSupport mimeTypeSupport, boolean preview, boolean selfPreview,
			boolean thumbnail) {
		Assert.assertNotNull(mimeTypeSupport);
		Assert.assertEquals(preview, mimeTypeSupport.supportsPreview());
		Assert.assertEquals(selfPreview, mimeTypeSupport.isSelfPreview());
		Assert.assertEquals(thumbnail, mimeTypeSupport.supportsThumbnail());
	}

	private byte[] getTestJson(String mimetype, boolean preview, boolean selfPreview, boolean thumbnail) {
		return Json.createObjectBuilder()
				.add("name", mimetype)
				.add("supportsPreview", preview)
				.add("isSelfPreview", selfPreview)
				.add("supportsThumbnail", thumbnail)
				.build()
				.toString()
				.getBytes(StandardCharsets.UTF_8);
	}

	private void stubPreviewConfigurations() {
		Mockito.when(previewConfigurations.getPreviewServiceAddress())
				.thenReturn(new ConfigurationPropertyMock<>("localhost:8300"));
	}
}
