package com.sirma.itt.seip.eai.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.convert.TypeConverterUtilMock;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.json.JsonUtil;

public class XlsxModelParserTest {

	/**
	 * Parses the model.
	 * 
	 * @throws EAIModelException
	 */
	@Test
	public void parseModel() throws EAIModelException {

		Map<String, Integer> model = XlsxModelParserTestUtil.provideXlsxPropertyMapping();

		TypeConverterUtilMock.setUpTypeConverter();

		List<EntityProperty> parseModelXlsx = XlsxModelParser
				.parseModelXlsx(XlsxModelParser.class.getResourceAsStream("common.xlsx"), "common.xlsx", model);
		for (EntityProperty entityProperty : parseModelXlsx) {
			System.out.println(entityProperty);
		}
		Assert.assertNotNull(parseModelXlsx);
		Assert.assertEquals(parseModelXlsx.size(), 21);
		EntityProperty entityProperty = parseModelXlsx
				.stream()
					.filter(e -> "emf:externalID".equals(e.getUri()))
					.findFirst()
					.get();
		Assert.assertEquals(entityProperty.getPropertyId(), "objectId");
		Assert.assertNull(entityProperty.getCodelist());
		Assert.assertEquals(entityProperty.getMapping(EntityPropertyMapping.AS_DATA), "cultObj:id");
		Assert.assertEquals(entityProperty.getMapping(EntityPropertyMapping.AS_DATA), entityProperty.getDataMapping());
		Assert.assertEquals(entityProperty.getTitle(), "Object ID");
		Assert.assertEquals(entityProperty.getType(), "n..6");
		Assert.assertTrue(entityProperty.isMandatory());
	}

	@Test
	public void parseSmallerModel() {
		JSONObject jsonObject = JsonUtil.createObjectFromString(
				"{\"TITLE\":0,\"URI\":10,\"PPROPERTY_ID\":11,\"MAPPING_DATA_CONVERT\":12,\"MAPPING_CRITERIA_CONVERT\":13,\"MANDATORY_SEIP\":2,\"DATA_TYPE\":4,\"CODELIST_ID\":5, \"THIN_REQUEST_USAGE\":16}");
		Map<String, Integer> model = JsonUtil.toMap(jsonObject);
		TypeConverterUtilMock.setUpTypeConverter();
		List<EntityProperty> parseModelXlsx = XlsxModelParser.parseModelXlsx(
				XlsxModelParser.class.getResourceAsStream("common_model2.xlsx"), "common_model2.xlsx", model);
		for (EntityProperty entityProperty : parseModelXlsx) {
			System.out.println(entityProperty);
		}
		Assert.assertNotNull(parseModelXlsx);
		Assert.assertEquals(parseModelXlsx.size(), 8);

		EntityProperty entityProperty = parseModelXlsx
				.stream()
					.filter(e -> "Sub-Classification".equals(e.getTitle()))
					.findFirst()
					.get();
		Assert.assertNull(entityProperty.getPropertyId());
		Assert.assertEquals(Integer.valueOf(247), entityProperty.getCodelist());
		Assert.assertEquals("cultObj:subClassification", entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
		Assert.assertEquals(entityProperty.getDataMapping(), entityProperty.getMapping(EntityPropertyMapping.AS_DATA));
		Assert.assertEquals("emf:businessType", entityProperty.getUri());
		Assert.assertEquals("an..50", entityProperty.getType());
		Assert.assertFalse(entityProperty.isMandatory());
	}

	/**
	 * Parses the types.
	 */
	@Test
	public void parseTypes() {
		List<EntityType> parseTypesXlsx = XlsxModelParser
				.parseTypesXlsx(XlsxModelParser.class.getResourceAsStream("types.xlsx"), "types.xslx");
		Assert.assertNotNull(parseTypesXlsx);
		Assert.assertEquals(parseTypesXlsx.size(), 2);
		EntityType entityType = parseTypesXlsx
				.stream()
					.filter(e -> "NGACO7001".equals(e.getIdentifier()))
					.findFirst()
					.get();
		Assert.assertEquals(entityType.getTitle(), "Drawing");
		Assert.assertEquals(entityType.getMappings().iterator().next(), "drawing");
		Assert.assertEquals(entityType.getUri(),
				"http://www.sirma.com/ontologies/2016/02/culturalHeritageConservation#Drawing");
		Assert.assertEquals(entityType.getProperties().size(), 0);
		Assert.assertEquals(entityType.getRelations().size(), 0);

	}

	/**
	 * Parses the relations.
	 */
	@Test
	public void parseRelations() {
		List<EntityRelation> parseTypesXlsx = XlsxModelParser
				.parseRelationsXlsx(XlsxModelParser.class.getResourceAsStream("relations.xlsx"), "relations.xlsx");
		Assert.assertNotNull(parseTypesXlsx);
		Assert.assertEquals(parseTypesXlsx.size(), 2);

		EntityRelation entityRelations = parseTypesXlsx
				.stream()
					.filter(e -> "associated with".equals(e.getTitle()))
					.findFirst()
					.get();
		Assert.assertEquals(entityRelations.getDomain(), "Cultural object");
		Assert.assertEquals(entityRelations.getRange(), "Cultural range");
		Assert.assertEquals(entityRelations.getMappings(),
				Arrays.asList(new String[] { "haschild", "hasparent", "hassibling" }));
		Assert.assertEquals(entityRelations.getUri(), "nga:associatedWith");
	}
}
