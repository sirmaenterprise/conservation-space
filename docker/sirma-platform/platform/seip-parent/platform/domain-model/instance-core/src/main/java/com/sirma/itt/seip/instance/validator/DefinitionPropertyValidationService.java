package com.sirma.itt.seip.instance.validator;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains all concrete field validator implementation. When passed a {@link FieldValidationContext} which
 * should contain the property definition and the property value we run this context against all validators that are
 * applicable for the definition property.
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@ApplicationScoped
public class DefinitionPropertyValidationService {

	@Inject
	@ExtensionPoint(value = PropertyFieldValidator.TARGET_NAME)
	private Plugins<PropertyFieldValidator> validators;

	/**
	 * Validates a instance property against its {@link com.sirma.itt.seip.domain.definition.PropertyDefinition}. Can
	 * return one or more error if multiple validators are executed on it.
	 *
	 * @param context
	 * 		the context
	 * @return list of validations errors or empty list if the validation passes.
	 */
	public List<PropertyValidationError> validate(FieldValidationContext context) {
		return validators.stream()
				.filter(validator -> validator.isApplicable(context))
				.flatMap(validator -> validator.validate(context))
				.collect(Collectors.toList());
	}
}
