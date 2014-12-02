package com.sirma.itt.objects.domain;

import java.util.Map;

import com.sirma.itt.emf.definition.dao.AllowedChildTypeMappingExtension;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Default type mappings for Objects classes as object.
 * 
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 150)
public class ObjectsAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant definitionMapping. */
	private static final Map<String, Class<? extends DefinitionModel>> definitionMapping;
	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> instanceMapping;
	/** The Constant typeMapping. */
	private static final Map<String, String> typeMapping;

	static {
		definitionMapping = CollectionUtils.createHashMap(3);
		definitionMapping.put(ObjectTypesObject.OBJECT, ObjectDefinition.class);

		instanceMapping = CollectionUtils.createHashMap(3);
		instanceMapping.put(ObjectTypesObject.OBJECT, ObjectInstance.class);

		typeMapping = CollectionUtils.createHashMap(3);
		typeMapping.put(ObjectTypesObject.OBJECT, "objectInstance");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends DefinitionModel>> getDefinitionMapping() {
		return definitionMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends Instance>> getInstanceMapping() {
		return instanceMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTypeMapping() {
		return typeMapping;
	}

}
