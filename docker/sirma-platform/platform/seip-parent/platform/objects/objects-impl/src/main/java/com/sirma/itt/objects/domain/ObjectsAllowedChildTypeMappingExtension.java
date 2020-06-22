package com.sirma.itt.objects.domain;

import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.AllowedChildTypeMappingExtension;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default type mappings for Objects classes as object.
 *
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 150)
public class ObjectsAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant definitionMapping. */
	private static final Map<String, Class<? extends DefinitionModel>> DEFINITION_MAPPING;
	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> INSTANCE_MAPPING;
	/** The Constant typeMapping. */
	private static final Map<String, String> TYPE_MAPPING;

	private static final String OBJECT_INSTANCE = "objectInstance";

	static {
		DEFINITION_MAPPING = CollectionUtils.createHashMap(3);
		DEFINITION_MAPPING.put(ObjectTypes.OBJECT, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.CLASS, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.CASE, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.DOCUMENT, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.FOLDER, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.STANDALONE_TASK, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.WORKFLOW, GenericDefinition.class);
		DEFINITION_MAPPING.put("project", GenericDefinition.class);

		INSTANCE_MAPPING = CollectionUtils.createHashMap(3);
		INSTANCE_MAPPING.put(ObjectTypes.OBJECT, ObjectInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.CASE, ObjectInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.DOCUMENT, ObjectInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.FOLDER, ObjectInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.STANDALONE_TASK, ObjectInstance.class);
		INSTANCE_MAPPING.put("project", ObjectInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.CLASS, ClassInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.WORKFLOW, ObjectInstance.class);

		TYPE_MAPPING = CollectionUtils.createHashMap(3);
		TYPE_MAPPING.put(ObjectTypes.OBJECT, OBJECT_INSTANCE);
		TYPE_MAPPING.put(ObjectTypes.CASE, OBJECT_INSTANCE);
		TYPE_MAPPING.put(ObjectTypes.DOCUMENT, OBJECT_INSTANCE);
		TYPE_MAPPING.put(ObjectTypes.FOLDER, OBJECT_INSTANCE);
		TYPE_MAPPING.put(ObjectTypes.STANDALONE_TASK, OBJECT_INSTANCE);
		TYPE_MAPPING.put("project", OBJECT_INSTANCE);
		TYPE_MAPPING.put(ObjectTypes.CLASS, "class");
		TYPE_MAPPING.put(ObjectTypes.WORKFLOW, OBJECT_INSTANCE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends DefinitionModel>> getDefinitionMapping() {
		return DEFINITION_MAPPING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends Instance>> getInstanceMapping() {
		return INSTANCE_MAPPING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTypeMapping() {
		return TYPE_MAPPING;
	}

}
