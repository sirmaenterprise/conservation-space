package com.sirma.itt.seip.instance.validation;

import java.util.Optional;

import com.sirma.itt.seip.domain.instance.Instance;

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
	 *            id of the instance to check
	 * @param purpose
	 *            the purpose of the operation - either create or upload
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
	 *            instance to check
	 * @return a non-empty String containing the error message if the operation can't be performed.
	 */
	Optional<String> canCreateOrUploadIn(Instance instance);

}
