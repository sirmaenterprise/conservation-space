package com.sirma.itt.seip.instance.validator.validators;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Contains logic for validating {@link PropertyDefinition} that has an URI in it. Currently the class only checks the
 * URI for validity. For the future would be nice to check if given URIs are correct for our system, for example if
 * emf:Ivo is valid user in the system.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 11)
public class UriFieldValidator extends PropertyFieldValidator {

	private static final Pattern VALID_URI_PATTERN = Pattern
			.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private static final Predicate<String> VALID_URI = value -> VALID_URI_PATTERN.matcher(value).matches();

	private static final Pattern VALID_SHORT_URI_PATTERN = Pattern.compile("[a-zA-Z0-9]+:[a-zA-Z0-9-._]+");
	private static final Predicate<String> VALID_SHORT_URI = value -> VALID_SHORT_URI_PATTERN.matcher(value).matches();

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		return collectValues(context)
				.map(value -> typeConverter.convert(String.class, value))
					.filter(VALID_URI.negate().and(VALID_SHORT_URI.negate()))
					.map(value -> builder.buildUriFieldError(context.getPropertyDefinition()));
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return PropertyDefinition.isObjectProperty().test(context.getPropertyDefinition())
				&& context.getValue() != null;
	}
}
