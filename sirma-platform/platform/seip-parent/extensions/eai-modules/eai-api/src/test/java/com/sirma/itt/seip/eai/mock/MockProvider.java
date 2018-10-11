package com.sirma.itt.seip.eai.mock;

import java.util.Collections;

import org.mockito.Mockito;

import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.model.mapper.RawQueryMapperExtension;
import com.sirma.itt.seip.rest.mapper.MapperProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The reusable mock provider for eai code.
 *
 * @author bbanchev
 */
public class MockProvider {

	/**
	 * Gets the mapper provider.
	 *
	 * @return the mapper provider
	 */
	public static MapperProvider getMapperProvider() {
		MapperProvider mapperProvider = new MapperProvider();
		ReflectionUtils.setFieldValue(mapperProvider, "extensions",
				Collections.singletonList(new RawQueryMapperExtension()));
		mapperProvider.initialize();
		return mapperProvider;
	}

	/**
	 * Mock {@link EAIConfigurationProvider} as new system.
	 *
	 * @param name
	 *            the config name
	 * @param enabled
	 *            - is enabled
	 * @param user
	 *            - is user service
	 * @return the EAI configuration provider
	 */
	public static EAIConfigurationProvider mockSystem(String name, Boolean enabled, Boolean user) {
		EAIConfigurationProvider system = Mockito.mock(EAIConfigurationProvider.class);
		Mockito.when(system.getName()).thenReturn(name);
		Mockito.when(system.isEnabled()).thenReturn(new ConfigurationPropertyMock<>(enabled));
		Mockito.when(system.isUserService()).thenReturn(user);
		return system;
	}

}
