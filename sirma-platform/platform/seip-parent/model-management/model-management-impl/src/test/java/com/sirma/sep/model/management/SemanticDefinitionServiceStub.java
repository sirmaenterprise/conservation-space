package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.*;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import org.eclipse.rdf4j.model.IRI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
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

	SemanticDefinitionServiceStub(SemanticDefinitionService semanticDefinitionService, EventService eventService) {
		this.semanticDefinitionService = semanticDefinitionService;
		classes = new HashMap<>();

		properties = new HashMap<>();
		when(semanticDefinitionService.getProperties()).thenAnswer(invocation -> new ArrayList<>(properties.values()));

		relations = new HashMap<>();
		when(semanticDefinitionService.getRelations()).thenAnswer(invocation -> new ArrayList<>(relations.values()));

		doAnswer(a -> {
			// eventService.fire(new SemanticModelUpdatedEvent());
			eventService.fire(new LoadSemanticDefinitions());
			return null;
		}).when(semanticDefinitionService).modelUpdated();
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

	public ClassBuilder withClass(IRI classIri) {
		return new ClassBuilder(classIri);
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

	public PropertyBuilder withProperty(IRI propertyIri) {
		return new PropertyBuilder(propertyIri, this::withProperty);
	}

	public PropertyBuilder withDataProperty(IRI propertyIri) {
		return withProperty(propertyIri).withProperty("propertyType", EMF.DEFINITION_DATA_PROPERTY.toString());
	}

	public PropertyBuilder withObjectProperty(IRI propertyIri) {
		return new PropertyBuilder(propertyIri, this::withRelation)
				.withProperty("propertyType", EMF.DEFINITION_OBJECT_PROPERTY.toString());
	}

	public SemanticDefinitionServiceStub withRelation(PropertyInstance relation) {
		String relationUri = relation.getId().toString();
		relations.put(relationUri, relation);
		when(semanticDefinitionService.getRelation(eq(relationUri))).thenReturn(relation);
		return this;
	}

	public class PropertyBuilder {
		private final IRI property;
		private final Consumer<PropertyInstance> resultConsumer;
		private String[] labels;
		private Map<String, Serializable> properties = new HashMap<>();

		private PropertyBuilder(IRI property, Consumer<PropertyInstance> resultConsumer) {
			this.property = property;
			this.resultConsumer = resultConsumer;
		}

		PropertyBuilder withLabels(String... labels) {
			this.labels = labels;
			return this;
		}

		PropertyBuilder withProperty(String property, Serializable value) {
			properties.put(property, value);
			return this;
		}

		PropertyBuilder withLanguageProperty(String property, String ...values) {
			properties.put(property, (Serializable) createStringMap(values));
			return this;
		}

		SemanticDefinitionServiceStub done() {
			PropertyInstance instance = createProperty(this.property, labels);
			instance.addAllProperties(properties);
			resultConsumer.accept(instance);
			return SemanticDefinitionServiceStub.this;
		}
	}

	public class ClassBuilder {
		private final IRI classIri;
		private String[] labels;
		private Map<String, Serializable> properties = new HashMap<>();
		private IRI parent;

		public ClassBuilder(IRI classIri) {this.classIri = classIri;}

		ClassBuilder withLabels(String... labels) {
			this.labels = labels;
			return this;
		}

		ClassBuilder withProperty(String property, Serializable value) {
			properties.put(property, value);
			return this;
		}

		ClassBuilder withParent(IRI parent) {
			this.parent = parent;
			return this;
		}

		SemanticDefinitionServiceStub done() {
			ClassInstance instance = createClass(this.classIri, labels);
			instance.addAllProperties(properties);
			if (parent != null) {
				SemanticDefinitionServiceStub.this.withClass(instance, parent);
			} else {
				SemanticDefinitionServiceStub.this.withRootClass(instance);
			}
			return SemanticDefinitionServiceStub.this;
		}
	}

}
