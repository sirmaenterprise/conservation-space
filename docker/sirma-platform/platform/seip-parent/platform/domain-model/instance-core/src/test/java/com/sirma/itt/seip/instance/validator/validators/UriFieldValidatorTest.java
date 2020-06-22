package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.instance.validator.errors.UriFieldValidationError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link UriFieldValidator} class.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class UriFieldValidatorTest {

	@InjectMocks
	private UriFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition type;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private UriFieldValidationError error;
	@Mock
	private Instance instance;
	@Mock
	private TypeConverter converter;

	@Before
	public void setup() {
		cut = new UriFieldValidator();
		MockitoAnnotations.initMocks(this);

		when(context.getInstance()).thenReturn(instance);
		when(context.getPropertyDefinition()).thenReturn(property);
		when(property.getName()).thenReturn("mandatory");
		when(errorBuilder.buildUriFieldError(any())).thenReturn(error);
		when(converter.convert(eq(String.class), anyString())).then(a -> a.getArgumentAt(1, Object.class).toString());
	}

	@Test
	public void test_validate_success_shortUri() throws Exception {
		when(property.isMultiValued()).thenReturn(false);
		when(context.getValue()).thenReturn("emf:something");

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_success_user() throws Exception {
		when(property.isMultiValued()).thenReturn(false);
		when(context.getValue()).thenReturn("emf:GROUP_admin");
		assertFalse(cut.validate(context).findFirst().isPresent());

		when(context.getValue()).thenReturn("emf:ivo-admin");
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_success_fullUri() throws Exception {
		when(property.isMultiValued()).thenReturn(false);
		when(context.getValue()).thenReturn("http://emfsomething/somethingelse1/asd#");

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_fail() throws Exception {
		when(property.isMultiValued()).thenReturn(false);
		when(context.getValue()).thenReturn("123");

		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_multivalued_success() throws Exception {
		when(property.isMultiValued()).thenReturn(true);

		List<String> valuesList = new ArrayList<>();
		valuesList.add("emf:instance");
		valuesList.add("emf:something");
		Serializable values = (Serializable) valuesList;
		when(context.getValue()).thenReturn(values);

		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_validate_multivalued_fail() throws Exception {
		when(property.isMultiValued()).thenReturn(true);

		List<String> valuesList = new ArrayList<>();
		valuesList.add("fail");
		valuesList.add("emf:fail");
		Serializable values = (Serializable) valuesList;
		when(context.getValue()).thenReturn(values);

		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void test_isApplicable_success() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("any");
		when(property.getType()).thenReturn("uri");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void test_isApplicable_without_type() {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("any");
		when(property.getType()).thenReturn("something");

		assertFalse(cut.isApplicable(context));
	}
}