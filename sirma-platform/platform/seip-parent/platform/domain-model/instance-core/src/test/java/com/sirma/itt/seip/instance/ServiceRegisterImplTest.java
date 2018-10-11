package com.sirma.itt.seip.instance;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.instance.dao.InstanceToServiceRegistryExtension;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;

/**
 * Test class for {@link ServiceRegisterImpl}
 *
 * @author BBonev
 */
public class ServiceRegisterImplTest {

	@InjectMocks
	private ServiceRegisterImpl serviceRegister;

	@Mock
	private InstanceToServiceRegistryExtension extension;
	@Spy
	private List<InstanceToServiceRegistryExtension> extensions = new ArrayList<>();
	@Mock
	private TypeMappingProvider typeProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		extensions.clear();
		extensions.add(extension);

		when(extension.getSupportedObjects()).thenReturn(Arrays.asList(Object.class));

		serviceRegister.initializeExtensionMapping();
	}

	@Test
	public void getEventProvider() throws Exception {
		assertNotNull(serviceRegister.getEventProvider(null));

		when(extension.getEventProvider()).thenReturn(mock(InstanceEventProvider.class));
		assertNotNull(serviceRegister.getEventProvider(Object.class));
	}

}
