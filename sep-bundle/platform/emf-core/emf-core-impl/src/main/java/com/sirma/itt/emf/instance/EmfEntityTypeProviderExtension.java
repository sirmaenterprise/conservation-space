package com.sirma.itt.emf.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.entity.CommonEntity;
import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.forum.entity.CommentEntity;
import com.sirma.itt.emf.forum.entity.TopicEntity;
import com.sirma.itt.emf.forum.model.ChatInstance;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Default Emf extension for entity type provider.
 *
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 10)
public class EmfEntityTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(Arrays.asList(
			CommonEntity.class, CommonInstance.class, LinkEntity.class, LinkInstance.class,
			LinkReference.class, CommentEntity.class, CommentInstance.class, TopicEntity.class,
			TopicInstance.class, ChatInstance.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Object object) {
		return EmfEntityIdType.getType(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(CommonEntity.class) || object.equals(CommonInstance.class)) {
			return EmfEntityIdType.INSTANCE;
		} else if (object.equals(LinkEntity.class) || object.equals(LinkReference.class)
				|| object.equals(LinkInstance.class)) {
			return EmfEntityIdType.LINK_INSTANCE;
		} else if (object.equals(CommentEntity.class) || object.equals(ChatInstance.class)
				|| object.equals(CommentInstance.class)) {
			return EmfEntityIdType.COMMENT_INSTANCE;
		} else if (object.equals(TopicEntity.class) || object.equals(TopicInstance.class)) {
			return EmfEntityIdType.TOPIC_INSTANCE;
		}
		return EmfEntityIdType.UNKNOWN;
	}

}
