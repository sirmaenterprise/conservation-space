package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller;
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
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test the alfresco 4 step.
 *
 * @author nvelkov
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

	@InjectMocks
	private TenantAlfresco4Step step;

	@Before
	public void init() {
		mockConfigurations();
		TypeConverter jsonArrayToListConverter = mock(TypeConverter.class);
		when(jsonArrayToListConverter.convert(any(), any(JSONArray.class)))
				.thenReturn(Arrays.asList(JsonUtil.createObjectFromString("{'nodeRef':'childId'}")));
		TypeConverterUtil.setTypeConverter(jsonArrayToListConverter);
	}

	@Test
	public void should_executeTenantCreationRequest() throws Exception {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenantId"));

		when(addressProvider.provideAddressForNewTenant()).thenReturn(URI.create(""));

		InputStream inputStream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
		HttpMethod httpMethod = mock(HttpMethod.class);
		when(httpMethod.getResponseBodyAsStream()).thenReturn(inputStream);
		when(restClient.rawRequest(Matchers.any(HttpMethod.class), any(org.apache.commons.httpclient.URI.class)))
				.thenReturn(httpMethod);

		boolean result = step.execute(data, context);

		verify(restClient).rawRequest(any(HttpMethod.class), any(org.apache.commons.httpclient.URI.class));
		assertTrue(result);
	}

	@Test
	public void should_clearAlfrescoSite_when_deletingTenant() throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));

		Mockito.when(restClient.request(Matchers.anyString(), Matchers.any(HttpMethod.class)))
				.thenReturn("{'data':{'items':[{'nodeRef':'id'}]}}")
				.thenReturn("{'data':{'items':[{'nodeRef':'childId'}]}}");
		boolean result = step.delete(data, new TenantInfo("tenantId"), false);
		verify(restClient, times(3)).request(Matchers.anyString(), Matchers.any(HttpMethod.class));

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
		step.delete(data, new TenantInfo("tenantId"), true);
		verify(restClient, never()).request(Matchers.anyString(), Matchers.any(HttpMethod.class));
	}

	@Test
	public void should_clearAlfrescoSite_when_clearConfigurationSet_onRollback()
			throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));
		Mockito.when(deleteTenantOnRollback.get()).thenReturn(Boolean.TRUE);
		Mockito.when(restClient.request(Matchers.anyString(), Matchers.any(HttpMethod.class)))
				.thenReturn("{'data':{'items':[{'nodeRef':'id'}]}}");
		boolean result = step.delete(data, new TenantInfo("tenantId"), true);
		verify(restClient, times(3)).request(Matchers.anyString(), Matchers.any(HttpMethod.class));
		assertTrue(result);
	}

	@Test
	public void should_returnFalse_onException() throws JSONException, DMSClientException {
		TenantStepData data = new TenantStepData("id",
				new JSONObject("{properties:[ {'id':'$success$','value':'true'}]}"));

		boolean result = step.delete(data, new TenantInfo("tenantId"), false);
		// Going to throw an exception on first execution and won't proceed to the second.
		verify(restClient, times(1)).request(Matchers.anyString(), Matchers.any(HttpMethod.class));
		assertFalse(result);
	}

	private void mockConfigurations() {
		ConfigurationProperty<String> containerId = mockConfigurationProperty("containerId");
		Mockito.when(adaptersConfiguration.getDmsContainerId()).thenReturn(containerId);
		Mockito.when(adaptersConfiguration.getDmsHostConfiguration()).thenReturn("dmsHost");
		Mockito.when(adaptersConfiguration.getDmsPortConfiguration()).thenReturn("25");
		Mockito.when(adaptersConfiguration.getDmsProtocolConfiguration()).thenReturn("http");
		Mockito.when(configurationManagement.addConfigurations(Matchers.anyCollection()))
				.thenReturn(Arrays.asList(Mockito.mock(Configuration.class)));
	}

	private static <T> ConfigurationProperty<T> mockConfigurationProperty(T value) {
		ConfigurationProperty<T> property = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(property.get()).thenReturn(value);
		return property;
	}
}
