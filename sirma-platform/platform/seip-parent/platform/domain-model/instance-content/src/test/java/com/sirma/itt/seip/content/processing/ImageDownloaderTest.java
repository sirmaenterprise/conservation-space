package com.sirma.itt.seip.content.processing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.rest.client.HTTPClient;

/**
 * Test for {@link ImageDownloader}
 *
 * @author BBonev
 */
public class ImageDownloaderTest {

	@InjectMocks
	private ImageDownloader downloader;

	@Mock
	private HTTPClient httpClient;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_returnImageContentAndContentType_onSuccess() throws Exception {
		URI address = URI.create("http://localhost/image.jpg");

		when(httpClient.execute(any(), any(), any(), any(), any())).then(a -> {
			BiFunction<Integer, HttpResponse, Pair<String, byte[]>> responseHandler = a.getArgumentAt(3,
					BiFunction.class);

			HttpResponse response = mock(HttpResponse.class);
			HttpEntity entity = mock(HttpEntity.class);
			when(entity.getContentLength()).thenReturn(4L);
			Header header = mock(Header.class);
			when(header.getValue()).thenReturn("image/jpg; charset=UTF-8");
			when(entity.getContentType()).thenReturn(header);
			when(response.getEntity()).thenReturn(entity);
			doAnswer(aa -> {
				aa.getArgumentAt(0, OutputStream.class).write("test".getBytes(StandardCharsets.UTF_8));
				return null;
			}).when(entity).writeTo(any());
			return responseHandler.apply(200, response);
		});

		downloader.download(address, (contentType, data) -> {
			assertEquals("image/jpg", contentType);
			assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), data);
			return null;
		}, () -> {
			fail("This should not be called");
			return null;
		});
	}

	@Test
	public void shouldNot_downloadImageContent_ifContentExceeds100MB() throws Exception {
		URI address = URI.create("http://localhost/image.jpg");

		when(httpClient.execute(any(), any(), any(), any(), any())).then(a -> {
			BiFunction<Integer, HttpResponse, Pair<String, byte[]>> responseHandler = a.getArgumentAt(3,
					BiFunction.class);

			HttpResponse response = mock(HttpResponse.class);
			HttpEntity entity = mock(HttpEntity.class);
			when(entity.getContentLength()).thenReturn(ImageDownloader.MAX_DOWNLOADS_SIZE + 1);
			Header header = mock(Header.class);
			when(header.getValue()).thenReturn("image/jpg; charset=UTF-8");
			when(entity.getContentType()).thenReturn(header);
			when(response.getEntity()).thenReturn(entity);
			return responseHandler.apply(200, response);
		});

		String response = downloader.download(address, (contentType, data) -> {
			fail("This should not be called");
			return null;
		}, () -> "failed");

		assertEquals("failed", response);
	}

	@Test
	public void should_doNothing_ifResponseNot200() throws Exception {
		URI address = URI.create("http://localhost/image.jpg");

		when(httpClient.execute(any(), any(), any(), any(), any())).then(a -> {
			BiFunction<Integer, HttpResponse, Pair<String, byte[]>> responseHandler = a.getArgumentAt(3,
					BiFunction.class);
			HttpResponse response = mock(HttpResponse.class);
			return responseHandler.apply(404, response);
		});

		String response = downloader.download(address, (contentType, data) -> {
			fail("This should not be called");
			return null;
		}, () -> "failed");

		assertEquals("failed", response);
	}

	@Test
	public void should_doNothing_onCommunicationError() throws Exception {
		URI address = URI.create("http://localhost/image.jpg");

		when(httpClient.execute(any(), any(), any(), any(), any())).then(a -> {
			BiFunction<Integer, HttpResponse, Pair<String, byte[]>> responseHandler = a.getArgumentAt(3,
					BiFunction.class);
			HttpResponse response = mock(HttpResponse.class);
			HttpEntity entity = mock(HttpEntity.class);
			when(entity.getContentLength()).thenReturn(4L);
			Header header = mock(Header.class);
			when(header.getValue()).thenReturn("image/jpg; charset=UTF-8");
			when(entity.getContentType()).thenReturn(header);
			when(response.getEntity()).thenReturn(entity);
			doThrow(IOException.class).when(entity).writeTo(any());
			return responseHandler.apply(200, response);
		});

		String response = downloader.download(address, (contentType, data) -> {
			fail("This should not be called");
			return null;
		}, () -> "failed");

		assertEquals("failed", response);
	}

	@Test
	public void should_returnDefault_OnMissingAddress() throws Exception {
		String response = downloader.download(null, (contentType, data) -> {
			fail("This should not be called");
			return null;
		}, () -> "failed");

		assertEquals("failed", response);
	}

	@Test
	public void should_returnDefault_OnDownloadFailure() throws Exception {
		URI address = URI.create("http://localhost/image.jpg");

		when(httpClient.execute(any(), any(), any(), any(), any())).then(a -> {
			Function<IOException, Pair<String, byte[]>> responseHandler = a.getArgumentAt(4, Function.class);
			return responseHandler.apply(new IOException());
		});
		String response = downloader.download(address, (contentType, data) -> {
			fail("This should not be called");
			return null;
		}, () -> "failed");

		assertEquals("failed", response);
	}
}
