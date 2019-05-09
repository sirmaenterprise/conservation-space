package com.sirma.itt.seip.instance.actions.relations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link UpdateRelationsAction}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateRelationsActionTest {

	private static final String INSTANCE_ID = "emf:instance-id";
	private static final String INSTANCE_ID_ONE = "emf:0001";
	private static final String INSTANCE_ID_TWO = "emf:0002";
	private static final String INSTANCE_ID_THREE = "emf:0003";
	private static final String INSTANCE_ID_FOUR = "emf:0004";

	private static final String PROPERTY_URI_FOR_REMOVE = "emf:hasWatchers";
	private static final String PROPERTY_NAME_FOR_REMOVE = "hasWatchers";
	private static final String PROPERTY_URI_FOR_ADD = "emf:hasAttachments";
	private static final String PROPERTY_NAME_FOR_ADD = "hasAttachments";

	@Mock
	private DomainInstanceService instanceService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	private UpdateRelationsAction action;

	@Test
	public void should_UpdateRelations() {
		Instance instance = createInstance();
		when(instanceService.loadInstance(any())).thenReturn(instance);
		UpdateRelationsRequest request = new UpdateRelationsRequest(INSTANCE_ID);
		request.setTargetReference(instance.toReference());

		setupLinkToBeAdded(request, "emf:have-to-be-added");
		setupLinkToBeRemoved(request, INSTANCE_ID_THREE);

		Instance updatedInstance = action.performAction(request);

		Collection<String> propertyWithAddedRelation = (Collection<String>) updatedInstance.get(PROPERTY_NAME_FOR_ADD);
		Assert.assertEquals(3, propertyWithAddedRelation.size());
		Assert.assertTrue(propertyWithAddedRelation.contains("emf:have-to-be-added"));
		Assert.assertTrue(propertyWithAddedRelation.contains(INSTANCE_ID_ONE));
		Assert.assertTrue(propertyWithAddedRelation.contains(INSTANCE_ID_TWO));

		Collection<String> propertyWithRemovedRelation = (Collection<String>) updatedInstance.get(PROPERTY_NAME_FOR_REMOVE);
		Assert.assertEquals(1, propertyWithRemovedRelation.size());
		Assert.assertFalse(propertyWithRemovedRelation.contains(INSTANCE_ID_THREE));
		Assert.assertTrue(propertyWithRemovedRelation.contains(INSTANCE_ID_FOUR));
	}

	private void setupLinkToBeAdded(UpdateRelationsRequest request, String instanceId) {
		Set<UpdateRelationData> toBeAdded = new HashSet<>(1);
		toBeAdded.add(new UpdateRelationData(PROPERTY_URI_FOR_ADD, createValue(instanceId)));
		request.setLinksToBeAdded(toBeAdded);
	}

	private void setupLinkToBeRemoved(UpdateRelationsRequest request, String instanceId) {
		Set<UpdateRelationData> toBeAdded = new HashSet<>(1);
		toBeAdded.add(new UpdateRelationData(PROPERTY_URI_FOR_REMOVE, createValue(instanceId)));
		request.setLinksToBeRemoved(toBeAdded);
	}

	@Test
	public void should_ReturnCorrectOperationName() {
		Assert.assertEquals(UpdateRelationsRequest.OPERATION_NAME, action.getName());
	}

	@Test
	public void should_ValidationPassed_When_() {
		UpdateRelationsRequest request = new UpdateRelationsRequest("emf:id");
		request.setTargetReference(Mockito.mock(InstanceReference.class));
		action.validate(request);
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_TargetIdIsEmpty() {
		action.validate(new UpdateRelationsRequest("  "));
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_TargetIdIsNull() {
		action.validate(new UpdateRelationsRequest(null));
	}

	@Test(expected = BadRequestException.class)
	public void should_ThrowBadRequestException_When_RequestIsNull() {
		action.validate(null);
	}

	@Test(expected = BadRequestException.class)
	public void validate_shouldFailIfRelationIsNotDefinedInInstance_onAdd() {
		UpdateRelationsRequest request = new UpdateRelationsRequest("emf:id");
		request.setTargetReference(createInstance().toReference());
		request.setLinksToBeAdded(
				Collections.singleton(new UpdateRelationData("emf:someUndefinedRelation",
						new HashSet<>(Collections.singletonList("emf:destination")))));
		action.validate(request);
	}

	@Test(expected = BadRequestException.class)
	public void validate_shouldFailIfRelationIsNotDefinedInInstance_onRemove() {
		UpdateRelationsRequest request = new UpdateRelationsRequest("emf:id");
		request.setTargetReference(createInstance().toReference());
		request.setLinksToBeRemoved(
				Collections.singleton(new UpdateRelationData("emf:someUndefinedRelation",
						new HashSet<>(Collections.singletonList("emf:destination")))));
		action.validate(request);
	}

	@Test
	public void validate_shouldNotFailIfRelationIsDefinedInInstance() {
		UpdateRelationsRequest request = new UpdateRelationsRequest("emf:id");
		request.setTargetReference(createInstance().toReference());
		request.setLinksToBeAdded(
				Collections.singleton(new UpdateRelationData(PROPERTY_URI_FOR_REMOVE,
						new HashSet<>(Collections.singletonList("emf:destination")))));
		request.setLinksToBeRemoved(
				Collections.singleton(new UpdateRelationData(PROPERTY_URI_FOR_ADD,
						new HashSet<>(Collections.singletonList("emf:destination")))));
		action.validate(request);
	}

	private Instance createInstance() {
		DefinitionMock definitionModel = new DefinitionMock();
		definitionModel.getFields().add(createPropertyDefinition(PROPERTY_NAME_FOR_REMOVE,
				PROPERTY_URI_FOR_REMOVE, true));
		definitionModel.getFields().add(createPropertyDefinition(PROPERTY_NAME_FOR_ADD, PROPERTY_URI_FOR_ADD, true));
		Instance instance = InstanceReferenceMock.createGeneric(INSTANCE_ID).toInstance();
		instance.add(PROPERTY_NAME_FOR_ADD, (Serializable) createValue(INSTANCE_ID_ONE, INSTANCE_ID_TWO));
		instance.add(PROPERTY_NAME_FOR_REMOVE, (Serializable) createValue(INSTANCE_ID_THREE, INSTANCE_ID_FOUR));
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(definitionModel);
		return instance;
	}

	private Set<String> createValue(String... values) {
		Set<String> result = new HashSet<>(values.length);
		for (String value: values) {
			result.add(value);
		}
		return result;
	}


	private PropertyDefinition createPropertyDefinition(String propertyName, String propertyUri, boolean isMultiValue) {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Mockito.when(propertyDefinition.getUri()).thenReturn(propertyUri);
		Mockito.when(propertyDefinition.getName()).thenReturn(propertyName);
		Mockito.when(propertyDefinition.isMultiValued()).thenReturn(isMultiValue);
		return propertyDefinition;
	}
}
