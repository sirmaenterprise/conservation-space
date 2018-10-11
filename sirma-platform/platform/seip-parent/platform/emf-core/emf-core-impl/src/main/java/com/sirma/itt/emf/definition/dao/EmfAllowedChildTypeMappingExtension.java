package com.sirma.itt.emf.definition.dao;

import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.AllowedChildTypeMappingExtension;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;

/**
 * Extension point to register the EMF class types to the allowed children types.
 *
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 5)
public class EmfAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant definitionMapping. */
	private static final Map<String, Class<? extends DefinitionModel>> DEFINITION_MAPPING;
	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> INSTANCE_MAPPING;
	/** The Constant typeMapping. */
	private static final Map<String, String> TYPE_MAPPING;

	static {
		DEFINITION_MAPPING = CollectionUtils.createHashMap(10);
		DEFINITION_MAPPING.put(ObjectTypes.LINK, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.USER, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.GROUP, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.RESOURCE, GenericDefinition.class);

		INSTANCE_MAPPING = CollectionUtils.createHashMap(10);
		INSTANCE_MAPPING.put(ObjectTypes.LINK, LinkInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.USER, EmfUser.class);
		INSTANCE_MAPPING.put(ObjectTypes.GROUP, EmfGroup.class);
		INSTANCE_MAPPING.put(ObjectTypes.RESOURCE, EmfResource.class);

		TYPE_MAPPING = CollectionUtils.createHashMap(10);
		TYPE_MAPPING.put(ObjectTypes.LINK, "linkInstance");
		TYPE_MAPPING.put(ObjectTypes.USER, "user");
		TYPE_MAPPING.put(ObjectTypes.GROUP, "group");
		TYPE_MAPPING.put(ObjectTypes.RESOURCE, "resource");
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
