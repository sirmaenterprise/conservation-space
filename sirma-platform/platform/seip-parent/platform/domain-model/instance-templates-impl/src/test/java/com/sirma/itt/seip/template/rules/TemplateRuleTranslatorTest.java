package com.sirma.itt.seip.template.rules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Tests the functionality of {@link TemplateRuleTranslator}.
 *
 * @author Vilizar Tsonev
 */
public class TemplateRuleTranslatorTest {

	@InjectMocks
	private TemplateRuleTranslator templateRuleTranslator;

	@Mock
	private CodelistService codelistService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(systemConfiguration.getSystemLanguage()).thenReturn("en");
	}

	@Test
	public void should_Properly_Translate_Single_Value_Codelists() {
		PropertyDefinition functional = constructDefinitionField("functional", "Functional", true);
		PropertyDefinition department = constructDefinitionField("department", "Department", true);
		PropertyDefinition filterCodelist = constructDefinitionField("filterCodelist", "Filter Codelist", true);
		havingDefinitionWithFields(functional, department, filterCodelist);

		withCodeValue("ENG", "Engineering department");
		withCodeValue("EDG", "Electrical Design Group");
		withCodeValue("AD210001", "Paid Fee");

		String inputRule = "department == \"ENG\" && functional == \"EDG\" && filterCodelist == \"AD210001\"";
		String expectedDescription = "Department: Engineering department AND Functional: Electrical Design Group AND Filter Codelist: Paid Fee";

		String actual = templateRuleTranslator.translate(inputRule, "sampleType");

		assertEquals(expectedDescription, actual);
	}

	@Test
	public void should_Properly_Translate_Multi_Value_Codelists() {
		PropertyDefinition functional = constructDefinitionField("functional", "Functional", true);
		PropertyDefinition department = constructDefinitionField("department", "Department", true);
		havingDefinitionWithFields(functional, department);

		withCodeValue("ENG", "Engineering department");
		withCodeValue("HMR", "Human Resources Department");
		withCodeValue("INF", "Infrastructure Department");
		withCodeValue("EDG", "Electrical Design Group");
		withCodeValue("AED", "Advanced Electrical Design Group");

		String inputRule = "(department == \"ENG\" || department == \"HMR\" || department == \"INF\") && (functional == \"EDG\" || functional == \"AED\")";
		String expectedDescription = "Department: (Engineering department, Human Resources Department, Infrastructure Department) AND Functional: (Electrical Design Group, Advanced Electrical Design Group)";

		String actual = templateRuleTranslator.translate(inputRule, "sampleType");

		assertEquals(expectedDescription, actual);
	}

	@Test
	public void should_Properly_Translate_Boolean_Properties() {
		PropertyDefinition primary = constructDefinitionField("primary", "Primary", false);
		PropertyDefinition someBoolean = constructDefinitionField("someBoolean", "Some Boolean", false);
		PropertyDefinition anotherBoolean = constructDefinitionField("anotherBoolean", "Another Boolean", false);
		havingDefinitionWithFields(primary, someBoolean, anotherBoolean);

		String inputRule = "primary == true && someBoolean == false && anotherBoolean == true";
		String expectedDescription = "Primary: true AND Some Boolean: false AND Another Boolean: true";

		String actual = templateRuleTranslator.translate(inputRule, "sampleType");

		assertEquals(expectedDescription, actual);
	}

	@Test
	public void should_Properly_Translate_Mixed_Boolean_And_Codelist_Properties() {
		PropertyDefinition functional = constructDefinitionField("functional", "Functional", true);
		PropertyDefinition department = constructDefinitionField("department", "Department", true);
		PropertyDefinition primary = constructDefinitionField("primary", "Primary", false);
		PropertyDefinition someBoolean = constructDefinitionField("someBoolean", "Some Boolean", false);

		withCodeValue("BA", "Business Analysis Department");
		withCodeValue("ENG", "Engineering department");
		withCodeValue("EDG", "Electrical Design Group");
		withCodeValue("AED", "Advanced Electrical Design Group");

		havingDefinitionWithFields(functional, department, primary, someBoolean);

		String inputRule = "primary == true && (department == \"BA\" || department == \"ENG\")"
				+ " && (functional == \"EDG\" || functional == \"AED\") && someBoolean == false";
		String expectedDescription = "Primary: true AND Department: (Business Analysis Department, Engineering department) "
				+ "AND Functional: (Electrical Design Group, Advanced Electrical Design Group) AND Some Boolean: false";

		String actual = templateRuleTranslator.translate(inputRule, "sampleType");

		assertEquals(expectedDescription, actual);
	}

	@Test(expected = EmfApplicationException.class)
	public void should_Throw_Exception_If_Rule_Field_Not_Present_In_Definition() {
		DefinitionModel definition = mock(DefinitionModel.class);
		when(definition.getField(eq("department"))).thenReturn(Optional.empty());
		when(definitionService.find(anyString())).thenReturn(definition);

		String inputRule = "(department == \"BA\" || department == \"ENG\")"
				+ " && (functional == \"EDG\" || functional == \"AED\")";
		templateRuleTranslator.translate(inputRule, "sampleType");
	}

	private static PropertyDefinition constructDefinitionField(String identifier, String label, boolean fromCodelist) {
		PropertyDefinition field = mock(PropertyDefinition.class);
		when(field.getIdentifier()).thenReturn(identifier);
		when(field.getLabel()).thenReturn(label);
		if (fromCodelist) {
			when(field.getCodelist()).thenReturn(Integer.valueOf(10));
		} else {
			when(field.getCodelist()).thenReturn(null);
		}
		return field;
	}

	private void havingDefinitionWithFields(PropertyDefinition... properties) {
		DefinitionModel definition = mock(DefinitionModel.class);

		for (PropertyDefinition propertyDefinition : properties) {
			when(definition.getField(eq(propertyDefinition.getIdentifier())))
					.thenReturn(Optional.of(propertyDefinition));
		}
		when(definitionService.find(anyString())).thenReturn(definition);
	}

	private void withCodeValue(String code, String label) {
		CodeValue codeValue = mock(CodeValue.class);
		when(codeValue.getDescription(any())).thenReturn(label);
		when(codelistService.getCodeValue(any(), eq(code))).thenReturn(codeValue);
	}
}
