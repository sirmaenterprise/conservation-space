package com.sirma.itt.seip.definition.rest;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

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
	private DictionaryService dictionaryService;

	@Mock
	private MultivaluedMap<String, String> pathParamsMap;

	@Mock
	private MultivaluedMap<String, String> queryParamsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Mock
	private InstanceContextInitializer contextInitializer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		service = new InstanceDefinitionModelRestService();
		MockitoAnnotations.initMocks(this);
		when(pathParamsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		when(uriInfo.getPathParameters()).thenReturn(pathParamsMap);
		when(uriInfo.getQueryParameters()).thenReturn(queryParamsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void getInstanceDefinitionModel_nullReference() {
		thrown.expect(ResourceException.class);
		thrown.expect(new TypeSafeMatcher<ResourceException>() {
			@Override
			public void describeTo(Description description) {
				//ignore
			}
			@Override
			protected boolean matchesSafely(ResourceException exception) {
				return exception.status.equals(NOT_FOUND);
			}
		});
		when(instanceTypeResolver.resolveReference("instanceId")).thenReturn(Optional.empty());
		service.getInstanceDefinitionModel("instanceId", "operation");
	}

	@Test(expected = ResourceException.class)
	public void getInstanceDefinitionModel_nullInstance() {
		when(instanceTypeResolver.resolveReference("instanceId"))
				.thenReturn(Optional.of(mock(InstanceReference.class)));
		service.getInstanceDefinitionModel("instanceId", "operation");
	}

	@Test
	public void getInstanceDefinitionModel_buildRequestObject() {
		InstanceReference reference = mock(InstanceReference.class);
		EmfInstance instance = new EmfInstance();
		when(reference.toInstance()).thenReturn(instance);
		when(instanceTypeResolver.resolveReference("instanceId")).thenReturn(Optional.of(reference));
		DefinitionModel model = mock(DefinitionModel.class);
		when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

		DefinitionModelObject modelObject = service.getInstanceDefinitionModel("instanceId", "operation");

		assertEquals(instance, modelObject.getInstance());
		assertEquals(model, modelObject.getDefinitionModel());
		assertEquals("operation", modelObject.getOperation());
	}

	@Test(expected = BadRequestException.class)
	public void getInstancesDefinitionModels_noIds() {
		when(queryParamsMap.get("id")).thenReturn(Collections.emptyList());
		service.getInstancesDefinitionModels(request);
	}

	@Test
	public void getInstancesDefinitionModels_buildMap() {
		List<String> ids = Arrays.asList("instanceId-1", "instanceId-2");
		when(queryParamsMap.get("id")).thenReturn(ids);
		when(queryParamsMap.get("operation")).thenReturn(Arrays.asList("bat-operation"));
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("instanceId-1");
		EmfInstance instance2 = new EmfInstance();
		instance2.setId("instanceId-2");

		when(instanceTypeResolver.resolveInstances(ids)).thenReturn(Arrays.asList(instance1, instance2));
		DefinitionModel instance1Model = mock(DefinitionModel.class);
		when(dictionaryService.getInstanceDefinition(instance1)).thenReturn(instance1Model);
		when(dictionaryService.getInstanceDefinition(instance2)).thenReturn(null);

		Map<String, DefinitionModelObject> result = service.getInstancesDefinitionModels(request);

		assertEquals(1, result.size());
		assertTrue(result.containsKey("instanceId-1"));

		DefinitionModelObject modelObject = result.get("instanceId-1");
		assertEquals(instance1, modelObject.getInstance());
		assertEquals(instance1Model, modelObject.getDefinitionModel());
		assertEquals("bat-operation", modelObject.getOperation());
	}

	@Test
	public void getInstancesDefinitionModelsBatch_buildMap() {
		List<String> ids = Arrays.asList("instanceId-1", "instanceId-2");
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("instanceId-1");
		EmfInstance instance2 = new EmfInstance();
		instance2.setId("instanceId-2");

		when(instanceTypeResolver.resolveInstances(ids)).thenReturn(Arrays.asList(instance1, instance2));
		DefinitionModel instance1Model = mock(DefinitionModel.class);
		when(dictionaryService.getInstanceDefinition(instance1)).thenReturn(instance1Model);
		when(dictionaryService.getInstanceDefinition(instance2)).thenReturn(null);

		Map<String, DefinitionModelObject> result = service.getInstancesDefinitionModelsBatch(ids, "bat-operation");

		assertEquals(1, result.size());
		assertTrue(result.containsKey("instanceId-1"));

		DefinitionModelObject modelObject = result.get("instanceId-1");
		assertEquals(instance1, modelObject.getInstance());
		assertEquals(instance1Model, modelObject.getDefinitionModel());
		assertEquals("bat-operation", modelObject.getOperation());
	}

}
