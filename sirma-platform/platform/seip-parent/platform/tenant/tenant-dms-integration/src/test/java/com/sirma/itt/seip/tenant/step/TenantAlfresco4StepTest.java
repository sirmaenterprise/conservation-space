package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.InitConfiguration;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.threads.ThreadSleeper;

/**
 * Test the alfresco 4 step.
 *
 * @author nvelkov
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantAlfresco4StepTest {

	@Mock
	private RESTClient restClient;
	@Mock
	private InitAlfresco4Controller initController;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private AdaptersConfiguration adaptersConfiguration;
	@Mock
	private RawConfigurationAccessor rawConfigurationAccessor;
	@Mock
	protected SubsystemTenantAddressProvider addressProvider;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private TempFileProvider tempFileProvider;
	@Mock
	private ConfigurationProperty<Boolean> deleteTenantOnRollback;
	@Mock
	private ThreadSleeper threadSleeper;

	@InjectMocks
	private TenantAlfresco4Step step;

	@Before
	public void init() throws InterruptedException {
		mockConfigurations();
		TypeConverter jsonArrayToListConverter = mock(TypeConverter.class);
		when(jsonArrayToListConverter.convert(any(), any(JSONArray.class)))
				.thenReturn(Arrays.asList(JsonUtil.createObjectFromString("{'nodeRef':'childId'}")));
		TypeConverterUtil.setTypeConverter(jsonArrayToListConverter);
		doNothing().when(threadSleeper).sleepFor(anyLong());
	}

	@Test
	public void should_executeTenantCreationRequest() throws Exception {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenantId"));
		context.setIdpProvider(SecurityConfiguration.WSO_IDP);

		when(addressProvider.provideAddressForNewTenant()).thenReturn(URI.create(""));

		InputStream inputStream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
		HttpMethod httpMethod = mock(HttpMethod.class);
		when(httpMethod.getResponseBodyAsStream()).thenReturn(inputStream);
		when(restClient.rawRequest(any(HttpMethod.class), any(org.apache.commons.httpclient.URI.class)))
				.thenReturn(httpMethod);

		boolean result = step.execute(data, context);

		verify(restClient).rawRequest(any(HttpMethod.class), any(org.apache.commons.httpclient.URI.class));
		assertTrue(result);
	}

	@Test
	public void should_SkipExecute_When_IdpNotWsoIdp() throws Exception {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenantId"));
		context.setIdpProvider(SecurityConfiguration.KEYCLOAK_IDP);

		when(addressProvider.provideAddressForNewTenant()).thenReturn(URI.create(""));

		mockHttpRequest();

		step.execute(data, context);

		verify(initController, never()).initialize(any());
		verify(configurationManagement, atLeastOnce()).addConfigurations(anyCollection());
	}

	private void mockHttpRequest() throws IOException, DMSClientException {
		InputStream inputStream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
		HttpMethod httpMethod = mock(HttpMethod.class);
		when(httpMethod.getResponseBodyAsStream()).thenReturn(inputStream);
		when(restClient.rawRequest(Matchers.any(HttpMethod.class), any(org.apache.commons.httpclient.URI.class)))
				.thenReturn(httpMethod);
	}

	@Test
	public void execute_shouldRetry_when_alfrescoInitFails() throws Exception {
		doThrow(new DMSClientException("", 500)).doNothing().when(initController).initialize(any(InitConfiguration.class));
		mockAndExecuteStep();
		verify(initController, times(2)).initialize(any(InitConfiguration.class));
	}

	private void mockAndExecuteStep() throws IOException, DMSClientException, JSONException {
		when(addressProvider.provideAddressForNewTenant()).thenReturn(URI.create(""));
		mockHttpRequest();

		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenantId"));
		context.setIdpProvider(SecurityConfiguration.WSO_IDP);
		step.execute(data, context);
	}

	@Test
	public void execute_shouldRetryTwoTimesBeforeFail_when_alfrescoInitFails() throws Exception {
		try {
			doThrow(new DMSClientException("", 500)).when(initController).initialize(any(InitConfiguration.class));
			mockAndExecuteStep();
		} catch (Exception e) {
			assertTrue(e instanceof TenantCreationException);
		} finally {
			// one for the initial call and two retries
			verify(initController, times(3)).initialize(any(InitConfiguration.class));
		}
	}

	@Test
	public void should_clearAlfrescoSite_when_deletingTenant() throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));

		when(restClient.request(anyString(), any(HttpMethod.class)))
				.thenReturn("{'data':{'items':[{'nodeRef':'id'}]}}")
				.thenReturn("{'data':{'items':[{'nodeRef':'childId'}]}}");
		boolean result = step.delete(data, new TenantDeletionContext(new TenantInfo("tenantId"), false));
		verify(restClient, times(3)).request(Matchers.anyString(), any(HttpMethod.class));

		// Verify that the operation is executed in the tenant's context.
		verify(securityContextManager, times(2)).initializeTenantContext("tenantId");
		verify(securityContextManager, times(2)).endContextExecution();

		// verify that the configurations were removed
		verify(configurationManagement, atLeastOnce()).removeConfiguration(anyString());

		assertTrue(result);
	}

	@Test
	public void should_not_clearAlfrescoSite_when_notCompleted_onRollback() throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$prop$','value':'true'}]}"));
		step.delete(data, new TenantDeletionContext(new TenantInfo("tenantId"), true));
		verify(restClient, never()).request(anyString(), any(HttpMethod.class));
	}

	@Test
	public void should_clearAlfrescoSite_when_clearConfigurationSet_onRollback()
			throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));
		when(deleteTenantOnRollback.get()).thenReturn(Boolean.TRUE);
		when(restClient.request(anyString(), any(HttpMethod.class)))
				.thenReturn("{'data':{'items':[{'nodeRef':'id'}]}}");
		boolean result = step.delete(data, new TenantDeletionContext(new TenantInfo("tenantId"), true));
		verify(restClient, times(3)).request(Matchers.anyString(), any(HttpMethod.class));
		assertTrue(result);
	}

	@Test
	public void should_returnFalse_onException() throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));

		boolean result = step.delete(data, new TenantDeletionContext(new TenantInfo("tenantId"), false));
		// Going to throw an exception on first execution and won't proceed to the second.
		verify(restClient, times(1)).request(anyString(), any(HttpMethod.class));
		assertFalse(result);
	}

	private void mockConfigurations() {
		ConfigurationProperty<String> containerId = mockConfigurationProperty("containerId");
		when(adaptersConfiguration.getDmsContainerId()).thenReturn(containerId);
		when(adaptersConfiguration.getDmsHostConfiguration()).thenReturn("dmsHost");
		when(adaptersConfiguration.getDmsPortConfiguration()).thenReturn("25");
		when(adaptersConfiguration.getDmsProtocolConfiguration()).thenReturn("http");
		when(adaptersConfiguration.getAlfrescoStoreEnabled()).thenReturn(new ConfigurationPropertyMock<>());
		when(adaptersConfiguration.getAlfrescoViewStoreEnabled()).thenReturn(new ConfigurationPropertyMock<>());
		when(configurationManagement.addConfigurations(anyCollection()))
				.thenReturn(Arrays.asList(mock(Configuration.class)));
	}

	private static <T> ConfigurationProperty<T> mockConfigurationProperty(T value) {
		ConfigurationProperty<T> property = mock(ConfigurationProperty.class);
		when(property.get()).thenReturn(value);
		return property;
	}
}