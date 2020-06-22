package com.sirma.itt.seip.eai.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirma.itt.seip.rest.resources.instances.InstancesLoadResponse;

public class EAIRestServiceTest {

	@InjectMocks
	private EAIRestService eaiRestService;

	@Mock
	private IntegrateExternalObjectsService integrationService;

	@Before
	public void init() {
		eaiRestService = new EAIRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_ImportObjectsAnd() {
		List<Instance> importedInstances = new ArrayList<>();
		importedInstances.add(new EmfInstance());

		when(integrationService.importInstances(anyList())).thenReturn(importedInstances);

		InstancesLoadResponse result = eaiRestService.importObjects(importedInstances);

		assertEquals(importedInstances, result.getInstances());
	}

}
