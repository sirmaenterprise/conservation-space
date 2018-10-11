package com.sirma.itt.seip.instance.version.compare;

import static com.sirma.itt.seip.testutil.rest.HttpClientTestUtils.buildURI;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.rest.HttpClientTestUtils;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotImportedException;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link VersionCompareConfigurationsImpl}.
 *
 * @author A. Kunchev
 */
public class VersionCompareServiceImplTest {

	@InjectMocks
	private VersionCompareService service;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private VersionCompareConfigurations versionCompareConfigurations;

	@Mock
	private ConfigurationProperty<URI> versionCompareConfigurationProperty;

	@Mock
	private InstanceContentService instanceContentService;

	private Map<String, String> headers;

	private static HttpServer server;

	@BeforeClass
	public static void startTestServer() throws HttpException, IOException {
		server = HttpClientTestUtils.createTestServer(stubResponses());
		server.start();
	}

	private static Map<String, HttpRequestHandler> stubResponses() throws HttpException, IOException {
		Map<String, HttpRequestHandler> handlers = new HashMap<>();
		handlers.put("/post/connection-refused", HttpClientTestUtils.buildConnectionRefusedResponse());
		handlers.put("/post/bad-request", HttpClientTestUtils.buildBadRequestResponse());
		handlers.put("/post/ok/without-file-name", buildOkWithFileName());
		handlers.put("/post/ok/successful", buildOkSuccessful());
		return handlers;
	}

	private static HttpRequestHandler buildOkWithFileName() throws HttpException, IOException {
		HttpRequestHandler okWithoutFileName = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.OK.getStatusCode());
			response.setEntity(new StringEntity("{}", StandardCharsets.UTF_8));
			return response;
		}).when(okWithoutFileName).handle(any(), any(), any());
		return okWithoutFileName;
	}

	private static HttpRequestHandler buildOkSuccessful() throws HttpException, IOException {
		HttpRequestHandler okWithoutFileName = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.OK.getStatusCode());
			JsonObject responseBody = Json
					.createObjectBuilder()
					.add("fileName", "test-file-name.pdf")
					.add("fileSize", 1024)
					.add("mimeType", "application/pdf")
					.build();
			response.setEntity(new StringEntity(responseBody.toString(), StandardCharsets.UTF_8));
			return response;
		}).when(okWithoutFileName).handle(any(), any(), any());
		return okWithoutFileName;
	}

	@AfterClass
	public static void shutdownTestServer() {
		if (server != null) {
			server.shutdown(1, TimeUnit.SECONDS);
		}
	}

	@Before
	public void setup() throws URISyntaxException {
		service = new VersionCompareServiceImpl();
		MockitoAnnotations.initMocks(this);

		when(systemConfiguration.getRESTRemoteAccessUrl())
		.thenReturn(new ConfigurationPropertyMock<>(new URI("http://system-test-address:8000/emf/api")));

		when(versionCompareConfigurations.getServiceBaseUrl()).thenReturn(versionCompareConfigurationProperty);
		when(versionCompareConfigurations.getExpirationTime()).thenReturn(24);

		headers = new HashMap<>(1);
		headers.put("cookie", "test-cookie-header");
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_restRemoteAccessUrlConfigurationNotSet() {
		ConfigurationProperty<URI> configurationProperty = mock(ConfigurationProperty.class);
		when(configurationProperty.isNotSet()).thenReturn(true);
		when(systemConfiguration.getRESTRemoteAccessUrl()).thenReturn(configurationProperty);
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.0", "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_compareUrlConfigurationNotSet() {
		ConfigurationProperty<URI> configurationProperty = mock(ConfigurationProperty.class);
		when(configurationProperty.isNotSet()).thenReturn(true);
		when(versionCompareConfigurations.getServiceBaseUrl()).thenReturn(configurationProperty);
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.0", "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdEmpty() {
		VersionCompareContext context = VersionCompareContext.create("", "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdEmpty() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdNull() {
		VersionCompareContext context = VersionCompareContext.create(null, "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdNull() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", null, headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_identifiersMatch() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_nullAuthenticationHeaders() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.2", null);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_emptyAuthenticationHeaders() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.2",
				new HashMap<>());
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdNotVersion() {
		VersionCompareContext context = VersionCompareContext.create("instance-id", "instance-id-v1.1", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdNotVersion() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id", headers);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdDoesNotMatchOriginal() {
		VersionCompareContext context = VersionCompareContext
				.create("bad-id-v1.0", "instance-id-v1.1", headers)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdDoesNotMatchOriginal() {
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "bad-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test
	public void compareVersionsContent_contentAlreadyExist() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(true);
		when(contentInfo.getContentId()).thenReturn("compared-versions-content-id");
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);

		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		String link = service.compareVersionsContent(context);
		assertEquals("/content/compared-versions-content-id?download=true", link);
	}

	@Test(expected = VersionCompareException.class)
	public void compareVersionsContent_externalServiceCalled_connectionRefused() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);

		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/connection-refused"));
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = VersionCompareException.class)
	public void compareVersionsContent_externalServiceCalled_badRequestResponse() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);

		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/bad-request"));
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = VersionCompareException.class)
	public void compareVersionsContent_externalServiceCalled_okWithoutFileName() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);

		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/ok/without-file-name"));
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = ContentNotImportedException.class)
	public void compareVersionsContent_externalServiceCalled_okFailedToImportContent() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);
		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/ok/successful"));
		when(instanceContentService.importContent(any(ContentImport.class))).thenReturn(null);

		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		String link = service.compareVersionsContent(context);
		assertEquals("", link);
		verify(instanceContentService, never()).deleteContent(any(Serializable.class), eq("comparedVersions"), eq(24),
				eq(TimeUnit.HOURS));
	}

	@Test
	public void compareVersionsContent_externalServiceCalled_okSuccessful() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		when(instanceContentService.getContent("instance-id-v1.1-instance-id-v1.3", "comparedVersions"))
		.thenReturn(contentInfo);
		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/ok/successful"));
		when(instanceContentService.importContent(any(ContentImport.class))).thenReturn("content-id");

		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", headers)
				.setOriginalInstanceId("instance-id");
		String link = service.compareVersionsContent(context);
		assertEquals("/content/content-id?download=true", link);
		verify(instanceContentService).deleteContent(eq("content-id"), eq("comparedVersions"), eq(24),
				eq(TimeUnit.HOURS));
	}

}
