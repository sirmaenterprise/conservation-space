package com.sirma.itt.seip.instance.version.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.rest.HttpClientTestUtils;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/**
 * Test for {@link VersionCompareContentStore}.
 *
 * @author A. Kunchev
 */
public class VersionCompareContentStoreTest {

	@InjectMocks
	private VersionCompareContentStore store;

	@Mock
	private VersionCompareConfigurations versionCompareConfigurations;

	@Mock
	private ConfigurationProperty<URI> versionCompareConfigurationProperty;

	private static HttpServer server;

	@BeforeClass
	public static void startTestServer() throws HttpException, IOException {
		server = HttpClientTestUtils.createTestServer(stubResponses());
		server.start();
	}

	private static Map<String, HttpRequestHandler> stubResponses() throws HttpException, IOException {
		Map<String, HttpRequestHandler> handlers = new HashMap<>();
		handlers.put("/get/connection-refused", HttpClientTestUtils.buildConnectionRefusedResponse());
		handlers.put("/get/bad-request", HttpClientTestUtils.buildBadRequestResponse());
		handlers.put("/get/ok-file-retrieved-successfully", get_contentRetrievedSuccessfully());

		handlers.put("/delete/connection-refused", HttpClientTestUtils.buildConnectionRefusedResponse());
		handlers.put("/delete/bad-request", HttpClientTestUtils.buildBadRequestResponse());
		handlers.put("/delete/successful", delete_successful());
		return handlers;
	}

	private static HttpRequestHandler get_contentRetrievedSuccessfully() throws HttpException, IOException {
		HttpRequestHandler okWithFile = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.OK.getStatusCode());
			BasicHttpEntity httpEntity = new BasicHttpEntity();
			httpEntity.setContent(new ByteArrayInputStream("content".getBytes()));
			response.setEntity(httpEntity);
			return response;
		}).when(okWithFile).handle(any(), any(), any());
		return okWithFile;
	}

	private static HttpRequestHandler delete_successful() throws HttpException, IOException {
		HttpRequestHandler successful = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.OK.getStatusCode());
			return response;
		}).when(successful).handle(any(), any(), any());
		return successful;
	}

	@AfterClass
	public static void shutdownTestServer() {
		if (server != null) {
			server.shutdown(1, TimeUnit.SECONDS);
		}
	}

	@Before
	public void setup() {
		store = new VersionCompareContentStore();
		MockitoAnnotations.initMocks(this);

		when(versionCompareConfigurations.getServiceBaseUrl()).thenReturn(versionCompareConfigurationProperty);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void add_notSupported() {
		store.add(new EmfInstance(), Content.createEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void update_notSupported() {
		store.update(new EmfInstance(), Content.createEmpty(), null);
	}

	@Test(expected = StoreException.class)
	public void getReadChannel_connectionRefused() {
		when(versionCompareConfigurationProperty.get())
				.thenReturn(HttpClientTestUtils.buildURI(server, "/get/connection-refused"));
		StoreItemInfo info = new StoreItemInfo();
		info.setRemoteId("connectionRefused");
		store.getReadChannel(info);
	}

	@Test(expected = StoreException.class)
	public void getReadChannel_statusBadRequest() {
		when(versionCompareConfigurationProperty.get())
				.thenReturn(HttpClientTestUtils.buildURI(server, "/get/bad-request"));
		StoreItemInfo info = new StoreItemInfo();
		info.setRemoteId("statusBadRequest");
		store.getReadChannel(info);
	}

	@Test
	public void getReadChannel_statusOK_fileDescriptorRetrievedSuccessfully() throws IOException {
		when(versionCompareConfigurationProperty.get())
				.thenReturn(HttpClientTestUtils.buildURI(server, "/get/ok-file-retrieved-successfully"));
		StoreItemInfo info = new StoreItemInfo().setProviderType(VersionCompareContentStore.STORE_NAME);
		info.setRemoteId("fileRetrievedSuccessfully");
		FileDescriptor descriptor = store.getReadChannel(info);
		assertNotNull(descriptor);
		assertEquals("content", descriptor.asString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void delete_notSupported() {
		assertFalse(store.delete(new StoreItemInfo()));
	}

	@Test
	public void delete_statusOK() throws Exception {
		Logger logger = mockLogger();
		when(versionCompareConfigurationProperty.get())
				.thenReturn(HttpClientTestUtils.buildURI(server, "/delete/successful"));
		store.delete(mockDeleteData("success"));
		verify(logger, times(0)).warn(any(String.class), any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void delete_badRequest() throws Exception {
		Logger logger = mockLogger();
		when(versionCompareConfigurationProperty.get()).thenReturn(
				HttpClientTestUtils.buildURI(server, "/delete/bad-request"));
		store.delete(mockDeleteData("badRequest"));
		verify(logger, times(1)).warn(any(String.class), eq(400), any(String.class), any(String.class));
	}

	@Test
	public void delete_connectionRefused() throws Exception {
		Logger logger = mockLogger();
		when(versionCompareConfigurationProperty.get())
				.thenReturn(HttpClientTestUtils.buildURI(server, "/delete/connection-refused"));
		store.delete(mockDeleteData("connection-refused"));
		verify(logger, times(1)).warn(any(String.class), any(String.class), any(String.class), any(String.class));
	}

	@Test
	public void getName() {
		assertEquals("comparedVersionsStore", store.getName());
	}

	private DeleteContentData mockDeleteData(String remoteId) {
		DeleteContentData data = new DeleteContentData();
		data.addProperty("remoteId", remoteId);
		data.addProperty("compareServiceUrl", versionCompareConfigurationProperty.get());
		return data;
	}

	private Logger mockLogger() throws Exception {
		Field field = ReflectionUtils.getClassField(VersionCompareContentStore.class, "LOGGER");
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		Logger logger = mock(Logger.class);
		field.set(null, logger);
		return logger;
	}
}
