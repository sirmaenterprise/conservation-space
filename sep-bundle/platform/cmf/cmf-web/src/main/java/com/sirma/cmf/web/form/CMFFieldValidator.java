package com.sirma.cmf.web.form;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.esotericsoftware.minlog.Log;
import com.sirma.itt.emf.domain.Pair;

/**
 * JSF validator validating jsf controls.
 * 
 * @author svelikov
 */
@FacesValidator(value = "CMFFieldValidator")
public class CMFFieldValidator implements Validator {

	/**
	 * Validates a jsf control against field information. If the validation fails,
	 * ValidatorException with error message is thrown.
	 * 
	 * @param context
	 *            faces context.
	 * @param component
	 *            the component to be validated.
	 * @param value
	 *            the value to be validated.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(final FacesContext context, final UIComponent component, final Object value) {

		// don't validate null values
		if (value == null) {
			return;
		}

		Pair<String, String> validatorData = (Pair<String, String>) component.getAttributes().get(
				"validationPattern");
		String patternAttr = validatorData.getFirst();
		String message = validatorData.getSecond();

		if ((patternAttr != null) && !"".equals(patternAttr)) {
			validateSingleValue(value, compilePattern(patternAttr), message);
		} else {
			throw new IllegalStateException("Field validator is missing validation pattern!");
		}
	}

	/**
	 * Validates a single string value.
	 * 
	 * @param value
	 *            The value to be validated.
	 * @param pattern
	 *            To be used for validating.
	 * @param message
	 *            the message
	 */
	private void validateSingleValue(final Object value, final Pattern pattern, String message) {

		final String stringValue = value.toString().trim();

		if (!pattern.matcher(stringValue).matches()) {
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, message,
					message));
		}
	}

	/**
	 * Compile a pattern from the given string.
	 * 
	 * @param pattern
	 *            The string to be compiled to pattern.
	 * @return Compiled pattern.
	 */
	private Pattern compilePattern(String pattern) {
		try {
			return Pattern.compile(pattern);
		} catch (Exception e) {
			Log.error("Regex compilation error!", e);
			return Pattern.compile("");
		}
	}

}