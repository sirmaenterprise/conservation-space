package com.sirma.sep.model.management;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;

import org.eclipse.rdf4j.model.IRI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Helper class for stubbing the behaviour of a {@link SemanticDefinitionService} {@link org.mockito.Mock}.
 *
 * @author Mihail Radkov
 */
class SemanticDefinitionServiceStub {

	private final SemanticDefinitionService semanticDefinitionService;
	private Map<String, ClassInstance> classes;
	private Map<String, PropertyInstance> properties;
	private Map<String, PropertyInstance> relations;

	SemanticDefinitionServiceStub(SemanticDefinitionService semanticDefinitionService) {
		this.semanticDefinitionService = semanticDefinitionService;
		classes = new HashMap<>();

		properties = new HashMap<>();
		when(semanticDefinitionService.getProperties()).thenAnswer(invocation -> new ArrayList<>(properties.values()));

		relations = new HashMap<>();
		when(semanticDefinitionService.getRelations()).thenAnswer(invocation -> new ArrayList<>(relations.values()));
	}

	public SemanticDefinitionServiceStub withRootClass(ClassInstance rootClass) {
		when(semanticDefinitionService.getRootClass()).thenReturn(rootClass);
		classes.put(rootClass.getId().toString(), rootClass);
		return this;
	}

	public SemanticDefinitionServiceStub withClass(ClassInstance classInstance, IRI parentIri) {
		String classId = classInstance.getId().toString();
		classes.put(classId, classInstance);
		ClassInstance parentClass = classes.get(parentIri.toString());
		parentClass.getSubClasses().put(classId, classInstance);
		classInstance.getSuperClasses().add(parentClass);
		return this;
	}

	public SemanticDefinitionServiceStub withPropertyForClass(String propertyKey, Serializable property, IRI classIri) {
		classes.get(classIri.toString()).getProperties().put(propertyKey, property);
		return this;
	}

	public SemanticDefinitionServiceStub withProperty(PropertyInstance property) {
		String propertyUri = property.getId().toString();
		properties.put(propertyUri, property);
		when(semanticDefinitionService.getProperty(eq(propertyUri))).thenReturn(property);
		return this;
	}

	public SemanticDefinitionServiceStub fillProperty(IRI propertyIri, Map<String, Serializable> propertiesToFill) {
		properties.get(propertyIri.toString()).addAllProperties(propertiesToFill);
		return this;
	}

	public SemanticDefinitionServiceStub withRelation(PropertyInstance relation) {
		String relationUri = relation.getId().toString();
		relations.put(relationUri, relation);
		when(semanticDefinitionService.getRelation(eq(relationUri))).thenReturn(relation);
		return this;
	}

}
