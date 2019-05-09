package com.sirma.sep.model.management.semantic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.AbstractModelNode;
import com.sirma.sep.model.management.ModelAttributeType;
import com.sirma.sep.model.management.ModelClass;
import com.sirma.sep.model.management.ModelProperty;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts semantic {@link ClassInstance} & {@link PropertyInstance} into their corresponding {@link ModelClass} and {@link ModelProperty}
 * representations.
 *
 * @author Mihail Radkov
 */
public class SemanticModelConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final NamespaceRegistryService namespaceRegistryService;
	private final SemanticDefinitionService definitionService;

	/**
	 * Initializes the converter with the supplied namespace registry.
	 *
	 * @param namespaceRegistryService registry used for URIs conversion
	 */
	@Inject
	public SemanticModelConverter(NamespaceRegistryService namespaceRegistryService, SemanticDefinitionService definitionService) {
		this.namespaceRegistryService = namespaceRegistryService;
		this.definitionService = definitionService;
	}

	/**
	 * Converts the provided list of {@link ClassInstance} into {@link ModelClass}.
	 * <p>
	 * After the conversion, {@link ModelClass} are linked with their parent reference.
	 *
	 * @param classInstances the class instances to convert
	 * @param modelsMetaInfo meta information used to convert {@link ModelClass}'s {@link com.sirma.sep.model.management.ModelAttribute}
	 * @return map with the converted and linked class models
	 */
	public Map<String, ModelClass> convertModelClasses(List<ClassInstance> classInstances, ModelsMetaInfo modelsMetaInfo) {
		Map<String, ModelClass> modelClassMap = classInstances.stream()
				.map(classInstance -> constructModelClass(classInstance, modelsMetaInfo))
				.collect(Collectors.toMap(ModelClass::getId, c -> c, SemanticModelConverter::mergeClassDuplicates));
		linkClasses(modelClassMap);
		return modelClassMap;
	}

	/**
	 * Converts the provided stream of {@link PropertyInstance} into corresponding {@link ModelProperty}.
	 *
	 * @param propertyInstances stream of the property instances to be converted to {@link ModelProperty}
	 * @param modelsMetaInfo meta information containing semantic property meta information used during the conversion to {@link ModelProperty}
	 * @return mapping of converted {@link ModelProperty}
	 */
	public Map<String, ModelProperty> convertModelProperties(Stream<PropertyInstance> propertyInstances, ModelsMetaInfo modelsMetaInfo) {
		return propertyInstances.map(propertyInstance -> this.constructModelProperty(propertyInstance, modelsMetaInfo))
				.collect(Collectors.toMap(ModelProperty::getId, p -> p, SemanticModelConverter::mergePropertyDuplicates));
	}

	private ModelClass constructModelClass(ClassInstance classInstance, ModelsMetaInfo modelsMetaInfo) {
		ModelClass modelClass = new ModelClass();
		modelClass.setModelsMetaInfo(modelsMetaInfo);
		modelClass.setId(classInstance.getId().toString());

		if (!classInstance.getSuperClasses().isEmpty()) {
			modelClass.setParent(classInstance.getSuperClasses().get(0).getId().toString());
		}

		collectAttributes(classInstance, modelClass, modelsMetaInfo.getSemantics());

		modelClass.setLabels(classInstance.getLabels());
		modelClass.setAsDeployed();

		return modelClass;
	}

	private void collectAttributes(Instance instance, AbstractModelNode modelClass, Collection<ModelMetaInfo> metaInfoCollection) {
		metaInfoCollection.forEach(metaInfo -> {
			Serializable property = instance.get(metaInfo.getId());
			Serializable correctedPropertyValue = ensureProperFormat(property, metaInfo.getDataType());
			modelClass.addAttribute(metaInfo.getUri(), correctedPropertyValue);
		});
	}

	private Serializable ensureProperFormat(Serializable property, String type) {
		if (ModelAttributeType.LABEL_ATTRIBUTES.contains(type)) {
			if (property == null) {
				return new HashMap<>();
			}
			if (property instanceof Map) {
				return property;
			}
			HashMap<String, Serializable> values = new HashMap<>();
			values.put("en", property);
			return values;
		} else if (ModelAttributeType.URI.equals(type)) {
			if (!(property instanceof String)) {
				return property;
			}
			String value = (String) property;
			return !value.isEmpty() ? this.namespaceRegistryService.buildFullUri(value) : value;
		}
		return property;
	}

	/**
	 * Link parent child relations between the given semantic classes
	 *
	 * @param classes the classes mapping to process
	 */
	public void linkClasses(Map<String, ModelClass> classes) {
		classes.values().forEach(classModel -> {
			ModelClass parentClass = classes.get(classModel.getParent());
			if (parentClass != null) {
				parentClass.addChild(classModel);
			}
		});
	}

	private ModelProperty constructModelProperty(PropertyInstance propertyInstance, ModelsMetaInfo modelsMetaInfo) {
		ModelProperty modelProperty = new ModelProperty();
		modelProperty.setModelsMetaInfo(modelsMetaInfo);

		modelProperty.setId(getFullUri(propertyInstance));

		collectAttributes(propertyInstance, modelProperty, modelsMetaInfo.getProperties());
		setDefaultDomain(modelProperty);

		// Override from collecting
		modelProperty.setLabels(new HashMap<>(propertyInstance.getLabels()));
		modelProperty.setAsDeployed();

		return modelProperty;
	}

	private void setDefaultDomain(ModelProperty modelProperty) {
		if (!modelProperty.hasAttribute(PropertyModelAttributes.DOMAIN)) {
			modelProperty.addAttribute(PropertyModelAttributes.DOMAIN, definitionService.getRootClass().getId());
		}
	}

	private String getFullUri(PropertyInstance propertyInstance) {
		String id = propertyInstance.getId().toString();
		return namespaceRegistryService.buildFullUri(id);
	}

	private static ModelClass mergeClassDuplicates(ModelClass m1, ModelClass m2) {
		LOGGER.warn("Duplicated class model during mapping {}", m2.getId());
		return m1;
	}

	private static ModelProperty mergePropertyDuplicates(ModelProperty p1, ModelProperty p2) {
		LOGGER.warn("Duplicated property model during mapping {}", p2.getId());
		return p1;
	}

}
