package com.sirma.itt.emf.instance.dao;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildTypeMappingExtension;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default implementation of the {@link AllowedChildrenTypeProvider}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AllowedChildrenTypeProviderImpl implements AllowedChildrenTypeProvider {

	/** The extensions. */
	@Inject
	@ExtensionPoint(AllowedChildTypeMappingExtension.TARGET_NAME)
	private Iterable<AllowedChildTypeMappingExtension> extensions;
	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The definition mapping. */
	private Map<String, Class<? extends DefinitionModel>> definitionMapping;
	/** The instance mapping. */
	private Map<String, Class<? extends Instance>> instanceMapping;
	/** The type mapping. */
	private Map<String, String> typeMapping;
	/** The class to type mapping. */
	private Map<Class<? extends Instance>, String> classToTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		definitionMapping = CollectionUtils.createHashMap(10);
		instanceMapping = CollectionUtils.createHashMap(10);
		typeMapping = CollectionUtils.createHashMap(10);
		classToTypeMapping = CollectionUtils.createHashMap(10);
		for (AllowedChildTypeMappingExtension extension : extensions) {
			definitionMapping.putAll(extension.getDefinitionMapping());
			instanceMapping.putAll(extension.getInstanceMapping());
			typeMapping.putAll(extension.getTypeMapping());
		}
		for (Entry<String, Class<? extends Instance>> entry : instanceMapping.entrySet()) {
			classToTypeMapping.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends DefinitionModel> getDefinitionClass(String type) {
		return definitionMapping.get(type.toLowerCase());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<? extends Instance> getInstanceClass(String type) {
		return instanceMapping.get(type.toLowerCase());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDataTypeName(String type) {
		return typeMapping.get(type.toLowerCase());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataTypeDefinition getDataType(String type) {
		String name = getDataTypeName(type);
		if (name != null) {
			return dictionaryService.getDataTypeDefinition(name);
		}
		return null;
	}

	@Override
	public String getTypeByInstance(Class<? extends Instance> clazz) {
		return classToTypeMapping.get(clazz);
	}

}
