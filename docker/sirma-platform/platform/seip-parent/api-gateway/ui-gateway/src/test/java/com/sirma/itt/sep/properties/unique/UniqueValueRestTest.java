package com.sirma.itt.sep.properties.unique;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;

/**
 * Unit tests for {@link UniqueValueRest}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueRestTest {

    private static final String DEFINITION_ID = "DT0007";
    private static final String PROPERTY_VALUE = "value of property";
    private static final String PROPERTY_NAME = "title";

    private static final String UNIQUE = "unique";

    @Mock
    private UniqueValueValidationService uniqueValueValidationService;

    @Mock
    private DefinitionService definitionService;

    @Mock
    private TypeConverter typeConverter;

    @InjectMocks
    private UniqueValueRest rest;

    @Test
    public void should_BeUnique_When_ConvertedValueIsNull() {
        UniqueValueRequest request = getUniqueValueRequest();
        UniqueValueRestTestHelper definitionBuilder = new UniqueValueRestTestHelper(DEFINITION_ID);
        definitionBuilder.addUniqueValueWithNullConvertedValue(PROPERTY_NAME, PROPERTY_VALUE);

        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_NotBeUnique_When_PropertyDefinitionIsDefinedAsUniqueAndValueIsAlreadyRegistered() {
        UniqueValueRequest request = getUniqueValueRequest();
        UniqueValueRestTestHelper definitionBuilder = new UniqueValueRestTestHelper(DEFINITION_ID);
        definitionBuilder.addRegisteredUniqueValue(PROPERTY_NAME, PROPERTY_VALUE, "convertedProperty");

        Assert.assertFalse(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_BeUnique_When_TypeConverterThrowException() {
        UniqueValueRequest request = getUniqueValueRequest();
        UniqueValueRestTestHelper definitionBuilder = new UniqueValueRestTestHelper(DEFINITION_ID);
        definitionBuilder.addUniqueValueWithExceptionWhenCovertValue(PROPERTY_NAME, PROPERTY_VALUE,
                                                                     "convertedProperty");

        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_BeUnique_When_PropertyDefinitionIsDefinedAsUniqueAndValueIsNotAlreadyRegistered() {
        UniqueValueRequest request = getUniqueValueRequest();
        UniqueValueRestTestHelper definitionBuilder = new UniqueValueRestTestHelper(DEFINITION_ID);
        definitionBuilder.addUniqueValue(PROPERTY_NAME, PROPERTY_VALUE, "convertedProperty");

        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_BeUnique_When_PropertyDefinitionIsNotDefinedAsUnique() {
        UniqueValueRequest request = getUniqueValueRequest();
        UniqueValueRestTestHelper definitionBuilder = new UniqueValueRestTestHelper(DEFINITION_ID);
        definitionBuilder.addNonUniqueValue(PROPERTY_NAME, PROPERTY_VALUE, "convertedProperty");

        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_BeUnique_When_DefinitionPropertyNotFound() {
        UniqueValueRequest request = new UniqueValueRequest();
        request.setValue(PROPERTY_VALUE);
        request.setDefinitionId(DEFINITION_ID);
        new UniqueValueRestTestHelper(DEFINITION_ID);
        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    @Test
    public void should_BeUnique_When_ValueIsNull() {
        UniqueValueRequest request = new UniqueValueRequest();
        Assert.assertTrue(rest.checksValueForUniqueness(request).get(UNIQUE));
    }

    private UniqueValueRequest getUniqueValueRequest() {
        UniqueValueRequest request = new UniqueValueRequest();
        request.setValue(PROPERTY_VALUE);
        request.setPropertyName(PROPERTY_NAME);
        request.setDefinitionId(DEFINITION_ID);
        return request;
    }

    private class UniqueValueRestTestHelper {
        private DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
        private String definitionId;

        private UniqueValueRestTestHelper(String definitionId) {
            this.definitionId = definitionId;
            Mockito.when(definitionService.find(definitionId)).thenReturn(instanceDefinition);
            Mockito.when(instanceDefinition.getField(null)).thenReturn(Optional.empty());
        }

        private void addUniqueValueWithExceptionWhenCovertValue(String propertyName, Object value,
                Object convertedValue) {
            addUniqueValue(propertyName, value, convertedValue);
            Mockito.when(typeConverter.convert(Matchers.any(), Matchers.eq(value))).thenThrow(ClassCastException.class);
        }

        private void addUniqueValueWithNullConvertedValue(String propertyName, Object value) {
            PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
            Mockito.when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
            DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
            Mockito.when(dataType.getJavaClass()).thenAnswer(invocation -> String.class);
            Mockito.when(typeConverter.convert(String.class, value))
                    .thenAnswer(invocation -> null);
            Mockito.when(propertyDefinition.getDataType()).thenReturn(dataType);
            Mockito.when(propertyDefinition.isUnique()).thenReturn(true);
        }

        private void addRegisteredUniqueValue(String propertyName, Object value, Object convertedValue) {

            PropertyDefinition propertyDefinition = addUniqueValue(propertyName, value, convertedValue);
            Mockito.when(uniqueValueValidationService.hasRegisteredValueForAnotherInstance(null, definitionId,
                                                                                           propertyDefinition, convertedValue))
                    .thenReturn(true);
        }

        private PropertyDefinition addUniqueValue(String propertyName, Object value, Object convertedValue) {
            PropertyDefinition propertyDefinition = addNonUniqueValue(propertyName, value, convertedValue);
            Mockito.when(propertyDefinition.isUnique()).thenReturn(true);
            return propertyDefinition;
        }

        private PropertyDefinition addNonUniqueValue(String propertyName, Object value, Object convertedValue) {
            PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
            Mockito.when(instanceDefinition.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
            DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
            Mockito.when(dataType.getJavaClass()).thenAnswer(invocation -> convertedValue.getClass());
            Mockito.when(typeConverter.convert(convertedValue.getClass(), value))
                    .thenAnswer(invocation -> convertedValue);
            Mockito.when(propertyDefinition.getDataType()).thenReturn(dataType);
            return propertyDefinition;
        }
    }
}