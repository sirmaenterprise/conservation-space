package com.sirma.itt.emf.definition.dao;

import java.util.Map;

import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.CollectionUtils;

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
		DEFINITION_MAPPING.put(ObjectTypes.COMMENT, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.LINK, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.TOPIC, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.USER, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.GROUP, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.RESOURCE, GenericDefinition.class);
		DEFINITION_MAPPING.put(ObjectTypes.IMAGE_ANNOTATION, GenericDefinition.class);

		INSTANCE_MAPPING = CollectionUtils.createHashMap(10);
		INSTANCE_MAPPING.put(ObjectTypes.COMMENT, CommentInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.LINK, LinkInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.TOPIC, TopicInstance.class);
		INSTANCE_MAPPING.put(ObjectTypes.USER, EmfUser.class);
		INSTANCE_MAPPING.put(ObjectTypes.GROUP, EmfGroup.class);
		INSTANCE_MAPPING.put(ObjectTypes.RESOURCE, EmfResource.class);
		INSTANCE_MAPPING.put(ObjectTypes.IMAGE_ANNOTATION, ImageAnnotation.class);

		TYPE_MAPPING = CollectionUtils.createHashMap(10);
		TYPE_MAPPING.put(ObjectTypes.COMMENT, "commentInstance");
		TYPE_MAPPING.put(ObjectTypes.LINK, "linkInstance");
		TYPE_MAPPING.put(ObjectTypes.TOPIC, "topicInstance");
		TYPE_MAPPING.put(ObjectTypes.USER, "user");
		TYPE_MAPPING.put(ObjectTypes.GROUP, "group");
		TYPE_MAPPING.put(ObjectTypes.RESOURCE, "resource");
		TYPE_MAPPING.put(ObjectTypes.IMAGE_ANNOTATION, "imageAnnotation");
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
