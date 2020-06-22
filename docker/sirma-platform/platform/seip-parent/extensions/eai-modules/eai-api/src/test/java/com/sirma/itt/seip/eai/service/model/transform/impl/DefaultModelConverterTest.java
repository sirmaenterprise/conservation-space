package com.sirma.itt.seip.eai.service.model.transform.impl;

import static com.sirma.itt.seip.eai.mock.MockProvider.mockSystem;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Tests {@link DefaultModelConverter}.
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultModelConverterTest {

	private static final String PROP_INTERNAL_TITLE = "seip:title";
	private static final String SYSTEM_ID = "CMS";
	@Mock
	private CodelistService codelistService;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private EAIConfigurationService integrationService;
	@Mock
	private TypeConverter typeConverter;
	private DefaultModelConverter defaultModelConverter;
	private ModelConfiguration modelConfigaration;

	public void setupMocks(DefaultModelConverter defaultModelConverter) throws Exception {
		MockitoAnnotations.initMocks(this);
		this.defaultModelConverter = defaultModelConverter;
		ReflectionUtils.setFieldValue(defaultModelConverter, "definitionService", definitionService);
		ReflectionUtils.setFieldValue(defaultModelConverter, "codelistService", codelistService);
		ReflectionUtils.setFieldValue(defaultModelConverter, "typeConverter", typeConverter);
		ReflectionUtils.setFieldValue(defaultModelConverter, "integrationService", integrationService);

		EAIConfigurationProvider configurationProvider = mockSystem(SYSTEM_ID, Boolean.TRUE, Boolean.TRUE);
		modelConfigaration = new ModelConfiguration();

		EntityType entity = mock(EntityType.class);
		when(entity.getIdentifier()).thenReturn("id");
		modelConfigaration.addEntityType(entity);

		when(configurationProvider.getModelConfiguration())
				.thenReturn(new ConfigurationPropertyMock<>(modelConfigaration));
		when(integrationService.getIntegrationConfiguration(SYSTEM_ID)).thenReturn(configurationProvider);
	}

	@Test(expected = EAIModelException.class)
	public void testConvertSEIPtoExternalPropertyFullWithFail1() throws Exception {
		setupMocks(createConverter());
		defaultModelConverter.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my title", "CO0001");
	}

	@Test(expected = EAIModelException.class)
	public void testConvertSEIPtoExternalPropertyFullWithFail4() throws Exception {
		setupMocks(createConverter());
		defaultModelConverter.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my title", null);
	}

	private static DefaultModelConverter createConverter() {
		return new DefaultModelConverter("CMS") {

			@Override
			protected Serializable convertInternalToExternalValueByCodelist(Integer codelist, Serializable source) {
				Assert.assertEquals(Integer.valueOf(1), codelist);
				Assert.assertEquals("my value", source);
				return "my cl value";
			}

			@Override
			protected Serializable convertExternalToInternalValueByCodelist(Integer codelist, Serializable source) {
				Assert.assertEquals(Integer.valueOf(1), codelist);
				Assert.assertEquals("my cl value", source);
				return "my value";
			}
		};
	}

	@Test(expected = EAIModelException.class)
	public void testConvertSEIPtoExternalPropertyFullWithFail2() throws Exception {
		setupMocks(createConverter());
		EntityType entity = new EntityType();
		entity.setIdentifier("CO0001");
		entity.addProperties(new LinkedList<>());
		EntityProperty titleProp = new EntityProperty();
		entity.getProperties().add(titleProp);
		titleProp.setPropertyId(PROP_INTERNAL_TITLE);
		titleProp.setType("an..1000");
		modelConfigaration.addEntityType(entity);

		// searched by uri
		defaultModelConverter.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my title", "CO0001");
	}

	@Test(expected = EAIModelException.class)
	public void testConvertSEIPtoExternalPropertyFullWithFail3() throws Exception {
		setupMocks(new DefaultModelConverter("CMS") {

			@Override
			protected Serializable convertInternalToExternalValueByCodelist(Integer codelist, Serializable source) {
				return null;
			}

			@Override
			protected Serializable convertExternalToInternalValueByCodelist(Integer codelist, Serializable source) {
				return null;
			}
		});
		EntityType entity = createEntity();
		modelConfigaration.addEntityType(entity);

		// searched by uri
		defaultModelConverter.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my value", "CO0001");
	}

	@Test
	public void testConvertSEIPtoExternalPropertyFullValid1() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		modelConfigaration.addEntityType(entity);

		// searched by uri
		List<Pair<String, Serializable>> convertSEIPtoExternalProperty = defaultModelConverter
				.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my value", "CO0001");
		Assert.assertEquals(1, convertSEIPtoExternalProperty.size());
		Assert.assertEquals("external_title", convertSEIPtoExternalProperty.get(0).getFirst());
		Assert.assertEquals("my cl value", convertSEIPtoExternalProperty.get(0).getSecond());
	}

	@Test
	public void testConvertSEIPtoExternalPropertyFullValid2() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		entity.getProperties().get(0).setCodelist(null);
		modelConfigaration.addEntityType(entity);

		// searched by uri
		List<Pair<String, Serializable>> convertSEIPtoExternalProperty = defaultModelConverter
				.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my value", "CO0001");
		Assert.assertEquals(1, convertSEIPtoExternalProperty.size());
		Assert.assertEquals("external_title", convertSEIPtoExternalProperty.get(0).getFirst());
		Assert.assertEquals("my value", convertSEIPtoExternalProperty.get(0).getSecond());
	}

	@Test
	public void testConvertSEIPtoExternalPropertyFullValid3() throws Exception {
		setupMocks(createConverter());

		EntityType entity1 = createEntity();
		EntityType entity2 = createEntity();
		entity2.setIdentifier("CO002");
		entity2.getProperties().get(0).addMapping(EntityPropertyMapping.AS_DATA, "external_title2");
		modelConfigaration.addEntityType(entity1);
		modelConfigaration.addEntityType(entity2);
		modelConfigaration.seal();
		// searched by uri
		List<Pair<String, Serializable>> convertSEIPtoExternalProperty = defaultModelConverter
				.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my value", null);
		Assert.assertEquals(2, convertSEIPtoExternalProperty.size());
		Assert.assertTrue(convertSEIPtoExternalProperty.get(0).getFirst().startsWith("external_title"));
		Assert.assertTrue(convertSEIPtoExternalProperty.get(1).getFirst().startsWith("external_title"));
		Assert.assertEquals("my cl value", convertSEIPtoExternalProperty.get(0).getSecond());
		Assert.assertEquals("my cl value", convertSEIPtoExternalProperty.get(1).getSecond());

		convertSEIPtoExternalProperty = defaultModelConverter.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE,
				"my value", null);
		Assert.assertEquals(2, convertSEIPtoExternalProperty.size());
	}

	private static EntityType createEntity() {
		EntityType entity = new EntityType();
		entity.setIdentifier("CO0001");
		entity.addProperties(new LinkedList<>());
		EntityProperty titleProp = new EntityProperty();
		entity.getProperties().add(titleProp);
		titleProp.setPropertyId("title");
		titleProp.setUri(PROP_INTERNAL_TITLE);
		titleProp.setType("an..1000");
		titleProp.addMapping(EntityPropertyMapping.AS_DATA, "external_title");
		titleProp.setCodelist(Integer.valueOf(1));
		return entity;
	}

	@Test
	public void testConvertSEIPtoExternalPropertyStringSerializableStringValid() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		entity.getProperties().get(0).setCodelist(null);
		modelConfigaration.addEntityType(entity);

		// searched by uri
		List<Pair<String, Serializable>> convertSEIPtoExternalProperty = defaultModelConverter
				.convertSEIPtoExternalProperty(PROP_INTERNAL_TITLE, "my value", "CO0001");
		Assert.assertEquals(1, convertSEIPtoExternalProperty.size());
		Assert.assertEquals("external_title", convertSEIPtoExternalProperty.get(0).getFirst());
		Assert.assertEquals("my value", convertSEIPtoExternalProperty.get(0).getSecond());
	}

	@Test
	public void testConvertExternaltoSEIPProperty() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		entity.getProperties().get(0).setCodelist(null);
		modelConfigaration.addEntityType(entity);
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionService.find("CO0001")).thenReturn(definitionModel);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(PROP_INTERNAL_TITLE);
		when(property.getIdentifier()).thenReturn("title");
		when(property.getCodelist()).thenReturn(null);

		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(String.class, "");
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(typeConverter.convert(eq(String.class), eq("my value"))).thenReturn("my converted value");
		when(definitionModel.findField(any(Predicate.class))).thenReturn(Optional.of(property));
		Pair<String, Serializable> convertExternaltoSEIPProperty = defaultModelConverter
				.convertExternaltoSEIPProperty("external_title", "my value", "CO0001");
		verify(typeConverter).convert(eq(String.class), eq("my value"));
		Assert.assertEquals(property.getIdentifier(), convertExternaltoSEIPProperty.getFirst());
		Assert.assertEquals("my converted value", convertExternaltoSEIPProperty.getSecond());
	}

	@Test
	public void testConvertExternaltoSEIPPropertyWithCodelist() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		modelConfigaration.addEntityType(entity);
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionService.find("CO0001")).thenReturn(definitionModel);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(PROP_INTERNAL_TITLE);
		when(property.getCodelist()).thenReturn(Integer.valueOf(1));

		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(String.class, "");
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(definitionModel.findField(any(Predicate.class))).thenReturn(Optional.of(property));

		Map<String, CodeValue> values = new HashMap<>();
		values.put("my cl value", new CodeValue());
		when(codelistService.getCodeValues(eq(Integer.valueOf(1)))).thenReturn(values);

		Pair<String, Serializable> convertExternaltoSEIPProperty = defaultModelConverter
				.convertExternaltoSEIPProperty("external_title", "my cl value", "CO0001");
		assertEquals(property.getIdentifier(), convertExternaltoSEIPProperty.getFirst());
		assertEquals("my value", convertExternaltoSEIPProperty.getSecond());
	}

	@Test(expected = EAIModelException.class)
	public void testConvertExternaltoSEIPPropertyWithCodelistWithFail() throws Exception {
		setupMocks(createConverter());
		EntityType entity = createEntity();
		modelConfigaration.addEntityType(entity);
		DefinitionModel definitionModel = mock(DefinitionModel.class);
		when(definitionService.find("CO0001")).thenReturn(definitionModel);
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getUri()).thenReturn(PROP_INTERNAL_TITLE);
		when(property.getCodelist()).thenReturn(Integer.valueOf(1));

		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(String.class, "");
		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(definitionModel.findField(any(Predicate.class))).thenReturn(Optional.of(property));
		defaultModelConverter.convertExternaltoSEIPProperty("external_title", "my cl value", "CO0001");
	}
}