package com.sirma.itt.seip.instance.actions.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link AddRelationAction}
 *
 * @author BBonev
 */
public class AddRelationActionTest {

	@InjectMocks
	private AddRelationAction action;

	@Mock
	private InstanceTypeResolver resolver;
	@Mock
	private DomainInstanceService instanceService;
	@Mock
	private DictionaryService dictionaryService;

	private PropertyDefinitionMock propertyDefinition;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(resolver.resolveReference(any()))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));

		when(instanceService.save(any(InstanceSaveContext.class)))
				.then(a -> a.getArgumentAt(0, InstanceSaveContext.class).getInstance());

		DefinitionMock definition = new DefinitionMock();
		propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setName("withInverse");
		propertyDefinition.setUri("emf:withInverse");
		definition.getFields().add(propertyDefinition);

		when(dictionaryService.getInstanceDefinition(any())).thenReturn(definition);
	}

	@Test
	public void getName() {
		assertEquals(AddRelationRequest.OPERATION_NAME, action.getName());
	}

	@Test(expected = BadRequestException.class)
	public void addRelation_withoutResolvedSource() {
		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("targetId");
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));
		when(resolver.resolveReference("targetId")).thenReturn(Optional.empty());
		action.perform(request);
	}

	@Test
	public void shouldAddUndefinedRelationAsIs() {
		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.setRelations(
				Collections.singletonMap("unknownRelation", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);

		Instance instance = (Instance) result;
		assertTrue(instance.get("unknownRelation") instanceof Collection);
	}

	@Test
	public void addRelation() {
		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveOldValueEventWhenNotConfiguredForOnNonMultivalueField() {

		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("oldValue")));
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			return "emf:destination".equals(context.getInstance().get("withInverse"));
		})));
	}

	@Test
	public void shouldNotRemoveOldValuesForNonMultivalueField() {

		propertyDefinition.setMultiValued(Boolean.TRUE);

		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("oldValue")));
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			return context.getInstance().getAsCollection("withInverse", LinkedList::new).size() == 2;
		})));
	}

	@Test
	public void shouldNotFlatNonMultivalueFields() {

		propertyDefinition.setMultiValued(Boolean.TRUE);

		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.setRemoveExisting(true);
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("oldValue")));
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			Serializable value = context.getInstance().get("withInverse");
			assertNotNull(value);
			assertTrue(value instanceof Collection);
			assertEquals(1, ((Collection<?>) value).size());
			assertEquals("emf:destination", ((Collection<?>) value).iterator().next());
		})));
	}

	@Test
	public void shouldNotModifyMuvalueField() {

		propertyDefinition.setMultiValued(Boolean.TRUE);

		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("oldValue")));
		request.setRelations(Collections.singletonMap("withInverse",
				new HashSet<>(Arrays.asList("emf:destination1", "emf:destination2"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			return context.getInstance().getAsCollection("withInverse", LinkedList::new).size() == 3;
		})));
	}

	@Test
	public void shouldUpdateFieldWhenConfiguredWithUri() {

		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		request.setTargetReference(InstanceReferenceMock.createGeneric("emf:source"));
		request.getTargetReference().toInstance().add("withInverse", new HashSet<>(Arrays.asList("oldValue")));
		request.setRemoveExisting(true);
		request.setRelations(
				Collections.singletonMap("emf:withInverse", new HashSet<>(Arrays.asList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			Instance instance = context.getInstance();
			assertTrue(instance.get("withInverse") instanceof String);
			assertNull(instance.get("emf:withInverse"));
		})));
	}

	@Test
	public void addRelation_removeExistingTrue() {
		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("emf:source");
		List<String> relations = Arrays.asList("emf:destination");
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(relations)));
		request.setRemoveExisting(true);

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test(expected = BadRequestException.class)
	public void addRelation_nullRequest() {
		action.perform(null);
	}

	@Test(expected = BadRequestException.class)
	public void addRelation_exceptionWhileLoadingSorce() {
		AddRelationRequest request = new AddRelationRequest();
		request.setTargetId("targetId");
		when(resolver.resolveReference("targetId")).thenReturn(Optional.empty());
		action.perform(request);
	}

}
