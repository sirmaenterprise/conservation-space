package com.sirma.sep.model.management.semantic;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.AbstractModelNode;
import com.sirma.sep.model.management.ModelAttributeType;
import com.sirma.sep.model.management.ModelClass;
import com.sirma.sep.model.management.ModelProperty;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

/**
 * Converts semantic {@link ClassInstance} & {@link PropertyInstance} into their corresponding {@link ModelClass} and {@link ModelProperty}
 * representations.
 *
 * @author Mihail Radkov
 */
public class SemanticModelConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final NamespaceRegistryService namespaceRegistryService;

	/**
	 * Initializes the converter with the supplied namespace registry.
	 *
	 * @param namespaceRegistryService registry used for URIs conversion
	 */
	@Inject
	public SemanticModelConverter(NamespaceRegistryService namespaceRegistryService) {
		this.namespaceRegistryService = namespaceRegistryService;
	}

	/**
	 * Converts the provided list of {@link ClassInstance} into {@link ModelClass}.
	 * <p>
	 * After the conversion, {@link ModelClass} are linked with their parent reference.
	 *
	 * @param classInstances the class instances to convert
	 * @param semanticMetaInfos mapping used to convert {@link ModelClass}'s {@link com.sirma.sep.model.management.ModelAttribute}
	 * @return map with the converted and linked class models
	 */
	public Map<String, ModelClass> convertModelClasses(List<ClassInstance> classInstances, Map<String, ModelMetaInfo> semanticMetaInfos) {
		Map<String, ModelClass> modelClassMap = classInstances.stream()
				.map(classInstance -> constructModelClass(classInstance, semanticMetaInfos))
				.collect(Collectors.toMap(ModelClass::getId, c -> c, SemanticModelConverter::mergeClassDuplicates));
		linkClasses(modelClassMap);
		return modelClassMap;
	}

	/**
	 * Converts the provided stream of {@link PropertyInstance} into corresponding {@link ModelProperty}.
	 *
	 * @param propertyInstances stream of the property instances to be converted to {@link ModelProperty}
	 * @param propertiesMetaInfo collection of semantic property meta information used during the conversion to {@link ModelProperty}
	 * @return mapping of converted {@link ModelProperty}
	 */
	public Map<String, ModelProperty> convertModelProperties(Stream<PropertyInstance> propertyInstances,
			Collection<ModelMetaInfo> propertiesMetaInfo) {
		return propertyInstances.map(propertyInstance -> this.constructModelProperty(propertyInstance, propertiesMetaInfo))
				.collect(Collectors.toMap(ModelProperty::getId, p -> p, SemanticModelConverter::mergePropertyDuplicates));
	}

	private ModelClass constructModelClass(ClassInstance classInstance, Map<String, ModelMetaInfo> semanticMetaInfos) {
		ModelClass modelClass = new ModelClass();
		modelClass.setId(classInstance.getId().toString());

		if (!classInstance.getSuperClasses().isEmpty()) {
			modelClass.setParent(classInstance.getSuperClasses().get(0).getId().toString());
		}

		collectAttributes(classInstance, modelClass, semanticMetaInfos.values());

		modelClass.setLabels(classInstance.getLabels());

		return modelClass;
	}

	private static void collectAttributes(Instance instance, AbstractModelNode modelClass,
			Collection<ModelMetaInfo> metaInfoCollection) {
		metaInfoCollection.forEach(metaInfo -> {
			Serializable property = instance.get(metaInfo.getId());
			Serializable correctedPropertyValue = ensureProperFormat(property, metaInfo.getType());
			modelClass.addAttribute(metaInfo.getUri(), metaInfo.getType(), correctedPropertyValue);
		});
	}

	private static Serializable ensureProperFormat(Serializable property, String type) {
		if (ModelAttributeType.MAP_ATTRIBUTES.contains(type)) {
			if (property == null) {
				return new HashMap<>();
			}
			if (property instanceof Map) {
				return property;
			}
			HashMap<String, Serializable> values = new HashMap<>();
			values.put("en", property);
			return values;
		}
		return property;
	}

	private static void linkClasses(Map<String, ModelClass> classes) {
		classes.values().forEach(classModel -> classModel.setParentReference(classes.get(classModel.getParent())));
	}

	private ModelProperty constructModelProperty(PropertyInstance propertyInstance, Collection<ModelMetaInfo> propertiesMetaInfo) {
		ModelProperty modelProperty = new ModelProperty();

		modelProperty.setId(getFullUri(propertyInstance));

		collectAttributes(propertyInstance, modelProperty, propertiesMetaInfo);

		// Override from collecting
		modelProperty.setLabels(new HashMap<>(propertyInstance.getLabels()));

		return modelProperty;
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
