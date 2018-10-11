package com.sirma.itt.seip.instance.validator.validators;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.instance.validator.errors.SingleValueFieldValidationError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link SingleValueFieldValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 25/01/2018
 */
public class SingleValueFieldValidatorTest {

	@InjectMocks
	private SingleValueFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private SingleValueFieldValidationError error;
	@Mock
	private Instance instance;
	@Mock
	private LabelProvider provider;

	@Before
	public void setup() {
		cut = new SingleValueFieldValidator();
		MockitoAnnotations.initMocks(this);

		when(provider.getValue(anyString())).thenReturn("%s");

		when(context.getInstance()).thenReturn(instance);
		when(context.getPropertyDefinition()).thenReturn(property);

		when(errorBuilder.buildSingleValueError(any())).thenReturn(error);
	}

	@Test
	public void testValidate_success_nullValue() {
		when(context.getValue()).thenReturn(null);
		assertEquals(0, cut.validate(context).count());
	}

	@Test
	public void testValidate_success_singleValue() {
		when(context.getValue()).thenReturn((Serializable) Collections.singleton("singleValue"));
		assertEquals(0, cut.validate(context).count());
	}

	@Test
	public void testValidate_validationFails() {
		when(context.getValue()).thenReturn((Serializable) Arrays.asList("firstValue", "secondValue"));
		assertEquals(1, cut.validate(context).count());
	}

	@Test
	public void testIsApplicable_notApplicable() {
		when(property.isMultiValued()).thenReturn(Boolean.TRUE);
		assertFalse(cut.isApplicable(context));
	}

	@Test
	public void testIsApplicable_applicable() {
		when(property.isMultiValued()).thenReturn(Boolean.FALSE);
		assertTrue(cut.isApplicable(context));
	}
}