package com.sirma.itt.seip.instance.validator.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validator.errors.CodelistFieldValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for the {@link CodelistFieldValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class CodelistFieldValidatorTest {

	@InjectMocks
	private CodelistFieldValidator cut;

	@Mock
	private PropertyDefinition property;
	@Mock
	private CodelistService codelistService;
	@Mock
	private FieldValidationContext context;
	@Mock
	private FieldValidationErrorBuilder errorBuilder;
	@Mock
	private CodelistFieldValidationError error;

	@Before
	public void setup() {
		cut = new CodelistFieldValidator();
		MockitoAnnotations.initMocks(this);

		when(context.getPropertyDefinition()).thenReturn(property);
		when(errorBuilder.buildBooleanFieldError(any())).thenReturn(error);
		when(errorBuilder.buildCodelistFieldError(any(), any(), any())).thenReturn(error);

		// mock cls
		when(context.getValue()).thenReturn("CL");
		when(property.getCodelist()).thenReturn(200);

		Map<String, CodeValue> map = new HashMap<>();
		CodeValue clValue = new CodeValue();
		clValue.setValue("some_value");
		map.put("CL", clValue);

		when(codelistService.getFilteredCodeValues(any(Integer.class), any(String[].class))).thenReturn(map);
		when(codelistService.getFilteredCodeValues(any(Integer.class))).thenReturn(map);
		when(codelistService.filterCodeValues(any(Integer.class),
				any(Boolean.class), any(String.class), any())).thenReturn(map);
	}

	@Test
	public void validate_success() {
		assertFalse(cut.validate(context).findFirst().isPresent());
		verify(errorBuilder, times(0)).buildCodelistFieldError(any(), any(), any());
	}

	@Test
	public void validate_success_filter() {
		when(context.getValue()).thenReturn("CL");
		when(property.getCodelist()).thenReturn(200);
		when(property.getFilters()).thenReturn(new HashSet<>(Collections.singletonList("aFilter")));

		assertFalse(cut.validate(context).findFirst().isPresent());
		verify(errorBuilder, times(0)).buildCodelistFieldError(any(), any(), any());
	}

	@Test
	public void validate_success_dynamicFilter() {
		when(context.getValue()).thenReturn("CL");
		when(property.getCodelist()).thenReturn(200);
		when(property.getName()).thenReturn("field");

		Map<String, DynamicCodeListFilter> dynamicFilters = new HashMap<>();
		DynamicCodeListFilter clFilter = new DynamicCodeListFilter();
		clFilter.setFilterSource("src");
		clFilter.setReRenderFieldName("field");
		clFilter.setValues(Collections.singletonList("CL3"));
		clFilter.setInclusive(Boolean.TRUE);
		dynamicFilters.put("field", clFilter);
		when(context.getDynamicClFilters()).thenReturn(dynamicFilters);

		assertFalse(cut.validate(context).findFirst().isPresent());
		verify(errorBuilder, times(0)).buildCodelistFieldError(any(), any(), any());
	}

	@Test
	public void validate_error() {
		when(property.getCodelist()).thenReturn(200);
		when(context.getValue()).thenReturn("CL_INVALID");

		assertTrue(cut.validate(context).findFirst().isPresent());
		verify(errorBuilder, times(1)).buildCodelistFieldError(any(), any(), any());
	}

	@Test
	public void validate_error_invalid_type() {
		when(property.getCodelist()).thenReturn(200);
		when(context.getValue()).thenReturn(new Date());

		assertTrue(cut.validate(context).findFirst().isPresent());
		verify(errorBuilder, times(1)).buildCodelistFieldError(any(), any(), any());
	}

	@Test
	public void isApplicable_true() {
		when(property.getCodelist()).thenReturn(200);
		assertTrue(cut.isApplicable(context));
	}

	@Test
	public void isApplicable_false() {
		when(property.getCodelist()).thenReturn(null);
		assertFalse(cut.isApplicable(context));
	}
}