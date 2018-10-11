package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.BooleanFieldValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link BooleanFieldValidator} class.
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class BooleanFieldValidatorTest {

	@InjectMocks
	private BooleanFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition type;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private BooleanFieldValidationError error;

	@Before
	public void setup() {
		cut = new BooleanFieldValidator();
		MockitoAnnotations.initMocks(this);
		context.setPropertyDefinition(property);
		when(context.getPropertyDefinition()).thenReturn(property);
		when(errorBuilder.buildBooleanFieldError(any())).thenReturn(error);
	}

	@Test
	public void test_validate_success() throws Exception {
		when(context.getValue()).thenReturn(true);
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_fail() throws Exception {
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_isApplicable_passes() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("boolean");
		when(context.getValue()).thenReturn(Serializable.class);

		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_fails() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("fail!");

		assertFalse(cut.isApplicable(context));
	}

}