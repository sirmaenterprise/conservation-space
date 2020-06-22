package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.DateTimeFieldValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DateTimeFieldValidator} class.
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class DateTimeFieldValidatorTest {

	@InjectMocks
	private DateTimeFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition type;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private DateTimeFieldValidationError error;

	@Before
	public void setup() {
		cut = new DateTimeFieldValidator();
		MockitoAnnotations.initMocks(this);
		when(context.getPropertyDefinition()).thenReturn(property);
		when(errorBuilder.buildDateTimeFieldError(any())).thenReturn(error);
	}

	@Test
	public void test_validate_success() throws Exception {
		when(context.getValue()).thenReturn(new Date());
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_fail() throws Exception {
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_isApplicable_with_datetime() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("datetime");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_with_date() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("date");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_fails() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("Fail!");
		assertFalse(cut.isApplicable(context));
	}

}