package com.sirma.itt.seip.instance.validator.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.instance.validator.errors.NumericFieldValidationError;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * Tests for {@link NumericFieldsValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class NumericFieldsValidatorTest {

	@InjectMocks
	private NumericFieldsValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition type;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuider;
	@Mock
	private NumericFieldValidationError error;
	@Mock
	private Instance instance;
	@Mock
	private LabelProvider provider;

	@Before
	public void setup() {
		cut = new NumericFieldsValidator();
		MockitoAnnotations.initMocks(this);

		when(provider.getValue(anyString())).thenReturn("%s");

		when(context.getInstance()).thenReturn(instance);
		when(context.getPropertyDefinition()).thenReturn(property);

		when(property.getDataType()).thenReturn(type);

		when(errorBuider.buildNumericFieldError(any())).thenReturn(error);
		when(errorBuider.buildNumericFieldError(any(), anyString())).thenReturn(error);

		RegExGenerator generator = new RegExGenerator(provider::getValue);
		Whitebox.setInternalState(cut, "regExGenerator", generator);
	}

	@Test
	public void validate_int_success() {
		when(type.getName()).thenReturn("int");
		when(property.getType()).thenReturn("n..10");
		when(context.getValue()).thenReturn(123);

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_long_pass() {
		when(type.getName()).thenReturn("long");
		when(property.getType()).thenReturn("n..3");
		when(context.getValue()).thenReturn(123L);

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_float_success() {
		when(type.getName()).thenReturn("float");
		when(property.getType()).thenReturn("n..3");
		when(context.getValue()).thenReturn(123f);

		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_double_notPass() {
		when(type.getName()).thenReturn("double");
		when(property.getType()).thenReturn("n..100");
		when(context.getValue()).thenReturn(123.123d);

		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_smallDouble_passes() {
		when(type.getName()).thenReturn("double");
		when(property.getType()).thenReturn("n..10,5");
		when(context.getValue()).thenReturn(0.00005d);

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_NaN() {
		when(type.getName()).thenReturn("double");
		when(property.getType()).thenReturn("n..100");
		when(context.getValue()).thenReturn(new Date());

		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void isApplicable_int() {
		when(type.getName()).thenReturn("int");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void isApplicable_long() {
		when(type.getName()).thenReturn("long");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void isApplicable_float() {
		when(type.getName()).thenReturn("float");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void isApplicable_double() {
		when(type.getName()).thenReturn("double");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}
}