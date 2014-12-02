/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.objects.web.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.javacrumbs.jsonunit.JsonAssert;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.PropertyInstance;

/**
 * Tests for {@link DefinitionRestService}.
 * 
 * @author Adrian Mitev
 */
@Test
public class DefinitionRestServiceTest {

	private DefinitionRestService definitionRestService;

	private List<PropertyInstance> relations = new ArrayList<>();

	/**
	 * Initializes CUT.
	 */
	@BeforeMethod
	public void init() {
		definitionRestService = new DefinitionRestService();
		relations = new ArrayList<>();
	}

	/**
	 * Tests {@link DefinitionRestService#getSearchableTypes()}. It should return json containing
	 * only searchable=true elements sorted by title field.
	 */
	// public void testGetSearchableTypes() {
	// List<ClassInstance> classInstances = new ArrayList<>();
	// classInstances.add(createClassInstance("A Name", "A title", true));
	// classInstances.add(createClassInstance("B Name", "B title", false));
	// classInstances.add(createClassInstance("D Name", "D title", true));
	// classInstances.add(createClassInstance("C Name", "C title", true));
	//
	// SemanticDefinitionService semanticDefinitionService = Mockito
	// .mock(SemanticDefinitionService.class);
	// Mockito.when(semanticDefinitionService.getClasses()).thenReturn(classInstances);
	//
	// ReflectionUtils.setField(definitionRestService, "semanticDefinitionService",
	// semanticDefinitionService);
	//
	// JsonAssert
	// .assertJsonEquals(
	// "[{\"title\":\"A title\",\"name\":\"A Name\"},{\"title\":\"C title\",\"name\":\"C Name\"},{\"title\":\"D title\",\"name\":\"D Name\"}]",
	// definitionRestService.getSearchableTypes());
	// }

	/**
	 * Creates ClassInstance and initializes properties.
	 * 
	 * @param name
	 *            name to initialize.
	 * @param title
	 *            title to initialize.
	 * @param searchable
	 *            is the element searchable.
	 * @return created instance.
	 */
	private ClassInstance createClassInstance(String name, String title, Boolean searchable) {
		ClassInstance instance = new ClassInstance();
		Map<String, Serializable> properties = new HashMap<>();
		instance.setProperties(properties);

		properties.put("searchable", searchable);
		properties.put("id", name);
		properties.put("title", title);

		return instance;
	}

	/**
	 * Tests {@link DefinitionRestService#getRelations(String)} with no (null) parameter. It should
	 * provide all available relationships for all object types.
	 */
	// public void testGetRelationsForAllTypes() {
	// withRelation("partOf", "Part of", "Part");
	// withRelation("childOf", "Child of", "Object");
	// withRelation("parentOf", "Parent of", "Object");
	// withRelation("extends", "Extends", "ClassType");
	//
	// expectGetReleationsJson(
	// null,
	// "[{\"title\":\"Child of\",\"name\":\"childOf\",\"domainClass\":\"Object\"},{\"title\":\"Extends\",\"name\":\"extends\",\"domainClass\":\"ClassType\"},{\"title\":\"Parent of\",\"name\":\"parentOf\",\"domainClass\":\"Object\"},{\"title\":\"Part of\",\"name\":\"partOf\",\"domainClass\":\"Part\"}]");
	// }

	/**
	 * Tests {@link DefinitionRestService#getRelations(String)} for only one object type. It should
	 * provide all available relationships for the given object type.
	 */
	// public void testGetRelationsForSingleType() {
	// withRelation("partOf", "Part of", "Part");
	// withRelation("childOf", "Child of", "Object");
	// withRelation("parentOf", "Parent of", "Object");
	// withRelation("extends", "Extends", "ClassType");
	//
	// expectGetReleationsJson(
	// "Object",
	// "[{\"title\":\"Child of\",\"name\":\"childOf\",\"domainClass\":\"Object\"},{\"title\":\"Parent of\",\"name\":\"parentOf\",\"domainClass\":\"Object\"}]");
	// }

	/**
	 * Tests {@link DefinitionRestService#getRelations(String)} for multiple object types. It should
	 * provide all available relationships for the given object types.
	 */
	// public void testGetRelationsForManyTypes() {
	// withRelation("partOf", "Part of", "Part");
	// withRelation("childOf", "Child of", "Object");
	// withRelation("parentOf", "Parent of", "Object");
	// withRelation("extends", "Extends", "ClassType");
	//
	// expectGetReleationsJson(
	// "ClassType,Part",
	// "[{\"title\":\"Extends\",\"name\":\"extends\",\"domainClass\":\"ClassType\"},{\"title\":\"Part of\",\"name\":\"partOf\",\"domainClass\":\"Part\"}]");
	// }

	/**
	 * Adds a relation (PropertyInstance) to the list with all instances that should be provided by
	 * the {@link SemanticDefinitionService#getRelations()} method.
	 * 
	 * @param id
	 *            relation id.
	 * @param title
	 *            relation title.
	 * @param domainClass
	 *            relation domain class.
	 */
	private void withRelation(String id, String title, String domainClass) {
		PropertyInstance instance = new PropertyInstance();
		Map<String, Serializable> properties = new HashMap<>();
		instance.setProperties(properties);

		properties.put("id", id);
		properties.put("title", title);
		properties.put("domainClass", domainClass);

		relations.add(instance);
	}

	/**
	 * Calls {@link DefinitionRestService#getRelations(String)} with provided mock for the backend
	 * service and asserts the returned json.
	 * 
	 * @param forType
	 *            types passed to {@link DefinitionRestService#getRelations(String)}
	 * @param expectedJson
	 *            expected json that the result will be asserted against.
	 */
	private void expectGetReleationsJson(String forType, String expectedJson) {
		SemanticDefinitionService semanticDefinitionService = Mockito
				.mock(SemanticDefinitionService.class);
		Mockito.when(semanticDefinitionService.getRelations()).thenReturn(relations);

		ReflectionUtils.setField(definitionRestService, "semanticDefinitionService",
				semanticDefinitionService);

		JsonAssert.assertJsonEquals(expectedJson, definitionRestService.getRelations(forType));
	}

}
