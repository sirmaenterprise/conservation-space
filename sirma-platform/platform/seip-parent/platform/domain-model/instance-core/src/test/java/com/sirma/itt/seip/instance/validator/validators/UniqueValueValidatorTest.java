package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link UniqueValueValidator}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueValidatorTest {

    @Mock
    private FieldValidationErrorBuilder builder;

    @Mock
    protected UniqueValueValidationService uniqueValueValidationService;

    @Mock
    private RevisionService revisionService;

    @InjectMocks
    private UniqueValueValidator uniqueValueValidator;

    @Test
    public void should_ProduceError_When_ValueIsRegisteredForAnotherInstance() {
        FieldValidationContext context = new FieldValidationContext();
        Instance instance = Mockito.mock(Instance.class);
        Mockito.when(revisionService.isRevision(instance)).thenReturn(false);
        context.setInstance(instance);
        PropertyDefinition propertyDefinition = createUniquePropertyDefinition();
        context.setPropertyDefinition(propertyDefinition);

        Mockito.when(uniqueValueValidationService.hasRegisteredValueForAnotherInstance(instance, propertyDefinition))
                .thenReturn(true);
        Assert.assertEquals(1, uniqueValueValidator.validate(context).count());
    }

    @Test
    public void should_NotProduceError_When_ValueIsNotRegisteredForAnotherInstance() {
        FieldValidationContext context = new FieldValidationContext();
        Instance instance = Mockito.mock(Instance.class);
        Mockito.when(revisionService.isRevision(instance)).thenReturn(false);
        context.setInstance(instance);
        PropertyDefinition propertyDefinition = createUniquePropertyDefinition();
        context.setPropertyDefinition(propertyDefinition);

        Mockito.when(uniqueValueValidationService.hasRegisteredValueForAnotherInstance(instance, propertyDefinition))
                .thenReturn(false);
        Assert.assertEquals(0, uniqueValueValidator.validate(context).count());
    }

    @Test
    public void should_NotProduceError_When_instanceIsRevision() {
        FieldValidationContext context = new FieldValidationContext();
        Instance instance = Mockito.mock(Instance.class);
        Mockito.when(revisionService.isRevision(instance)).thenReturn(true);
        context.setInstance(instance);
        Assert.assertEquals(0, uniqueValueValidator.validate(context).count());
    }

    @Test
    public void should_NotBeApplicable_When_PropertyDefinitionIsNotUnique() {
        FieldValidationContext context = new FieldValidationContext();
        context.setPropertyDefinition(createNonUniquePropertyDefinition());
        Assert.assertFalse(uniqueValueValidator.isApplicable(context));
    }

    @Test
    public void should_BeApplicable_When_PropertyDefinitionIsUnique() {
        FieldValidationContext context = new FieldValidationContext();
        context.setPropertyDefinition(createUniquePropertyDefinition());
        Assert.assertTrue(uniqueValueValidator.isApplicable(context));
    }

    private PropertyDefinition createNonUniquePropertyDefinition() {
        PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
        Mockito.when(propertyDefinition.isMultiValued()).thenReturn(false);
        Mockito.when(propertyDefinition.isUnique()).thenReturn(false);
        DataTypeDefinition dataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
        Mockito.when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
        Mockito.when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
        return propertyDefinition;
    }

    private PropertyDefinition createUniquePropertyDefinition() {
        PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
        Mockito.when(propertyDefinition.isMultiValued()).thenReturn(false);
        Mockito.when(propertyDefinition.isUnique()).thenReturn(true);
        DataTypeDefinition dataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
        Mockito.when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
        Mockito.when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
        return propertyDefinition;
    }
}