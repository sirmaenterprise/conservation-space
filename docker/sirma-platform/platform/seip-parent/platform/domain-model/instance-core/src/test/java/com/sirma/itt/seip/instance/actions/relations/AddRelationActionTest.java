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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link AddRelationAction}.
 *
 * @author BBonev
 */
public class AddRelationActionTest {

	@InjectMocks
	private AddRelationAction action;

	@Mock
	private DomainInstanceService instanceService;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private LabelProvider labelProvider;

	private PropertyDefinitionMock propertyDefinition;
	private AddRelationRequest request;
	private Instance instance;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		request = new AddRelationRequest();
		request.setTargetId("targetId");
		request.setTargetReference(InstanceReferenceMock.createGeneric("targetId"));
		instance = request.getTargetReference().toInstance();

		when(instanceService.save(any(InstanceSaveContext.class)))
		.then(a -> a.getArgumentAt(0, InstanceSaveContext.class).getInstance());

		DefinitionMock definition = new DefinitionMock();
		propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setName("withInverse");
		propertyDefinition.setUri("emf:withInverse");
		definition.getFields().add(propertyDefinition);

		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);
	}

	@Test
	public void getName() {
		assertEquals(AddRelationRequest.OPERATION_NAME, action.getName());
	}

	@Test(expected = BadRequestException.class)
	public void validate_shouldFailIfRelationIsNotDefinedInInstance() {
		request.setRelations(
				Collections.singletonMap("emf:someUndefinedRelation", new HashSet<>(Collections.singletonList("emf:destination"))));
		action.validate(request);
	}

	@Test
	public void validate_shouldNotFailIfRelationIsDefinedInInstance() {
		request.setRelations(
				Collections.singletonMap("emf:withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));
		action.validate(request);
	}

	@Test
	public void shouldAddUndefinedRelationAsIs() {
		request.setRelations(
				Collections.singletonMap("unknownRelation",
						new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);

		Instance resultInstance = (Instance) result;
		assertTrue(resultInstance.get("unknownRelation") instanceof Collection);
	}

	@Test
	public void addRelation() {
		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void shouldRemoveOldValueEventWhenNotConfiguredForOnNonMultivalueField() {

		instance.add("withInverse", new HashSet<>(Collections.singletonList("oldValue")));
		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.ofPredicate(
				(InstanceSaveContext context) -> "emf:destination".equals(context.getInstance().get("withInverse")))));
	}

	@Test
	public void shouldNotRemoveOldValuesForNonMultivalueField() {

		propertyDefinition.setMultiValued(Boolean.TRUE);
		instance.add("withInverse", new HashSet<>(Collections.singletonList("oldValue")));

		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.ofPredicate((InstanceSaveContext context) -> context
				.getInstance()
				.getAsCollection("withInverse", LinkedList::new)
				.size() == 2)));
	}

	@Test
	public void shouldNotFlatNonMultivalueFields() {

		propertyDefinition.setMultiValued(Boolean.TRUE);

		request.setRemoveExisting(true);
		instance.add("withInverse", new HashSet<>(Collections.singletonList("oldValue")));
		request.setRelations(
				Collections.singletonMap("withInverse", new HashSet<>(Collections.singletonList("emf:destination"))));

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
		instance.add("withInverse", new HashSet<>(Collections.singletonList("oldValue")));

		request.setRelations(Collections.singletonMap("withInverse",
				new HashSet<>(Arrays.asList("emf:destination1", "emf:destination2"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.ofPredicate((InstanceSaveContext context) -> context
				.getInstance()
				.getAsCollection("withInverse", LinkedList::new)
				.size() == 3)));
	}

	@Test
	public void shouldUpdateFieldWhenConfiguredWithUri() {
		instance.add("withInverse", new HashSet<>(Collections.singletonList("oldValue")));
		request.setRemoveExisting(true);
		request.setRelations(
				Collections.singletonMap("emf:withInverse",
						new HashSet<>(Collections.singletonList("emf:destination"))));

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			Instance resultInstance = context.getInstance();
			assertTrue(resultInstance.get("withInverse") instanceof String);
			assertNull(resultInstance.get("emf:withInverse"));
		})));
	}

	@Test
	public void addRelation_removeExistingTrue() {
		List<String> relations = Collections.singletonList("emf:destination");
		request.setRelations(Collections.singletonMap("withInverse", new HashSet<>(relations)));
		request.setRemoveExisting(true);

		Object result = action.perform(request);
		assertNotNull(result);
		assertTrue(result instanceof Instance);

		verify(instanceService).save(any(InstanceSaveContext.class));
	}

	@Test(expected = BadRequestException.class)
	public void addRelation_nullRequest() {
		action.validate(null);
	}
}
