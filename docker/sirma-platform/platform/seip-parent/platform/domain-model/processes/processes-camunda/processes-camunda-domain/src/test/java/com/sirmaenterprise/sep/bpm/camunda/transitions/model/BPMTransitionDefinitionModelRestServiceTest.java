package com.sirmaenterprise.sep.bpm.camunda.transitions.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModelTest;

/**
 * Test for {@link BPMTransitionDefinitionModelRestService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMTransitionDefinitionModelRestServiceTest {

	@InjectMocks
	private BPMTransitionDefinitionModelRestService bPMTransitionDefinitionModelRestService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private TransitionModelService transitionModelService;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Mock
	private MultivaluedMap<String, String> queryParamsMap;

	@Mock
	private Instance instance;

	private String id;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		id = "emf:Id";
		when(uriInfo.getQueryParameters()).thenReturn(queryParamsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
		when(instance.get(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getId()).thenReturn(id);
	}

	@Test
	public void testGetInstanceDefinitionModelForMultipleActivities() throws Exception {
		when(queryParamsMap.get("operation")).thenReturn(Arrays.asList("id2"));
		List<Instance> instanceList = Arrays.asList(instance);
		when(instanceTypeResolver.resolveInstances(eq(Collections.singletonList(id)))).thenReturn(instanceList);

		Instance newActivity1 = mock(Instance.class);
		Instance newActivity2 = mock(Instance.class);
		when(newActivity1.getId()).thenReturn("emf:activity1");
		when(newActivity2.getId()).thenReturn("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("activity1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("activity2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(definitionService.getInstanceDefinition(eq(newActivity1))).thenReturn(model1);
		when(definitionService.getInstanceDefinition(eq(newActivity2))).thenReturn(model2);
		when(definitionService.getInstanceDefinition(eq(instance))).thenReturn(instanceModel);
	}

	@Test
	public void testGetInstanceDefinitionModelOnlyForCurrentInstance() throws Exception {
		when(queryParamsMap.get("operation")).thenReturn(Arrays.asList("id1"));

		List<Instance> instanceList = Arrays.asList(instance);
		when(instanceTypeResolver.resolveInstances(eq(Collections.singletonList(id)))).thenReturn(instanceList);

		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(definitionService.getInstanceDefinition(eq(instance))).thenReturn(instanceModel);
		when(transitionModelService.generateTransitionActivities(eq(instance), eq("id1")))
				.thenReturn(Collections.singletonList(instance));
		Map<String, BPMDefinitionModelObject> result = bPMTransitionDefinitionModelRestService
				.getInstanceDefinitionModelBPM(id, request);

		assertEquals(1, result.size());
		assertTrue(result.containsKey(id));
		assertEquals(instanceModel, result.get(id).getModel().getDefinitionModel());
	}
}
