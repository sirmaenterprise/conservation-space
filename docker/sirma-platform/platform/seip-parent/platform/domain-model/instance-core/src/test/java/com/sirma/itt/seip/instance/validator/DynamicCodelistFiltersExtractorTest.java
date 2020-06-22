package com.sirma.itt.seip.instance.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableSet;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;

/**
 * Tests for {@link DynamicCodelistFiltersExtractor}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 08/11/2017
 */
public class DynamicCodelistFiltersExtractorTest {

	@Mock
	private DefinitionModel definition;

	@Mock
	private Instance instance;
	
	@InjectMocks
	private DynamicCodelistFiltersExtractor dynamicCodelistFiltersExtractor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		stubDefinitionModel();
	}

	@Test
	public void testGetDynamicClFiltersExtraction() throws Exception {
		Map<String, DynamicCodeListFilter> dynamicClFilters = dynamicCodelistFiltersExtractor
				.getDynamicClFilters(definition, instance);
		assertEquals(dynamicClFilters.size(), 2);
		assertTrue(dynamicClFilters.keySet().containsAll(ImmutableSet.of("secondField", "firstField")));

		// validate first filter.
		DynamicCodeListFilter firstFilter = dynamicClFilters.get("firstField");
		assertTrue(firstFilter.isFilterValid());
		assertEquals("first-filter-source", firstFilter.getFilterSource());
		assertEquals("firstField", firstFilter.getReRenderFieldName());
		assertEquals(true, firstFilter.isInclusive());

		// validate second filter.
		DynamicCodeListFilter secondFilter = dynamicClFilters.get("secondField");
		assertTrue(secondFilter.isFilterValid());
		assertEquals("second-filter-source", secondFilter.getFilterSource());
		assertEquals("secondField", secondFilter.getReRenderFieldName());
		assertEquals(false, secondFilter.isInclusive());
	}

	/**
	 * Mocks the definitions model. Adds two property definitions, one for cl
	 * and one more simple string field.
	 */
	private void stubDefinitionModel() {
		// Mock a first definition property, which is a CL field.
		PropertyDefinition firstField = mock(PropertyDefinition.class);
		when(firstField.getName()).thenReturn("firstField");
		when(instance.get("some-field")).thenReturn("CL_VALUE");
		when(firstField.getCodelist()).thenReturn(200);
		ControlDefinition firstFieldControlDefinition = Mockito.mock(ControlDefinition.class);
		when(firstFieldControlDefinition.getIdentifier()).thenReturn("RELATED_FIELDS");
		when(firstField.getControlDefinition()).thenReturn(firstFieldControlDefinition);
		mockControlDefinition(firstFieldControlDefinition, "firstFilter", "some-field", "first-filter-source", "true");

		// Mock a second definition property, which is a CL field.
		PropertyDefinition secondField = mock(PropertyDefinition.class);
		when(secondField.getName()).thenReturn("secondField");
		when(instance.get("some-other-field")).thenReturn("CL_VALUE");
		when(secondField.getCodelist()).thenReturn(200);
		ControlDefinition secondFieldControlDefinition = Mockito.mock(ControlDefinition.class);
		when(secondFieldControlDefinition.getIdentifier()).thenReturn("RELATED_FIELDS");
		when(secondField.getControlDefinition()).thenReturn(secondFieldControlDefinition);
		mockControlDefinition(secondFieldControlDefinition, "secondfilter", "some-other-field", "second-filter-source",
				"false");

		// Mock a third property.
		PropertyDefinition thirdField = mock(PropertyDefinition.class);
		when(thirdField.getName()).thenReturn("stringField");
		when(instance.get("stringField")).thenReturn("some-string-value");

		// add properties
		final List<PropertyDefinition> properties = new ArrayList<>();
		properties.add(firstField);
		properties.add(secondField);
		properties.add(thirdField);

		when(definition.fieldsStream()).then(a -> properties.stream());
	}

	/**
	 * Mocks the control definitions for cl fields.
	 */
	private void mockControlDefinition(ControlDefinition controlDefinition, String type, String reRenderValue,
			String filterSourceValue, String inclusiveValue) {
		List<ControlParam> parameters = new ArrayList<>();
		parameters.addAll(mockFilter(type, reRenderValue, filterSourceValue, inclusiveValue));
		when(controlDefinition.getControlParams()).thenReturn(parameters);
	}

	private List<ControlParam> mockFilter(String type, String reRenderValue, String filterSourceValue,
			String inclusiveValue) {
		ControlParam reRender = mock(ControlParam.class);
		when(reRender.getType()).thenReturn(type);
		when(reRender.getName()).thenReturn("RERENDER");
		when(reRender.getValue()).thenReturn(reRenderValue);

		ControlParam filterSource = mock(ControlParam.class);
		when(filterSource.getType()).thenReturn(type);
		when(filterSource.getName()).thenReturn("FILTER_SOURCE");
		when(filterSource.getValue()).thenReturn(filterSourceValue);

		ControlParam inclusive = mock(ControlParam.class);
		when(inclusive.getType()).thenReturn(type);
		when(inclusive.getIdentifier()).thenReturn("filterInclusive");
		when(inclusive.getName()).thenReturn("INCLUSIVE");
		when(inclusive.getValue()).thenReturn(inclusiveValue);

		List<ControlParam> parameters = new ArrayList<>();
		parameters.add(reRender);
		parameters.add(filterSource);
		parameters.add(inclusive);
		return parameters;
	}
}