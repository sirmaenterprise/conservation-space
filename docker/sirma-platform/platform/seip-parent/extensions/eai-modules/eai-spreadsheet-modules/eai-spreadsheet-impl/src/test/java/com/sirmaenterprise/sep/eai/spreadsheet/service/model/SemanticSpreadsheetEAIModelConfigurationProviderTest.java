package com.sirmaenterprise.sep.eai.spreadsheet.service.model;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants.LOCALE_BG;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;

/**
 * Test for {@link SemanticSpreadsheetEAIModelConfigurationProvider}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SemanticSpreadsheetEAIModelConfigurationProviderTest {

	@Mock
	private CodelistService codelistService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@InjectMocks
	private SemanticSpreadsheetEAIModelConfigurationProvider provider;

	@Test
	public void testProvideModel() throws Exception {
		List<DefinitionModel> definitions = Arrays.asList(
				mockDefinitionModel(mock(ClassInstance.class), "type1", mockPropertyDefinition(UNIQUE_IDENTIFIER, null),
						mockPropertyDefinition(TYPE, 1), mockPropertyDefinition(SEMANTIC_TYPE, null),
						mockPropertyDefinition(TITLE, null)),
				mockDefinitionModel(mock(ClassInstance.class), "type2", mockPropertyDefinition(UNIQUE_IDENTIFIER, null),
						mockPropertyDefinition(TYPE, 2), mockPropertyDefinition(DESCRIPTION, null),
						mockPropertyDefinition(SEMANTIC_TYPE, null), mockPropertyDefinition(TITLE, null)));

		when(definitionService.getAllDefinitions()).thenReturn(definitions.stream());
		ModelConfiguration provideModel = provider.provideModel();
		assertTrue(provideModel.isSealed());
		assertEquals(2, provideModel.getEntityTypes().size());
		EntityType first = provideModel.getTypeByDefinitionId("type1");
		assertNotNull(first);
		EntityType second = provideModel.getTypeByDefinitionId("type2");
		assertNotNull(second);
		assertEquals("type1", first.getIdentifier());
		assertEquals(4, first.getProperties().size());
		assertEquals("type2", second.getIdentifier());
		assertEquals(5, second.getProperties().size());

		assertEquals(2, first.getMappings().size());
		assertEquals(Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("type1val", "type1ст-ст"))),
				first.getMappings());
		assertEquals("emf:type1", first.getUri());
		assertEquals("type1", first.getIdentifier());

		assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(UNIQUE_IDENTIFIER, TYPE, SEMANTIC_TYPE, TITLE),
				first.getProperties().stream().map(EntityProperty::getPropertyId).collect(Collectors.toList())));
	}

	private DefinitionModel mockDefinitionModel(ClassInstance type1Class, String id, PropertyDefinition... properties) {
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionModel.getIdentifier()).thenReturn(id);
		when(definitionModel.fieldsStream()).thenAnswer(invocation -> Stream.of(properties));
		for (PropertyDefinition propertyDefinition : properties) {
			when(definitionModel.getField(eq(propertyDefinition.getIdentifier())))
					.thenReturn(Optional.of(propertyDefinition));
		}
		Map<String, PropertyDefinition> collect = new HashMap<>(Stream.of(properties).collect(
				Collectors.toMap(prop -> prop.getName(), java.util.function.Function.identity())));

		when(definitionModel.getFieldsAsMap()).thenReturn(collect);

		String uri = "emf:" + id;
		when(definitionModel.getFieldsAsMap().get(SEMANTIC_TYPE).getDefaultValue()).thenReturn(uri);
		CodeValue codeValue = mock(CodeValue.class);
		when(codeValue.getDescription(eq(ENGLISH))).thenReturn(id + "val");
		when(codeValue.getDescription(eq(LOCALE_BG))).thenReturn(id + "ст-ст");

		PropertyDefinition propertyDefinition = definitionModel.getFieldsAsMap().get(TYPE);

		when(codelistService.getCodeValue(eq(propertyDefinition.getCodelist()), eq(null))).thenReturn(codeValue);

		when(type1Class.getId()).thenReturn(uri);
		when(semanticDefinitionService.getClassInstance(eq(uri))).thenReturn(type1Class);
		return definitionModel;
	}

	private static PropertyDefinition mockPropertyDefinition(String id, Integer codelist) {
		PropertyDefinition property = mock(PropertyDefinition.class);
		String uri = "emf:" + id;
		when(property.getUri()).thenReturn(uri);
		when(property.getName()).thenReturn(id);
		when(property.getIdentifier()).thenReturn(id);
		when(property.isMandatory()).thenReturn(true);
		when(property.getCodelist()).thenReturn(codelist);
		DataTypeDefinition dataType = mock(DataTypeDefinition.class);
		when(dataType.getName()).thenReturn("an..0");
		when(property.getDataType()).thenReturn(dataType);
		return property;
	}
}