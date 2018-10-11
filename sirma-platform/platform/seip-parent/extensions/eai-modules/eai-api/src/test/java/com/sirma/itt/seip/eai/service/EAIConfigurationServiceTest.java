package com.sirma.itt.seip.eai.service;

import static com.sirma.itt.seip.eai.mock.MockProvider.mockSystem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Tests {@link EAIConfigurationService}
 * 
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIConfigurationServiceTest {
	@Mock
	private Plugins<EAIConfigurationProvider> configurationProviders;
	@InjectMocks
	private EAIConfigurationService eaiConfigurationService;

	@Before
	public void setupMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetRegisteredSystems() throws Exception {
		EAIConfigurationProvider mocked = mockSystem("TEST", Boolean.TRUE, Boolean.TRUE);
		when(configurationProviders.stream()).thenReturn(Collections.singletonList(mocked).stream());
		Set<String> registeredSystems = eaiConfigurationService.getAllRegisteredSystems();
		Assert.assertEquals(registeredSystems.size(), 1);
		Assert.assertTrue(registeredSystems.contains("TEST"));
	}

	@Test
	public void testGetIntegrationConfigurationValid() throws Exception {
		EAIConfigurationProvider mocked = mockSystem("TEST", Boolean.TRUE, Boolean.TRUE);
		when(configurationProviders.stream()).thenReturn(Stream.of(mocked), Stream.of(mocked));
		EAIConfigurationProvider integrationConfiguration = eaiConfigurationService.getIntegrationConfiguration("test");
		assertEquals(integrationConfiguration, mocked);
		integrationConfiguration = eaiConfigurationService.getIntegrationConfiguration("TEst");
		assertEquals(integrationConfiguration, mocked);
	}

	@Test(expected = EAIRuntimeException.class)
	public void testGetIntegrationConfigurationInvalid() throws Exception {
		when(configurationProviders.stream()).thenReturn(Stream.empty());
		eaiConfigurationService.getIntegrationConfiguration("test");
	}

	@Test
	public void testGetAllRegisteredSystemsOrdered() throws Exception {
		EAIConfigurationProvider system1 = mockSystem("TEST1", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system2 = mockSystem("TEST2", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system3 = mockSystem("TEST3", Boolean.FALSE, Boolean.TRUE);
		EAIConfigurationProvider system4 = mockSystem("TEST4", Boolean.TRUE, Boolean.FALSE);
		EAIConfigurationProvider system5 = mockSystem("TEST5", Boolean.TRUE, Boolean.TRUE);

		List<EAIConfigurationProvider> systems = Arrays.asList(system1, system2, system3, system5, system4);
		when(configurationProviders.stream()).thenReturn(systems.stream());
		Set<String> registeredSystems = eaiConfigurationService.getAllRegisteredSystems();
		assertEquals(4, registeredSystems.size());
		Iterator<String> iterator = registeredSystems.iterator();
		assertEquals("TEST1", iterator.next());
		assertEquals("TEST2", iterator.next());
		assertEquals("TEST5", iterator.next());
		assertEquals("TEST4", iterator.next());
	}

	@Test
	public void testResolveIntegrationConfiguration() throws Exception {
		EAIConfigurationProvider system1 = mockSystem("TEST1", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system2 = mockSystem("TEST2", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system3 = mockSystem("TEST3", Boolean.FALSE, Boolean.TRUE);
		EAIConfigurationProvider system4 = mockSystem("TEST4", Boolean.TRUE, Boolean.FALSE);
		EAIConfigurationProvider system5 = mockSystem("TEST5", Boolean.TRUE, Boolean.TRUE);

		List<EAIConfigurationProvider> systems = Arrays.asList(system1, system5, system4, system3, system2);
		when(configurationProviders.stream()).then(a -> systems.stream());
		Optional<EAIConfigurationProvider> test3 = eaiConfigurationService
				.resolveIntegrationConfiguration("TEST3");
		assertTrue(test3.isPresent());
		assertFalse(test3.get().isEnabled().get());

		Optional<EAIConfigurationProvider> test4 = eaiConfigurationService
				.resolveIntegrationConfiguration("TEST4");
		assertTrue(test4.isPresent());
		assertTrue(test4.get().isEnabled().get());
	}

	@Test
	public void testGetFilteredSystemsOrdered() throws Exception {
		EAIConfigurationProvider system1 = mockSystem("TEST1", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system2 = mockSystem("TEST2", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system3 = mockSystem("TEST3", Boolean.FALSE, Boolean.TRUE);
		EAIConfigurationProvider system4 = mockSystem("TEST4", Boolean.TRUE, Boolean.FALSE);
		EAIConfigurationProvider system5 = mockSystem("TEST5", Boolean.TRUE, Boolean.TRUE);

		List<EAIConfigurationProvider> systems = Arrays.asList(system1, system5, system4, system3, system2);
		when(configurationProviders.stream()).thenReturn(systems.stream());
		Set<String> registeredSystems = eaiConfigurationService
				.getRegisteredSystems(EAIConfigurationProvider::isUserService);
		assertEquals(3, registeredSystems.size());
		Iterator<String> iterator = registeredSystems.iterator();
		assertEquals("TEST1", iterator.next());
		assertEquals("TEST5", iterator.next());
		assertEquals("TEST2", iterator.next());
	}

	@Test
	public void testHasRegisteredSystem() throws Exception {
		EAIConfigurationProvider system1 = mockSystem("TEST1", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system2 = mockSystem("TEST2", Boolean.TRUE, Boolean.TRUE);
		EAIConfigurationProvider system3 = mockSystem("TEST3", Boolean.FALSE, Boolean.TRUE);
		EAIConfigurationProvider system4 = mockSystem("TEST4", Boolean.TRUE, Boolean.FALSE);
		EAIConfigurationProvider system5 = mockSystem("TEST5", Boolean.TRUE, Boolean.TRUE);

		List<EAIConfigurationProvider> systems = Arrays.asList(system1, system5, system4, system3, system2);
		when(configurationProviders.stream()).thenReturn(systems.stream(), systems.stream(), systems.stream());
		assertTrue(eaiConfigurationService.hasRegisteredSystem("TEST1"));
		assertFalse(eaiConfigurationService.hasRegisteredSystem("TEST3"));
		assertFalse(eaiConfigurationService.hasRegisteredSystem("TEST6"));
	}

	@Test
	public void testGetIntegrationConfigurationWithNullParam() {
		String systemId = null;
		assertFalse(eaiConfigurationService.findIntegrationConfiguration(systemId).isPresent());
	}

	@Test
	public void testGetIntegrationConfiguration() {
		String systemId = "cms";
		EAIConfigurationProvider mocked = mockSystem(systemId, Boolean.TRUE, Boolean.TRUE);
		when(configurationProviders.stream()).thenReturn(Stream.of(mocked));
		assertEquals(mocked, eaiConfigurationService.getIntegrationConfiguration(systemId));
	}
}
