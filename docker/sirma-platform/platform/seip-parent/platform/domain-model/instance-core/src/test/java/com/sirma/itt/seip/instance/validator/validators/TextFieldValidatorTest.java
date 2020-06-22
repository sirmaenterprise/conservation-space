package com.sirma.itt.seip.instance.validator.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

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
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.instance.validator.errors.TextFieldValidationError;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * Test for {@link TextFieldValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class TextFieldValidatorTest {

	@InjectMocks
	private TextFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private DataTypeDefinition type;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private TextFieldValidationError error;
	@Mock
	private Instance instance;
	@Mock
	private LabelProvider provider;

	@Before
	public void setup() {
		cut = new TextFieldValidator();
		MockitoAnnotations.initMocks(this);

		when(provider.getValue(anyString())).thenReturn("%s");

		when(context.getInstance()).thenReturn(instance);
		when(context.getPropertyDefinition()).thenReturn(property);

		when(property.getDataType()).thenReturn(type);

		when(errorBuilder.buildTextFieldError(any(), any())).thenReturn(error);
		when(errorBuilder.buildNumericFieldError(any(), anyString())).thenReturn(error);

		RegExGenerator generator = new RegExGenerator(provider::getValue);
		Whitebox.setInternalState(cut, "regExGenerator", generator);
	}

	@Test
	public void validate_success() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..30");
		when(context.getValue()).thenReturn("some text to validate");
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_success_multiple_value() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..30");
		when(property.isMultiValued()).thenReturn(true);
		Collection<Serializable> values = new ArrayList<>();
		values.add("test one");
		values.add("test two");
		values.add("!@#");
		when(context.getValue()).thenReturn((Serializable) values);
		assertFalse(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_fail_multiple_value_with_null() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..30");
		when(property.isMultiValued()).thenReturn(true);
		Collection<Serializable> values = new ArrayList<>();
		values.add("test one");
		values.add(null);
		when(context.getValue()).thenReturn((Serializable) values);
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_fail_multiple_value() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..3");
		when(property.isMultiValued()).thenReturn(true);
		Collection<Serializable> values = new ArrayList<>();
		values.add("test one");
		values.add("qw");
		when(context.getValue()).thenReturn((Serializable) values);
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_fail_multiple_value_with_multilanguage() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..3");
		when(property.isMultiValued()).thenReturn(true);
		Collection<Serializable> values = new ArrayList<>();
		values.add("test one");
		values.add("qw");
		MultiLanguageValue multiLanguageTitle = mock(MultiLanguageValue.class);
		when(multiLanguageTitle.getAllValues()).thenReturn(Stream.of("title1", "title2"));
		values.add(multiLanguageTitle);

		when(context.getValue()).thenReturn((Serializable) values);
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void validate_single_value_fail() throws Exception {
		when(type.getName()).thenReturn("text");
		when(property.getType()).thenReturn("an..3");
		when(context.getValue()).thenReturn("!@#$%^&*(()");
		assertTrue(cut.validate(context).findFirst().isPresent());
	}

	@Test
	public void isApplicable_cls_field() throws Exception {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("text");
		when(property.getCodelist()).thenReturn(100);

		assertFalse(cut.isApplicable(context));
	}

	@Test
	public void isApplicable() throws Exception {
		when(property.getDataType()).thenReturn(type);
		when(type.getName()).thenReturn("text");
		when(context.getValue()).thenReturn(mock(Serializable.class));
		assertTrue(cut.isApplicable(context));
	}
}