package com.sirma.itt.seip.instance.validation;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides means of validating an instance based on the operation that is being performed.
 *
 * @author nvelkov
 */
@Documentation("Validator extension point.")
public interface Validator extends Plugin {

	String TARGET_NAME = "validator";

	/**
	 * Validate the instance based on the operation.
	 *
	 * @param validationContext the validation context
	 * @throws InstanceValidationException when errors are detected
	 */
	void validate(ValidationContext validationContext);
}
