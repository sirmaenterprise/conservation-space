package com.sirma.itt.seip.instance.actions.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link RemoveRelationAction}.
 *
 * @author BBonev
 */
public class RemoveRelationActionTest {

	@InjectMocks
	private RemoveRelationAction action;

	@Mock
	private DomainInstanceService instanceService;
	@Mock
	private DefinitionService definitionService;

	private Instance instance;
	private RemoveRelationRequest request;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		request = new RemoveRelationRequest();
		request.setTargetId("targetId");
		instance = new EmfInstance(request.getTargetId());
		when(instanceService.loadInstance(any())).thenReturn(instance);

		when(instanceService.save(any(InstanceSaveContext.class)))
		.then(a -> a.getArgumentAt(0, InstanceSaveContext.class).getInstance());

		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setName("withInverse");
		propertyDefinition.setUri("emf:withInverse");
		definition.getFields().add(propertyDefinition);

		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);
	}

	@Test
	public void getName() {
		assertEquals(RemoveRelationRequest.OPERATION_NAME, action.getName());
	}

	@Test
	public void shouldRemovePropertyIfAllValuesAreRemoved() throws Exception {
		instance.add("withInverse", new HashSet<>(Collections.singletonList("emf:destination")));

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance resultInstance = (Instance) result;
		assertFalse(resultInstance.isPropertyPresent("withInverse"));

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveOnlyRequestedValue() throws Exception {
		instance.add("withInverse",
				new HashSet<>(Arrays.asList("emf:destination", "otherValue")));

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance resultInstance = (Instance) result;
		assertTrue(resultInstance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", resultInstance.getAsCollection("withInverse", HashSet::new).iterator().next());

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveSingleValue() throws Exception {
		instance.add("withInverse", "emf:destination");

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance resultInstance = (Instance) result;
		assertFalse(resultInstance.isPropertyPresent("withInverse"));

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldNotRemoveSingleNonMatchingValue() throws Exception {
		instance.add("withInverse", "otherValue");

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance resultInstance = (Instance) result;
		assertTrue(resultInstance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", resultInstance.getAsCollection("withInverse", HashSet::new).iterator().next());
	}

	@Test
	public void shouldNotRemoveNonMatchingValue() throws Exception {
		instance.add("withInverse", new HashSet<>(Collections.singletonList("otherValue")));

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance resultInstance = (Instance) result;
		assertTrue(resultInstance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", resultInstance.getAsCollection("withInverse", HashSet::new).iterator().next());
	}

	@Test(expected = BadRequestException.class)
	public void removeRelation_nullRequest() throws Exception {
		action.validate(null);
	}

	@Test(expected = BadRequestException.class)
	public void removeRelation_noRelations() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");

		action.perform(request);
	}
}
