package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.instance.validator.errors.MandatoryFieldValidationError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MandatoryPropertiesValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class MandatoryPropertiesValidatorTest {

	@InjectMocks
	private MandatoryPropertiesValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private MandatoryFieldValidationError error;
	@Mock
	private Instance instance;

	@Before
	public void setup() {
		cut = new MandatoryPropertiesValidator();
		MockitoAnnotations.initMocks(this);

		when(context.getInstance()).thenReturn(instance);
		when(context.getPropertyDefinition()).thenReturn(property);
		when(property.getName()).thenReturn("mandatory");
		when(errorBuilder.buildMandatoryFieldError(Mockito.any())).thenReturn(error);
	}
	
	@Test
	public void test_validate_success() {
		when(instance.get(Mockito.anyString())).thenReturn("something");
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_success_multivalued() {
		when(property.isMandatory()).thenReturn(true);
		Collection<Serializable> values = new ArrayList<>();
		values.add("mandatory1");
		values.add("mandatory2");
		when(context.getValue()).thenReturn((Serializable) values);
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_error_isMandatory() {
		when(property.isMandatory()).thenReturn(true);
		when(context.getValue()).thenReturn("something");
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_error_mandatoryFromCondition() throws Exception {
		Set<String> mandatoryFields = new HashSet<>();
		mandatoryFields.add("mandatory");
		when(context.getMandatoryFields()).thenReturn(mandatoryFields);

		when(instance.get(Mockito.anyString())).thenReturn(null);
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_isApplicable_isMandatory() {
		when(property.isMandatory()).thenReturn(true);
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_mandatoryFromContext() {
		when(context.getMandatoryFields()).thenReturn(Collections.singleton("mandatory"));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_false() {
		assertFalse(cut.isApplicable(context));
	}

	@Test
	public void test_success_optional_mandatory() {
		Set<String> optionalFields = new HashSet<>();
		optionalFields.add("mandatory");
		when(context.getOptionalFields()).thenReturn(optionalFields);
		Set<String> mandatoryFields = new HashSet<>();
		mandatoryFields.add("mandatory");
		when(context.getMandatoryFields()).thenReturn(mandatoryFields);
		assertFalse(cut.validate(context).findFirst().isPresent());
	}
}