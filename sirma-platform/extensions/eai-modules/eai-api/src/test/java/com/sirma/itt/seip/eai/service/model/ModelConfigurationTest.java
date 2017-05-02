package com.sirma.itt.seip.eai.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;

/**
 * Test ModelConfiguration.
 * 
 * @author gshevkedov
 */
public class ModelConfigurationTest {

	@Test
	public void testAddEntityType() {
		EntityType entity = Mockito.mock(EntityType.class);
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		Mockito.when(entity.getIdentifier()).thenReturn("id");
		modelConfigaration.addEntityType(entity);
		assertEquals(1, modelConfigaration.getEntityTypes().size());
	}

	@Test(expected = EAIRuntimeException.class)
	public void testAddEntityTypeFail() {
		EntityType entity = Mockito.mock(EntityType.class);
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		Mockito.when(entity.getIdentifier()).thenReturn("id");
		modelConfigaration.addEntityType(entity);
		modelConfigaration.addEntityType(entity);
	}

	@Test
	public void testGetRelationByName() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		String definitionId = "NGADT21";
		String name = "definition name";
		assertNull(modelConfigaration.getRelationByExternalName(definitionId, name));
	}

	@Test
	public void testGetPropertyByExternalNameWithNullParam() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		assertNull(modelConfigaration.getPropertyByExternalName(null));
	}

	@Test
	public void testGetTypeByExternalName() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		assertNull(modelConfigaration.getTypeByExternalName(null));
	}

	@Test
	public void testGetTypeByDefintionId() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		assertNull(modelConfigaration.getTypeByDefinitionId(null));
	}

	@Test
	public void testPropertyByInternalName() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		EntityType entity = mock(EntityType.class);
		when(entity.getIdentifier()).thenReturn("type");
		EntityProperty property1 = buildProperty("id1");
		EntityProperty property2 = buildProperty("id2");
		when(entity.getProperties()).thenReturn(Arrays.asList(property1, property2));
		modelConfigaration.addEntityType(entity);
		modelConfigaration.seal();
		assertEquals(0, modelConfigaration.getPropertyByInternalName(null).size());
		assertEquals(property1, modelConfigaration.getPropertyByInternalName("type", "uri_id1"));
		assertNull(modelConfigaration.getPropertyByInternalName("type_missing", "uri_id1"));
		assertEquals(property1, modelConfigaration.getPropertyByInternalName(null, "uri_id1"));
		assertNull(modelConfigaration.getPropertyByInternalName(null, null));
	}

	@Test
	public void testIsSealed() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		assertFalse(modelConfigaration.isSealed());
		modelConfigaration.seal();
		assertTrue(modelConfigaration.isSealed());
	}

	@Test
	public void testGetRelationByExternalName() throws Exception {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		EntityType entity = mock(EntityType.class);
		when(entity.getIdentifier()).thenReturn("type");
		EntityRelation relation = mock(EntityRelation.class);
		when(relation.getTitle()).thenReturn("title");
		when(relation.hasMapping("ext_name")).thenReturn(true);
		when(entity.getRelations()).thenReturn(Collections.singletonList(relation));
		modelConfigaration.addEntityType(entity);
		modelConfigaration.seal();
		assertEquals(relation, modelConfigaration.getRelationByExternalName("type", "ext_name"));
		assertNull(modelConfigaration.getRelationByExternalName("type", "ext_name_missing"));
	}

	@Test
	public void testGetPropertyByFilter() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		EntityType entity = mock(EntityType.class);
		when(entity.getIdentifier()).thenReturn("type");
		EntityProperty property1 = buildProperty("id1");
		EntityProperty property2 = buildProperty("id2");
		when(entity.getProperties()).thenReturn(Arrays.asList(property1, property2));
		modelConfigaration.addEntityType(entity);
		modelConfigaration.seal();
		assertEquals(property1,
				modelConfigaration.getPropertyByFilter(property -> "uri_id1".equals(property.getUri())));
		assertEquals(property1,
				modelConfigaration.getPropertyByFilter(property -> "id1".equals(property.getPropertyId())));
		assertEquals(property1,
				modelConfigaration.getPropertyByFilter(property -> "title_id1".equals(property.getTitle())));
		assertEquals(property1,
				modelConfigaration.getPropertyByFilter("type", property -> "uri_id1".equals(property.getUri())));
		assertEquals(property2,
				modelConfigaration.getPropertyByFilter(property -> "uri_id2".equals(property.getUri())));

		assertNull(modelConfigaration.getPropertyByFilter(property -> "uri_id3".equals(property.getUri())));
		assertNull(modelConfigaration.getPropertyByFilter("type1", property -> "uri_id1".equals(property.getUri())));
	}

	@Test
	public void testGetByFilter() {
		ModelConfiguration modelConfigaration = new ModelConfiguration();
		EntityType entity1 = mock(EntityType.class);
		when(entity1.getIdentifier()).thenReturn("type1");
		when(entity1.hasMapping("ext_type1")).thenReturn(true);

		EntityType entity2 = mock(EntityType.class);
		when(entity2.getIdentifier()).thenReturn("type2");
		when(entity2.hasMapping("ext_type2")).thenReturn(true);

		EntityProperty property1 = buildProperty("id1");
		EntityProperty property2 = buildProperty("id2");
		when(entity1.getProperties()).thenReturn(Arrays.asList(property1, property2));
		EntityProperty property3 = buildProperty("id3");
		when(entity2.getProperties()).thenReturn(Arrays.asList(property3));
		modelConfigaration.addEntityType(entity1);
		modelConfigaration.addEntityType(entity2);
		modelConfigaration.seal();
		assertEquals(entity1, modelConfigaration.getTypeByDefinitionId("type1"));
		assertEquals(entity2, modelConfigaration.getTypeByDefinitionId("type2"));
		assertNull(modelConfigaration.getTypeByDefinitionId("type3"));
		assertEquals(entity2, modelConfigaration.getTypeByExternalName("ext_type2"));
		assertNull(modelConfigaration.getTypeByExternalName("ext_type3"));
	}

	private EntityProperty buildProperty(String id) {
		EntityProperty property = mock(EntityProperty.class);
		when(property.getTitle()).thenReturn("title_" + id);
		when(property.getUri()).thenReturn("uri_" + id);
		when(property.getPropertyId()).thenReturn(id);
		return property;
	}
}
