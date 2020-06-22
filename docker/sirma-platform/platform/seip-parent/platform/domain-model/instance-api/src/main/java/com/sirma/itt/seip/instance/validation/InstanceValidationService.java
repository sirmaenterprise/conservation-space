package com.sirma.itt.seip.instance.validation;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

import java.util.Optional;

/**
 * Contains instance validations that will be run mainly from the {@link Validator} chain.
 *
 * @author nvelkov
 */
public interface InstanceValidationService {

	/**
	 * Check if create/upload operations can be performed in the given instance. The performed validations are based on:
	 * <ul>
	 * <li>The state of the instance - the instance must be in a state that allows the createInContext action to be
	 * executed.</li>
	 * </ul>
	 *
	 * @param instanceId
	 * 		id of the instance to check
	 * @param purpose
	 * 		the purpose of the operation - either create or upload
	 * @return a non-empty String containing the error message if the operation can't be performed.
	 */
	Optional<String> canCreateOrUploadIn(String instanceId, String purpose);

	/**
	 * Check if create/upload operations can be performed in the given instance. The performed validations are based on:
	 * <ul>
	 * <li>The state of the instance - the instance must be in a state that allows the createInContext action to be
	 * executed.</li>
	 * </ul>
	 *
	 * @param instance
	 * 		instance to check
	 * @return a non-empty String containing the error message if the operation can't be performed.
	 */
	Optional<String> canCreateOrUploadIn(Instance instance);

	/**
	 * Validates {@link Instance} data for correctness. This service does the following steps:
	 * <ol>
	 * <li>Extract the corresponding {@link com.sirma.itt.seip.domain.definition.DefinitionModel} for the instance</li>
	 * <li>For each {@link com.sirma.itt.seip.domain.definition.PropertyDefinition} from the
	 * {@link com.sirma.itt.seip.domain.definition.DefinitionModel} check if the data stored in the instance is
	 * correct.</li>
	 * </ol>
	 * Most of the validations would not just check if there is a correct data type in the field but also if the
	 * value is correct. See concrete implementations for more information.
	 *
	 * @param context
	 * 		should contain the instance that has to be validated and the operation. Both are mandatory in order to be
	 * 		able to evaluate the conditions.
	 * @return InstanceValidationResult
	 */
	InstanceValidationResult validate(ValidationContext context);

	/**
	 * Check if instance with <code>instanceDefinition</code> can exist in context.
	 *
	 * @param instanceDefinition - the definition of instance.
	 * @return true if instance with <code>instanceDefinition</code> can exist in context.
	 */
	boolean canExistInContext(DefinitionModel instanceDefinition);

	/**
	 * Check if instance with <code>instanceDefinition</code> can exist without context.
	 *
	 * @param instanceDefinition - the definition of instance.
	 * @return true if instance with <code>instanceDefinition</code> can exist without context.
	 */
	boolean canExistWithoutContext(DefinitionModel instanceDefinition);
}
