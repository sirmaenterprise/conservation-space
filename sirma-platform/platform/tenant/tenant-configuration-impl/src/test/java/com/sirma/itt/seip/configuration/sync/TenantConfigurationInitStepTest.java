package com.sirma.itt.seip.configuration.sync;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Test for {@link TenantConfigurationInitStep}
 *
 * @author BBonev
 */
public class TenantConfigurationInitStepTest {

	@InjectMocks
	private TenantConfigurationInitStep configurationInitStep;

	@Mock
	private RawConfigurationAccessor configurationAccessor;
	@Spy
	private SecurityContextManager contextManager = new SecurityContextManagerFake();
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private ConfigurationInstanceProvider configurationInstanceProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(configurationManagement.addConfigurations(anyCollection()))
				.then(a -> a.getArgumentAt(0, Collection.class));

		when(configurationInstanceProvider.getRegisteredConfigurations())
				.thenReturn(new HashSet<>(Arrays.asList("config1", "config2", "config3", "config4", "config5")));
	}

	@Test
	public void testExecute() throws Exception {
		TenantStepData stepData = new TenantStepData(null, readDataModel());
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));

		configurationInitStep.execute(stepData, context);

		verify(configurationManagement).addConfigurations(
				argThat(CustomMatcher.of((Collection c) -> c.size() == 3, "Should have collection of 3 elements")));
		Map<String, Serializable> properties = stepData.getProperties();
		assertTrue(properties.containsKey("$added$"));
	}

	@Test
	public void testRollback() throws Exception {
		TenantStepData stepData = new TenantStepData(null, readDataModel());
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));

		configurationInitStep.rollback(stepData, context);
		// should do nothing
		verify(configurationManagement, never()).removeConfiguration(anyString());

		configurationInitStep.execute(stepData, context);

		configurationInitStep.rollback(stepData, context);

		verify(configurationManagement, times(3)).removeConfiguration(anyString());
	}

	@Test
	public void testProvide() throws Exception {
		ConfigurationInstance config1 = mock(ConfigurationInstance.class);
		when(config1.getName()).thenReturn("config1");
		when(config1.getType()).then(a -> Integer.class);
		when(config1.getLabel()).thenReturn("Config 1 label");

		when(configurationAccessor.getRawConfigurationValue("config1")).thenReturn("1");
		ConfigurationInstance config2 = mock(ConfigurationInstance.class);
		when(config2.getName()).thenReturn("config2");
		when(config2.getType()).then(a -> Boolean.class);
		when(config2.getLabel()).thenReturn("Config 2 label");
		when(configurationAccessor.getRawConfigurationValue("config2")).thenReturn("true");
		ConfigurationInstance config3 = mock(ConfigurationInstance.class);
		when(config3.getName()).thenReturn("config3");
		when(config3.getType()).then(a -> String.class);
		when(config3.getLabel()).thenReturn("Config 3 label");
		when(configurationAccessor.getRawConfigurationValue("config3")).thenReturn("testValue");
		when(configurationInstanceProvider.getAllInstances()).thenReturn(Arrays.asList(config1, config2, config3));

		TenantStepData stepData = configurationInitStep.provide();
		assertNotNull(stepData);
	}

	private JSONObject readDataModel() {
		return JsonUtil
				.createObjectFromString(ResourceLoadUtil.loadResource(getClass(), "TenantConfigurationInitStep.json"));
	}
}
