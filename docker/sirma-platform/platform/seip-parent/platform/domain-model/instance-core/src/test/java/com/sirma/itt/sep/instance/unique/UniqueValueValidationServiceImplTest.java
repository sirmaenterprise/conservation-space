package com.sirma.itt.sep.instance.unique;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.sirma.itt.sep.instance.unique.loader.UniqueValueLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.sep.instance.unique.persistence.UniqueField;
import com.sirma.itt.sep.instance.unique.persistence.UniqueValueDao;

/**
 * Unit tests for {@link UniqueValueValidationServiceImpl}.
 *
 * @author Boyan Tonchev
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueValidationServiceImplTest {

	private static final String PROPERTY_NAME_TITLE = "title";
	private static final String PROPERTY_TITLE_URI = "emf:titleUri";
	private static final String PROPERTY_TITLE_VALUE = "emptyValue";
	private static final String PROPERTY_TITLE_CONVERTED_VALUE = "convertedValue";
	private static final String INSTANCE_ID = "emf:00087";
	private static final String DEFINITION_ID = "DT0007";
	private static final String ANOTHER_INSTANCE_ID = "id-of-another-instance";

	/**
	 * Constants for should_UpdateAllUniqueFields method.
	 */
	private static final String DEFINITION_ID_ONE = "definition-id-of-model-one";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_ONE = "existing-field-uri-of-definition-one";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_ADDED = "existing-field-uri-of-definition-one-to-be-added";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_REMOVED = "existing-field-uri-of-definition-one-to-be-removed";
	private static final String NON_UNIQUE_FIELD_URI_OF_DEFINITION_ONE = "non-unique-field-uri-of-definition-one";

	private static final String DEFINITION_ID_TWO = "definition-id-of-model-two";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_TWO = "existing-field-uri-of-definition-two";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_ADDED = "existing-field-uri-of-definition-two-to-be-added";
	private static final String EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_REMOVED = "existing-field-uri-of-definition-two-to-be-removed";
	private static final String NON_UNIQUE_FIELD_URI_OF_DEFINITION_TWO = "non-unique-field-uri-of-definition-two";

	@Mock
	private DefinitionService definitionService;

	@Mock
	private UniqueValueDao uniqueValueDao;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private UniqueValueLoader uniqueValueLoader;

	@InjectMocks
	private UniqueValueValidationServiceImpl uniqueValueValidationService;

	@Test
	public void should_NotBeRegistered_When_CheckedValueIsNull() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition uniquePropertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, null, null, DataTypeDefinition.TEXT);
		Assert.assertFalse(uniqueValueValidationService
				.hasRegisteredValueForAnotherInstance(instanceBuilder.getInstance(), uniquePropertyDefinition));
	}

	@Test
	public void should_Return_AllUniqueFields_When_MethodIsCalled() {

		Collection<PropertyDefinition> propertyDefinitions = uniqueValueValidationService
				.extractUniquePropertyDefinitions(setUpDictionaryService().stream());

		Assert.assertTrue(propertyDefinitions.size() == 4);
		Assert.assertTrue(hasPropertyDefinition(propertyDefinitions, EXISTING_FIELD_URI_OF_DEFINITION_TWO));
		Assert.assertTrue(hasPropertyDefinition(propertyDefinitions, EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_ADDED));
		Assert.assertTrue(hasPropertyDefinition(propertyDefinitions, EXISTING_FIELD_URI_OF_DEFINITION_ONE));
		Assert.assertTrue(hasPropertyDefinition(propertyDefinitions, EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_ADDED));
	}

	private static boolean hasPropertyDefinition(Collection<PropertyDefinition> propertyDefinitions, String fieldUri) {
		return propertyDefinitions
				.stream()
					.filter(propertyDefinition -> propertyDefinition.getUri().equals(fieldUri))
					.count() == 1;
	}

	@Test
	public void should_RegisterUniqueValues_WhenMethodIsCalled() {
		UniqueValueValidationServiceImpl spiedUniqueService = spy(uniqueValueValidationService);
		DefinitionModel instanceDefinition = mock(DefinitionModel.class);
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition uniquePropertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, PROPERTY_TITLE_CONVERTED_VALUE, DataTypeDefinition.TEXT);
		when(uniquePropertyDefinition.isUnique()).thenReturn(true);
		PropertyDefinition dateUniquePropertyDefinition = instanceBuilder.addDatePropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, new Date(), DataTypeDefinition.DATE);
		when(dateUniquePropertyDefinition.isUnique()).thenReturn(true);
		PropertyDefinition dateTimeUniquePropertyDefinition = instanceBuilder.addDatePropertyDefinition(
				PROPERTY_NAME_TITLE, PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, new Date(), DataTypeDefinition.DATETIME);
		when(dateTimeUniquePropertyDefinition.isUnique()).thenReturn(true);

		Instance instance = instanceBuilder.getInstance();

		when(instanceDefinition.fieldsStream()).thenReturn(
				Stream.of(uniquePropertyDefinition, dateUniquePropertyDefinition, dateTimeUniquePropertyDefinition));

		when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
		doCallRealMethod().when(spiedUniqueService).registerUniqueValues(instance);
		doCallRealMethod().when(spiedUniqueService).extractUniquePropertyDefinitions(Matchers.any());

		spiedUniqueService.registerUniqueValues(instance);

		verify(spiedUniqueService, times(2)).registerUniqueValue(Matchers.eq(instance),
				Matchers.any(PropertyDefinition.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_UpdateAllUniqueFields() {
		setupQuerySelectAllResult();
		setUpDictionaryService();

		uniqueValueValidationService.updateUniqueFields();

		ArgumentCaptor<Set<UniqueField>> removeAllArgCapture = ArgumentCaptor.forClass(Set.class);
		verify(uniqueValueDao).unRegister(removeAllArgCapture.capture());
		assertRemoveAllArguments(removeAllArgCapture.getValue());

		ArgumentCaptor<Set<UniqueField>> addAllArgCapture = ArgumentCaptor.forClass(Set.class);
		verify(uniqueValueDao).register(addAllArgCapture.capture());
		assertAddAndRegisterArguments(addAllArgCapture.getValue());

		ArgumentCaptor<Set<UniqueField>> registerUniqueValuesArgCapture = ArgumentCaptor.forClass(Set.class);
		verify(uniqueValueLoader).registerUniqueValues(registerUniqueValuesArgCapture.capture());
		assertAddAndRegisterArguments(registerUniqueValuesArgCapture.getValue());

	}

	private static void assertRemoveAllArguments(Set<UniqueField> toBeRemovedArgument) {
		Assert.assertTrue(toBeRemovedArgument.size() == 2);
		Assert.assertTrue(hasUniqueField(toBeRemovedArgument, DEFINITION_ID_ONE,
				EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_REMOVED));
		Assert.assertTrue(hasUniqueField(toBeRemovedArgument, DEFINITION_ID_TWO,
				EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_REMOVED));
	}

	private static void assertAddAndRegisterArguments(Set<UniqueField> toBeAddedArgument) {
		Assert.assertTrue(toBeAddedArgument.size() == 2);
		Assert.assertTrue(
				hasUniqueField(toBeAddedArgument, DEFINITION_ID_ONE, EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_ADDED));
		Assert.assertTrue(
				hasUniqueField(toBeAddedArgument, DEFINITION_ID_TWO, EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_ADDED));
	}

	private static boolean hasUniqueField(Set<UniqueField> fields, String definitionId, String fieldUri) {
		return fields.stream().filter(uniqueField -> {
			return uniqueField.getDefinitionId().equals(definitionId) && uniqueField.getFieldUri().equals(fieldUri);
		}).count() == 1;
	}

	@Test
	public void should_UnregisterAllUniqueFieldsForInstance() {
		uniqueValueValidationService.unRegisterUniqueValues(INSTANCE_ID);
		verify(uniqueValueDao).unregisterAllUniqueValuesForInstance(INSTANCE_ID);
	}

	@Test
	public void should_UpdateRegisteredValue_When_NewValueIsNotNull() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, PROPERTY_TITLE_CONVERTED_VALUE, DataTypeDefinition.TEXT);
		uniqueValueValidationService.registerUniqueValue(instanceBuilder.getInstance(), propertyDefinition);

		verify(uniqueValueDao).registerOrUpdateUniqueValue(INSTANCE_ID, DEFINITION_ID, PROPERTY_TITLE_URI,
				PROPERTY_TITLE_CONVERTED_VALUE);
	}

	@Test
	public void should_UnregisterValue_When_NewValueIsNull() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, null, DataTypeDefinition.TEXT);
		uniqueValueValidationService.registerUniqueValue(instanceBuilder.getInstance(), propertyDefinition);

		verify(uniqueValueDao).unregisterValue(INSTANCE_ID, DEFINITION_ID, PROPERTY_TITLE_URI);
	}

	@Test
	public void should_BeRegisteredForAnotherInstance_When_ValueIRegisteredForAnotherInstance() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, PROPERTY_TITLE_CONVERTED_VALUE, DataTypeDefinition.TEXT);
		Mockito
				.when(uniqueValueDao.getInstanceId(DEFINITION_ID, PROPERTY_TITLE_URI, PROPERTY_TITLE_CONVERTED_VALUE))
					.thenReturn(ANOTHER_INSTANCE_ID);

		Assert.assertTrue(uniqueValueValidationService
				.hasRegisteredValueForAnotherInstance(instanceBuilder.getInstance(), propertyDefinition));
	}

	@Test
	public void should_NotBeRegisteredForAnotherInstance_When_ValueIRegisteredForCurrentInstance() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, PROPERTY_TITLE_CONVERTED_VALUE, DataTypeDefinition.TEXT);
		Mockito
				.when(uniqueValueDao.getInstanceId(DEFINITION_ID, PROPERTY_TITLE_URI, PROPERTY_TITLE_CONVERTED_VALUE))
					.thenReturn(INSTANCE_ID);

		Assert.assertFalse(uniqueValueValidationService
				.hasRegisteredValueForAnotherInstance(instanceBuilder.getInstance(), propertyDefinition));
	}

	@Test
	public void should_NotBeRegisteredForAnotherInstance_When_RegisteredInstanceIdNotFound() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, PROPERTY_TITLE_CONVERTED_VALUE, DataTypeDefinition.TEXT);

		Assert.assertFalse(uniqueValueValidationService
				.hasRegisteredValueForAnotherInstance(instanceBuilder.getInstance(), propertyDefinition));
	}

	@Test
	public void should_NotBeRegisteredForAnotherInstance_When_ConvertedValueIsNull() {
		InstanceBuilder instanceBuilder = new InstanceBuilder(INSTANCE_ID, DEFINITION_ID);
		PropertyDefinition propertyDefinition = instanceBuilder.addPropertyDefinition(PROPERTY_NAME_TITLE,
				PROPERTY_TITLE_URI, PROPERTY_TITLE_VALUE, null, DataTypeDefinition.TEXT);

		Assert.assertFalse(uniqueValueValidationService
				.hasRegisteredValueForAnotherInstance(instanceBuilder.getInstance(), propertyDefinition));
	}

	private void setupQuerySelectAllResult() {
		List<UniqueField> querySelectAllResult = new ArrayList<>(2);
		querySelectAllResult.add(createUniqueFieldEntity(DEFINITION_ID_ONE, EXISTING_FIELD_URI_OF_DEFINITION_ONE));
		querySelectAllResult
				.add(createUniqueFieldEntity(DEFINITION_ID_ONE, EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_REMOVED));
		querySelectAllResult.add(createUniqueFieldEntity(DEFINITION_ID_TWO, EXISTING_FIELD_URI_OF_DEFINITION_TWO));
		querySelectAllResult
				.add(createUniqueFieldEntity(DEFINITION_ID_TWO, EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_REMOVED));
		when(uniqueValueDao.getAllUniqueFields()).thenReturn(querySelectAllResult);
	}

	private List<DefinitionModel> setUpDictionaryService() {
		List<DefinitionModel> models = new ArrayList<>(3);
		models.add(createDefinitionModel(DEFINITION_ID_TWO, createDefinitionTwoFields()));
		models.add(createDefinitionModel(DEFINITION_ID_ONE, createDefinitionOneFields()));
		when(definitionService.getAllDefinitions()).thenReturn(models.stream());
		return models;
	}

	private static DefinitionModel createDefinitionModel(String definitionId, List<PropertyDefinition> fields) {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionModel.getIdentifier()).thenReturn(definitionId);
		when(definitionModel.fieldsStream()).thenReturn(fields.stream());
		return definitionModel;
	}

	private static List<PropertyDefinition> createDefinitionTwoFields() {
		List<PropertyDefinition> fields = new ArrayList<>();
		fields.add(createPropertyDefinition(EXISTING_FIELD_URI_OF_DEFINITION_TWO, true));
		fields.add(createPropertyDefinition(NON_UNIQUE_FIELD_URI_OF_DEFINITION_TWO, false));
		fields.add(createPropertyDefinition(EXISTING_FIELD_URI_OF_DEFINITION_TWO_TO_BE_ADDED, true));
		return fields;
	}

	private static List<PropertyDefinition> createDefinitionOneFields() {
		List<PropertyDefinition> fields = new ArrayList<>();
		fields.add(createPropertyDefinition(EXISTING_FIELD_URI_OF_DEFINITION_ONE_TO_BE_ADDED, true));
		fields.add(createPropertyDefinition(EXISTING_FIELD_URI_OF_DEFINITION_ONE, true));
		fields.add(createPropertyDefinition(NON_UNIQUE_FIELD_URI_OF_DEFINITION_ONE, false));
		return fields;
	}

	private static PropertyDefinition createPropertyDefinition(String fieldUry, boolean unique) {
		PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
		when(propertyDefinition.isUnique()).thenReturn(unique);
		when(propertyDefinition.isMultiValued()).thenReturn(false);
		when(propertyDefinition.getUri()).thenReturn(fieldUry);
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
		return propertyDefinition;
	}

	private static UniqueField createUniqueFieldEntity(String definitionId, String fieldUry) {
		UniqueField uniqueField = mock(UniqueField.class);
		when(uniqueField.getDefinitionId()).thenReturn(definitionId);
		when(uniqueField.getFieldUri()).thenReturn(fieldUry);
		return uniqueField;
	}

	class InstanceBuilder {

		private DefinitionModel instanceDefinition;

		private Instance instance;

		InstanceBuilder(String instanceId, String definitionId) {
			instanceDefinition = mock(DefinitionModel.class);
			when(instanceDefinition.getIdentifier()).thenReturn(definitionId);
			instance = mock(Instance.class);
			when(instance.getIdentifier()).thenReturn(definitionId);
			when(instance.getId()).thenReturn(instanceId);
			when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
		}

		PropertyDefinition addPropertyDefinition(String propertyName, String fieldUri, Serializable value,
				String convertedValue, String dataTypeName) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getName()).thenReturn(propertyName);
			when(propertyDefinition.getUri()).thenReturn(fieldUri);
			when(instance.get(propertyName)).thenReturn(value);
			when(typeConverter.convert(String.class, value)).thenReturn(convertedValue);
			DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
			when(dataTypeDefinition.getName()).thenReturn(dataTypeName);
			when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
			return propertyDefinition;
		}

		PropertyDefinition addDatePropertyDefinition(String propertyName, String fieldUri, Serializable value,
				Date convertedValue, String dataTypeName) {
			PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
			when(propertyDefinition.getName()).thenReturn(propertyName);
			when(propertyDefinition.getUri()).thenReturn(fieldUri);
			when(instance.get(propertyName)).thenReturn(value);
			when(typeConverter.convert(Date.class, value)).thenReturn(convertedValue);
			DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
			when(dataTypeDefinition.getName()).thenReturn(dataTypeName);
			when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
			return propertyDefinition;
		}

		Instance getInstance() {
			return instance;
		}
	}
}