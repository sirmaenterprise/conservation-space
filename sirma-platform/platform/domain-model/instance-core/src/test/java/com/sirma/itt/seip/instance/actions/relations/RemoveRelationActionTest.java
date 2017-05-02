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
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link RemoveRelationAction}
 *
 * @author BBonev
 */
public class RemoveRelationActionTest {

	@InjectMocks
	private RemoveRelationAction action;

	@Mock
	private InstanceTypeResolver resolver;
	@Mock
	private DomainInstanceService instanceService;
	@Mock
	private DictionaryService dictionaryService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(resolver.resolveReference(any()))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));

		when(instanceService.save(any(InstanceSaveContext.class)))
				.then(a -> a.getArgumentAt(0, InstanceSaveContext.class).getInstance());

		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setName("withInverse");
		propertyDefinition.setUri("emf:withInverse");
		definition.getFields().add(propertyDefinition);

		when(dictionaryService.getInstanceDefinition(any())).thenReturn(definition);
	}

	@Test
	public void getName() {
		assertEquals(RemoveRelationRequest.OPERATION_NAME, action.getName());
	}

	@Test(expected = BadRequestException.class)
	public void removeRelation_withoutSource() {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("targetId");
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));
		when(resolver.resolveReference("targetId")).thenReturn(Optional.empty());
		action.perform(request);
	}

	@Test
	public void shouldRemovePropertyIfAllValuesAreRemoved() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("emf:destination")));

		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance instance = (Instance) result;
		assertFalse(instance.isPropertyPresent("withInverse"));

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveOnlyRequestedValue() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse",
				new HashSet<>(Arrays.asList("emf:destination", "otherValue")));

		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance instance = (Instance) result;
		assertTrue(instance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", instance.getAsCollection("withInverse", HashSet::new).iterator().next());

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveSingleValue() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", "emf:destination");

		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance instance = (Instance) result;
		assertFalse(instance.isPropertyPresent("withInverse"));

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldNotRemoveSingleNonMatchingValue() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", "otherValue");

		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance instance = (Instance) result;
		assertTrue(instance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", instance.getAsCollection("withInverse", HashSet::new).iterator().next());
	}

	@Test
	public void shouldNotRemoveNonMatchingValue() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("otherValue")));

		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		Instance instance = (Instance) result;
		assertTrue(instance.isPropertyPresent("withInverse"));
		assertEquals("otherValue", instance.getAsCollection("withInverse", HashSet::new).iterator().next());
	}

	@Test(expected = BadRequestException.class)
	public void removeRelation_nullRequest() throws Exception {
		action.perform(null);
	}

	@Test(expected = BadRequestException.class)
	public void removeRelation_noRelations() throws Exception {
		RemoveRelationRequest request = new RemoveRelationRequest();
		request.setTargetId("emf:source");

		action.perform(request);
	}
}
