package com.sirma.itt.seip.definition.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Test for {@link InstanceDefinitionModelRestService}.
 *
 * @author A. Kunchev
 */
public class InstanceDefinitionModelRestServiceTest {

	@InjectMocks
	private InstanceDefinitionModelRestService service;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		service = new InstanceDefinitionModelRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void getInstanceDefinitionModel_nullReference() {
		when(instanceTypeResolver.resolveReference("instanceId")).thenReturn(Optional.empty());
		service.getInstanceDefinitionModel("instanceId", "operation", emptyList());
	}

	@Test(expected = InstanceNotFoundException.class)
	public void getInstanceDefinitionModel_nullInstance() {
		when(instanceTypeResolver.resolveReference("instanceId"))
				.thenReturn(Optional.of(mock(InstanceReference.class)));
		service.getInstanceDefinitionModel("instanceId", "operation", emptyList());
	}

	@Test
	public void getInstanceDefinitionModel_buildRequestObject() {
		InstanceReference reference = mock(InstanceReference.class);
		EmfInstance instance = new EmfInstance();
		when(reference.toInstance()).thenReturn(instance);
		when(instanceTypeResolver.resolveReference("instanceId")).thenReturn(Optional.of(reference));
		DefinitionModel model = mock(DefinitionModel.class);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		DefinitionModelObject modelObject = service.getInstanceDefinitionModel("instanceId", "operation", emptyList());

		assertEquals(instance, modelObject.getInstance());
		assertEquals(model, modelObject.getDefinitionModel());
		assertEquals("operation", modelObject.getOperation());
	}

	@Test(expected = BadRequestException.class)
	public void getInstancesDefinitionModels_noIds() {
		service.getInstancesDefinitionModels(emptyList(), emptyList(), "");
	}

	@Test
	public void getInstancesDefinitionModels_buildMap() {
		List<String> ids = Arrays.asList("instanceId-1", "instanceId-2");
		Instance instance1 = new EmfInstance();
		instance1.setId("instanceId-1");
		Instance instance2 = new EmfInstance();
		instance2.setId("instanceId-2");

		when(instanceTypeResolver.resolveInstances(ids)).thenReturn(Arrays.asList(instance1, instance2));
		DefinitionModel instance1Model = mock(DefinitionModel.class);
		when(definitionService.getInstanceDefinition(instance1)).thenReturn(instance1Model);
		when(definitionService.getInstanceDefinition(instance2)).thenReturn(null);

		Map<String, DefinitionModelObject> result = service.getInstancesDefinitionModels(ids,
				Arrays.asList("property-1", "property-2"), "bat-operation");

		assertEquals(1, result.size());
		assertTrue(result.containsKey("instanceId-1"));

		DefinitionModelObject modelObject = result.get("instanceId-1");
		assertEquals(instance1, modelObject.getInstance());
		assertEquals(instance1Model, modelObject.getDefinitionModel());
		assertEquals("bat-operation", modelObject.getOperation());
		assertEquals(2, modelObject.getRequestedFields().size());
	}

	@Test
	public void getInstancesDefinitionModelsBatch_buildMap() {
		List<String> ids = Arrays.asList("instanceId-1", "instanceId-2");
		Instance instance1 = new EmfInstance();
		instance1.setId("instanceId-1");
		Instance instance2 = new EmfInstance();
		instance2.setId("instanceId-2");

		when(instanceTypeResolver.resolveInstances(ids)).thenReturn(Arrays.asList(instance1, instance2));
		DefinitionModel instance1Model = mock(DefinitionModel.class);
		when(definitionService.getInstanceDefinition(instance1)).thenReturn(instance1Model);
		when(definitionService.getInstanceDefinition(instance2)).thenReturn(null);

		LoadModelsRequest loadRequest = new LoadModelsRequest().setInstanceIds(ids).setOperation("bat-operation");
		Map<String, DefinitionModelObject> result = service.getInstancesDefinitionModelsBatch(loadRequest);

		assertEquals(1, result.size());
		assertTrue(result.containsKey("instanceId-1"));

		DefinitionModelObject modelObject = result.get("instanceId-1");
		assertEquals(instance1, modelObject.getInstance());
		assertEquals(instance1Model, modelObject.getDefinitionModel());
		assertEquals("bat-operation", modelObject.getOperation());
	}
}