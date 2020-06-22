package com.sirma.itt.seip.instance.version.compare;

import static com.sirma.itt.seip.testutil.rest.HttpClientTestUtils.buildURI;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.rest.HttpClientTestUtils;

/**
 * Test for {@link VersionCompareConfigurationsImpl}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionCompareServiceImplTest {

	@InjectMocks
	private VersionCompareServiceImpl service;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private VersionCompareConfigurations versionCompareConfigurations;

	@Mock
	private ConfigurationProperty<URI> versionCompareConfigurationProperty;

	@Mock
	private TempFileProvider fileProvider;

	@Mock
	private SecurityContext securityContext;

	private String auth = "xxx.yyy.zzz";

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

			InputStreamEntity entity = new InputStreamEntity(
					new ByteArrayInputStream("test file content\n".getBytes(StandardCharsets.UTF_8)));
			response.setEntity(entity);
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
		when(fileProvider.createTempFile(any(), any())).thenAnswer((invocation) -> {
			File tempFile = Files.createTempFile(null, null).toFile();
			tempFile.deleteOnExit();
			return tempFile;
		});

		when(securityContext.getRequestId()).then((invocation) -> UUID.randomUUID().toString());

		when(systemConfiguration.getRESTRemoteAccessUrl())
		.thenReturn(new ConfigurationPropertyMock<>(new URI("http://system-test-address:8000/emf/api")));

		when(versionCompareConfigurations.getServiceBaseUrl()).thenReturn(versionCompareConfigurationProperty);
		when(versionCompareConfigurations.getExpirationTime()).thenReturn(24);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_restRemoteAccessUrlConfigurationNotSet() {
		ConfigurationProperty<URI> configurationProperty = mock(ConfigurationProperty.class);
		when(configurationProperty.isNotSet()).thenReturn(true);
		when(systemConfiguration.getRESTRemoteAccessUrl()).thenReturn(configurationProperty);
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.0", "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_compareUrlConfigurationNotSet() {
		ConfigurationProperty<URI> configurationProperty = mock(ConfigurationProperty.class);
		when(configurationProperty.isNotSet()).thenReturn(true);
		when(versionCompareConfigurations.getServiceBaseUrl()).thenReturn(configurationProperty);
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.0", "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdEmpty() {
		VersionCompareContext context = VersionCompareContext.create("", "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdEmpty() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdNull() {
		VersionCompareContext context = VersionCompareContext.create(null, "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdNull() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", null, auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_identifiersMatch() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_nullAuthenticationHeaders() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.2", null);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_emptyAuthenticationHeaders() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id-v1.2", "");
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdNotVersion() {
		VersionCompareContext context = VersionCompareContext.create("instance-id", "instance-id-v1.1", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdNotVersion() {
		VersionCompareContext context = VersionCompareContext.create("instance-id-v1.1", "instance-id", auth);
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_firstIdDoesNotMatchOriginal() {
		VersionCompareContext context = VersionCompareContext
				.create("bad-id-v1.0", "instance-id-v1.1", auth)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = IllegalArgumentException.class)
	public void compareVersionsContent_secondIdDoesNotMatchOriginal() {
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "bad-id-v1.3", auth)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = VersionCompareException.class)
	public void compareVersionsContent_externalServiceCalled_connectionRefused() {
		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/connection-refused"));
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", auth)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test(expected = VersionCompareException.class)
	public void compareVersionsContent_externalServiceCalled_badRequestResponse() {
		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/bad-request"));
		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", auth)
				.setOriginalInstanceId("instance-id");
		service.compareVersionsContent(context);
	}

	@Test
	public void compareVersionsContent_externalServiceCalled_okSuccessful() throws IOException {
		when(versionCompareConfigurationProperty.get()).thenReturn(buildURI(server, "/post/ok/successful"));

		VersionCompareContext context = VersionCompareContext
				.create("instance-id-v1.1", "instance-id-v1.3", auth)
				.setOriginalInstanceId("instance-id");
		File result = service.compareVersionsContent(context);

		Assert.assertNotNull(result);
		Assert.assertEquals(FileUtils.readFileToString(result), "test file content\n");
	}

}
