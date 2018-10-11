package com.sirma.itt.seip.definition.compile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.MutableDefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link DefinitionCompilerHelper}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 28/08/2017
 */
public class DefinitionCompilerHelperTest {

	@InjectMocks
	private DefinitionCompilerHelper cut;
	@Mock
	private ExpressionsManager manager;
	@Mock
	private MutableDefinitionService mutableDefinitionService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	private GenericDefinition definitionMock;

	private List<PropertyDefinition> propertyDefinitions;

	@Before
	public void init() {
		cut = new DefinitionCompilerHelper();
		MockitoAnnotations.initMocks(this);

		definitionMock = mock(GenericDefinition.class);
		propertyDefinitions = mockProperties();

		when(definitionMock.fieldsStream()).thenReturn(propertyDefinitions.stream());
	}

	@Test
	public void prepareDefaultValueSuggests() throws Exception {
		cut.prepareDefaultValueSuggests(definitionMock);

		propertyDefinitions.forEach(property -> {
			ControlDefinition controlDefinition = property.getControlDefinition();
			if (controlDefinition != null) {
				List<ControlParam> controlParams = controlDefinition.getControlParams();
				assertEquals(5, controlParams.size());

				long countPropertyBindings = controlParams.stream()
						.filter(param -> param.getIdentifier().equals("propertyNameBinding"))
						.peek(param -> assertTrue(
								"emf:references.hasAttachment".equals(param.getName()) || "emf:status".equals(
										param.getName())))
						.count();
				assertEquals(2, countPropertyBindings);

				long countFunctions = controlParams.stream()
						.filter(param -> param.getIdentifier().equals("function"))
						.peek(param -> {
							assertEquals("${expression${subExpression1${subExpression2}}something}", param.getValue());
							assertEquals("label", param.getName());
						})
						.count();
				assertEquals(1, countFunctions);
			}
		});
	}

	@Test
	public void should_SaveProperties() {
		DefinitionMock definitionMock = new DefinitionMock("userDefinition");
		definitionMock.setFields(createProperties("firstName", "lastName", "email"));

		cut.saveProperties(definitionMock, null);

		verify(mutableDefinitionService, times(3)).savePropertyIfChanged(any(), any());
	}

	@Test
	public void should_SavePropertiesFromRegions() {
		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("systemData");
		region.setFields(createProperties("email", "department"));

		DefinitionMock definitionMock = new DefinitionMock("userDefinition");
		definitionMock.setFields(createProperties("firstName", "lastName", "email"));
		definitionMock.setRegions(Collections.singletonList(region));

		cut.saveProperties(definitionMock, new DefinitionMock("userDefinition"));

		verify(mutableDefinitionService, times(5)).savePropertyIfChanged(any(), any());
	}

	private static List<PropertyDefinition> createProperties(String... properties) {
		List<PropertyDefinition> propertiesDefinitions = new ArrayList<>();
		for (String name : properties) {
			propertiesDefinitions.add(createPropertyDefinition(name));
		}
		return propertiesDefinitions;
	}

	private static PropertyDefinition createPropertyDefinition(String name) {
		PropertyDefinitionProxy propertyDefinition = new PropertyDefinitionProxy();
		propertyDefinition.setName(name);
		return propertyDefinition;
	}

	private List<PropertyDefinition> mockProperties() {
		List<PropertyDefinition> properties = new ArrayList<>();

		// property without control.
		PropertyDefinition firstProperty = mock(PropertyDefinition.class);
		when(firstProperty.getControlDefinition()).thenReturn(null);

		// property with control.
		PropertyDefinition secondProperty = mock(PropertyDefinition.class);
		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setControlParams(mockControlParams());
		when(firstProperty.getControlDefinition()).thenReturn(control);

		properties.add(firstProperty);
		properties.add(secondProperty);
		return properties;
	}

	private List<ControlParam> mockControlParams() {
		List<ControlParam> params = new ArrayList<>();
		// control parameter with suggest.
		ControlParamImpl parameterWithSuggest = new ControlParamImpl();
		parameterWithSuggest.setIdentifier("template");
		parameterWithSuggest.setType("default_value_pattern");
		parameterWithSuggest.setName("template");
		parameterWithSuggest.setValue("Some text $[emf:references.hasAttachment] $[emf:status] "
											  + "{${expression${subExpression1${subExpression2}}something}|label} text.");

		// Some other control parameter.
		ControlParam rangeControlParam = new ControlParamImpl();
		rangeControlParam.setIdentifier("range");

		params.add(parameterWithSuggest);
		params.add(rangeControlParam);

		return params;
	}
}