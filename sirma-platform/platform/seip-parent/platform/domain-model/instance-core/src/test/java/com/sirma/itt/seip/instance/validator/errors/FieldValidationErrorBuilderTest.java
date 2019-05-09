package com.sirma.itt.seip.instance.validator.errors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.model.DataType;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;

/**
 * Test for the {@link FieldValidationErrorBuilder}.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class FieldValidationErrorBuilderTest {

	private static final String MESSAGE_LABEL = "message %s";

	@InjectMocks
	private FieldValidationErrorBuilder cut;
	@Mock
	private PropertyDefinition property;
	@Mock
	private DataType type;
	@Mock
	private LabelProvider provider;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		cut = new FieldValidationErrorBuilder();

		Mockito.when(property.getDataType()).thenReturn(type);
		Mockito.when(type.getName()).thenReturn("name");

		Mockito.when(provider.getLabel(Mockito.anyString())).thenReturn(MESSAGE_LABEL);
		Whitebox.setInternalState(cut, "provider", provider);
	}

	@Test
	public void buildUniqueFieldError() {
		String propertyLabel = "label property";
		Mockito.when(property.getLabel()).thenReturn(propertyLabel);
		PropertyValidationError propertyValidationError = cut.buildUniqueValueError(property);
		String[] propertyLabelAsArray = { propertyLabel };
		Assert.assertEquals(String.format(MESSAGE_LABEL, propertyLabelAsArray), propertyValidationError.getMessage());
	}

	@Test
	public void buildBooleanFieldError() {
		PropertyValidationError error = cut.buildBooleanFieldError(property);
		validate(BooleanFieldValidationError.class, error);
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		// run once for default label.
		error = cut.buildBooleanFieldError(property);
		validate(BooleanFieldValidationError.class, error);
	}

	@Test
	public void buildCodelistFieldError() {
		PropertyValidationError error = cut.buildCodelistFieldError(property, "CL101", Collections.emptyMap());
		validate(CodelistFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildCodelistFieldError(property, "CL101", Collections.emptyMap());
		validate(CodelistFieldValidationError.class, error);
	}

	@Test
	public void buildDateTimeFieldError() {
		PropertyValidationError error = cut.buildDateTimeFieldError(property);
		validate(DateTimeFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildDateTimeFieldError(property);
		validate(DateTimeFieldValidationError.class, error);
	}

	@Test
	public void buildMandatoryFieldError() {
		PropertyValidationError error = cut.buildMandatoryFieldError(property);
		validate(MandatoryFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildMandatoryFieldError(property);
		validate(MandatoryFieldValidationError.class, error);
	}

	@Test
	public void buildMandatoryControlParamError() {
		PropertyValidationError error = cut.buildMandatoryControlParamError(property);
		validate(MandatoryControlParamValidationError.class, error);
	}
	
	@Test
	public void buildTextFieldError() {
		PropertyValidationError error = cut.buildTextFieldError(property, "message");
		validate(TextFieldValidationError.class, error);
	}

	@Test
	public void buildUriFieldError() {
		PropertyValidationError error = cut.buildUriFieldError(property);
		validate(UriFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildUriFieldError(property);
		validate(UriFieldValidationError.class, error);
	}

	@Test
	public void buildNumericFieldError_wrongType() {
		PropertyValidationError error = cut.buildNumericFieldError(property);
		validate(NumericFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildNumericFieldError(property);
		validate(NumericFieldValidationError.class, error);
	}

	@Test
	public void buildSingleValueFieldValidationError() {
		PropertyValidationError error = cut.buildSingleValueError(property);
		validate(SingleValueFieldValidationError.class, error);
		// run once for default label.
		Mockito.when(provider.getLabel(Mockito.any())).thenReturn(null);
		error = cut.buildSingleValueError(property);
		validate(SingleValueFieldValidationError.class, error);
	}

	@Test
	public void buildNumericFieldError_wrongFormat() {
		PropertyValidationError error = cut.buildNumericFieldError(property, "message");
		validate(NumericFieldValidationError.class, error);
	}

	private void validate(Class clazz, PropertyValidationError error) {
		assertTrue(clazz.isInstance(error));
		assertTrue(StringUtils.isNotBlank(error.getMessage()));
		assertNotNull(error.getFieldName());
	}

}