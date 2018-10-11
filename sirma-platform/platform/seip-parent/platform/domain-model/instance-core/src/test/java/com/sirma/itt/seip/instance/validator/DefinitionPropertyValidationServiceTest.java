package com.sirma.itt.seip.instance.validator;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.CodelistFieldValidationError;
import com.sirma.itt.seip.plugin.Plugins;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefinitionPropertyValidationService}
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class DefinitionPropertyValidationServiceTest {

	@InjectMocks
	private DefinitionPropertyValidationService service;
	@Mock
	private Plugins<PropertyFieldValidator> validators;
	@Mock
	private CodelistFieldValidationError error;
	@Mock
	private FieldValidationContext context;
	@Mock
	private PropertyFieldValidator testApplicableValidatorNoError;
	@Mock
	private PropertyFieldValidator testApplicableValidatorError;
	@Mock
	private PropertyFieldValidator testNonApplicableValidator;

	@Before

	public void init() {
		MockitoAnnotations.initMocks(this);
		service = new DefinitionPropertyValidationService();

		when(testApplicableValidatorNoError.isApplicable(context)).thenReturn(true);
		when(testApplicableValidatorNoError.validate(context)).thenReturn(Stream.empty());

		when(testApplicableValidatorError.isApplicable(context)).thenReturn(true);
		when(testApplicableValidatorError.validate(context)).thenReturn(Stream.of(error));

		when(testNonApplicableValidator.isApplicable(context)).thenReturn(false);
		when(testNonApplicableValidator.validate(context)).thenReturn(Stream.empty());

		List<PropertyFieldValidator> testValidators = new ArrayList<>();
		testValidators.add(testApplicableValidatorError);
		testValidators.add(testApplicableValidatorNoError);
		testValidators.add(testNonApplicableValidator);

		when(validators.stream()).thenReturn(testValidators.stream());
		Whitebox.setInternalState(service, "validators", validators);
	}

	@Test
	public void validate() {
		List<PropertyValidationError> validations = service.validate(context);
		assertEquals(validations.size(), 1);
		assertTrue(validations.get(0) instanceof CodelistFieldValidationError);
	}
}