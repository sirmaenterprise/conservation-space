package com.sirma.itt.seip.instance.dao;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.AllowedChildTypeMappingExtension;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Default implementation of the {@link TypeMappingProvider}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AllowedChildrenTypeProviderImpl implements TypeMappingProvider {

	@Inject
	@ExtensionPoint(AllowedChildTypeMappingExtension.TARGET_NAME)
	private Iterable<AllowedChildTypeMappingExtension> extensions;

	@Inject
	private DefinitionService definitionService;

	private Map<String, Class<? extends DefinitionModel>> definitionMapping;

	private Map<String, Class<? extends Instance>> instanceMapping;

	private Map<String, String> typeMapping;

	private Map<String, String> inverseTypeMapping;

	private Map<Class<? extends Instance>, String> classToTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		definitionMapping = CollectionUtils.createHashMap(32);
		instanceMapping = CollectionUtils.createHashMap(32);
		typeMapping = CollectionUtils.createHashMap(32);
		inverseTypeMapping = CollectionUtils.createHashMap(32);
		classToTypeMapping = CollectionUtils.createHashMap(32);
		for (AllowedChildTypeMappingExtension extension : extensions) {
			definitionMapping.putAll(extension.getDefinitionMapping());
			instanceMapping.putAll(extension.getInstanceMapping());
			for (Entry<String, String> entry : extension.getTypeMapping().entrySet()) {
				typeMapping.put(entry.getKey(), entry.getValue());
				inverseTypeMapping.put(entry.getValue().toLowerCase(), entry.getKey());
			}
		}
		for (Entry<String, Class<? extends Instance>> entry : instanceMapping.entrySet()) {
			classToTypeMapping.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public Class<? extends DefinitionModel> getDefinitionClass(String type) {
		Class<? extends DefinitionModel> definitionClass = definitionMapping.get(type.toLowerCase());
		if (definitionClass == null) {
			String classType = inverseTypeMapping.get(type);
			if (classType != null) {
				definitionClass = definitionMapping.get(classType.toLowerCase());
			}
		}
		return definitionClass;
	}

	@Override
	public Class<? extends Instance> getInstanceClass(String type) {
		Class<? extends Instance> instanceClass = instanceMapping.get(type.toLowerCase());
		if (instanceClass == null) {
			String classType = inverseTypeMapping.get(type);
			if (classType != null) {
				instanceClass = instanceMapping.get(classType.toLowerCase());
			}
		}
		return instanceClass;
	}

	@Override
	public String getDataTypeName(String type) {
		return typeMapping.get(type.toLowerCase());
	}

	@Override
	public DataTypeDefinition getDataType(String type) {
		String name = getDataTypeName(type);
		if (name != null) {
			return definitionService.getDataTypeDefinition(name.toLowerCase());
		}
		return null;
	}

	@Override
	public String getTypeByInstance(Class<? extends Instance> clazz) {
		return classToTypeMapping.get(clazz);
	}
}
