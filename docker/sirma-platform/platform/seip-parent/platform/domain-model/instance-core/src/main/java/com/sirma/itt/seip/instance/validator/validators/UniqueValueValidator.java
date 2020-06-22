package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;

import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * Validates a value of property defined as unique.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 12)
public class UniqueValueValidator extends PropertyFieldValidator {

    @Inject
    private FieldValidationErrorBuilder builder;

    @Inject
    private UniqueValueValidationService uniqueValueValidationService;

    @Inject
    private RevisionService revisionService;

    @Override
    public boolean isApplicable(FieldValidationContext context) {
        PropertyDefinition propertyDefinition = context.getPropertyDefinition();
        return PropertyDefinition.isUniqueProperty().test(propertyDefinition);
    }

    @Override
    public Stream<PropertyValidationError> validate(FieldValidationContext context) {
        Instance instance = context.getInstance();
        //we skip the revision instance as from requirement.
        if (!revisionService.isRevision(instance)) {
            PropertyDefinition propertyDefinition = context.getPropertyDefinition();
            if (uniqueValueValidationService.hasRegisteredValueForAnotherInstance(instance, propertyDefinition)) {
                return Stream.of(builder.buildUniqueValueError(propertyDefinition));
            }
        }
        return Stream.empty();
    }
}
