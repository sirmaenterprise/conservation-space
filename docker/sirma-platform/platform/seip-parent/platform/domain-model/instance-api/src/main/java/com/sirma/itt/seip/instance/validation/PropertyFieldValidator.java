package com.sirma.itt.seip.instance.validation;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * Class used for base for all field validations for a certain instance. Each field validator is specialized in
 * validating a certain type of field definition, for example: text, number, etc. See the children for more information.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public abstract class PropertyFieldValidator implements Plugin {

	public static final String TARGET_NAME = "definitionPropertyValidators";

	@Inject
	protected LabelProvider labelProvider;

	protected RegExGenerator regExGenerator;

	/**
	 * Collects the values in a collection no matter if the values is multiValued or not.
	 *
	 * @return collection with one element if the value is not multivalued, otherwise the underlying collection.
	 */
	protected static Stream<Serializable> collectValues(FieldValidationContext context) {
		if (context.getValue() == null) {
			return Stream.empty();
		}
		if (context.getValue() instanceof Collection) {
			return ((Collection) context.getValue()).stream();
		}
		return Stream.of(context.getValue());
	}

	/**
	 * Validates is a value, that is set in the instance is valid.
	 *
	 * @param context
	 * 		the {@link FieldValidationContext}.
	 * @return a stream of validation errors.
	 */
	public abstract Stream<PropertyValidationError> validate(FieldValidationContext context);

	/**
	 * Checks if a concrete validator can handle a specific field by its type. This Method is implemented by each
	 * concrete validator and should return true if the field can be handled by the validator.
	 *
	 * @param context
	 * 		the {@link com.sirma.itt.seip.domain.definition.DefinitionModel}
	 * @return true to handle, false otherwise
	 */
	public abstract boolean isApplicable(FieldValidationContext context);

	@PostConstruct
	protected void init() {
		if (regExGenerator == null) {
			regExGenerator = new RegExGenerator(labelProvider::getValue);
		}
	}
}