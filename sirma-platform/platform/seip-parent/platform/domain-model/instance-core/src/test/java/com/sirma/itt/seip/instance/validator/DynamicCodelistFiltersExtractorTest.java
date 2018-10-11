package com.sirma.itt.seip.instance.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
	@Mock
	private ControlDefinition controlDefinition;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		stubDefinitionModel();
	}

	@Test
	public void testGetDynamicClFiltersExtraction() throws Exception {
		Map<String, DynamicCodeListFilter> dynamicClFilters = DynamicCodelistFiltersExtractor.getDynamicClFilters(
				definition, instance);
		assertEquals(dynamicClFilters.size(), 2);
		assertTrue(dynamicClFilters.keySet().containsAll(ImmutableSet.of("some-other-field", "some-field")));
		// validate first filter.
		DynamicCodeListFilter firstFilter = dynamicClFilters.get("some-field");
		assertTrue(firstFilter.isFilterValid());
		assertEquals("first-filter-source", firstFilter.getFilterSource());
		assertEquals("some-field", firstFilter.getReRenderFieldName());
		assertEquals(true, firstFilter.isInclusive());
		// validate second filter.
		DynamicCodeListFilter secondFilter = dynamicClFilters.get("some-other-field");
		assertTrue(secondFilter.isFilterValid());
		assertEquals("second-filter-source", secondFilter.getFilterSource());
		assertEquals("some-other-field", secondFilter.getReRenderFieldName());
		assertEquals(false, secondFilter.isInclusive());
	}

	/**
	 * Mocks the definitions model. Adds two property definitions, one for cl
	 * and one more simple string field.
	 */
	private void stubDefinitionModel() {
		// Mock a first definition property, which is a CL field.
		PropertyDefinition clProperty = mock(PropertyDefinition.class);
		when(clProperty.getName()).thenReturn("clField");
		when(instance.get("clField")).thenReturn("CL_VALUE");
		when(clProperty.getCodelist()).thenReturn(200);
		when(controlDefinition.getIdentifier()).thenReturn("RELATED_FIELDS");
		when(clProperty.getControlDefinition()).thenReturn(controlDefinition);
		mockControlDefinition();

		// Mock a second property.
		PropertyDefinition stringProperty = mock(PropertyDefinition.class);
		when(stringProperty.getName()).thenReturn("stringField");
		when(instance.get("stringField")).thenReturn("some-string-value");

		// add properties
		final List<PropertyDefinition> properties = new ArrayList<>();
		properties.add(clProperty);
		properties.add(stringProperty);

		when(definition.fieldsStream()).then(a -> properties.stream());
	}

	/**
	 * Mocks the control definitions for cl fields.
	 */
	private void mockControlDefinition() {
		List<ControlParam> parameters = new ArrayList<>();
		parameters.addAll(mockFilter("firstFilter", "some-field", "first-filter-source", "true"));
		parameters.addAll(mockFilter("secondfilter", "some-other-field", "second-filter-source", "false"));
		when(controlDefinition.getControlParams()).thenReturn(parameters);
	}

	private List<ControlParam> mockFilter(String type, String reRenderValue, String filterSourceValue, String
			inclusiveValue) {
		ControlParam reRender = mock(ControlParam.class);
		when(reRender.getType()).thenReturn(type);
		when(reRender.getName()).thenReturn("RERENDER");
		when(reRender.getValue()).thenReturn(reRenderValue);

		ControlParam filterSource = mock(ControlParam.class);
		when(filterSource.getType()).thenReturn(type);
		when(filterSource.getName()).thenReturn("FILTER_SOURCE");
		when(filterSource.getValue()).thenReturn(filterSourceValue);
		when(instance.get("clField")).thenReturn("clValue");

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