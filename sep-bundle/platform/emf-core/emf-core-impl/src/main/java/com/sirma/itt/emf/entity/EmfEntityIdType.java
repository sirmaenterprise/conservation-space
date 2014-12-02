/*
 *
 */
package com.sirma.itt.emf.entity;

import com.sirma.itt.emf.forum.entity.CommentEntity;
import com.sirma.itt.emf.forum.entity.TopicEntity;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * The Enum EntityIdType.
 *
 * @author BBonev
 */
public enum EmfEntityIdType implements EntityType {

	/** The unknown. */
	UNKNOWN(0),
	/** The user. */
	USER(6),
	/** The {@link CommonInstance} representation type. */
	INSTANCE(7),
	/** The value instance. */
	VALUE_INSTANCE(8),
	/** The link instance. */
	LINK_INSTANCE(9),
	/** The topic instance. */
	TOPIC_INSTANCE(10),
	/** The comment instance. */
	COMMENT_INSTANCE(11),
	/** The group. */
	GROUP(12);

	/** The id. */
	private int id;

	/**
	 * Instantiates a new entity id type.
	 *
	 * @param id
	 *            the id
	 */
	private EmfEntityIdType(int id) {
		this.id = id;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType() {
		return id;
	}

	/**
	 * Gets the {@link EmfEntityIdType} by the given object instance. If the object
	 * instance is not recognized then {@link #UNKNOWN} type will be returned.
	 * <p>
	 * NOTE: this is implementation specific method!
	 *
	 * @param object
	 *            the object
	 * @return the type
	 */
	public static EmfEntityIdType getType(Object object) {
		if ((object instanceof CommonInstance) || (object instanceof CommonEntity)) {
			return INSTANCE;
		} else if ((object instanceof LinkInstance) || (object instanceof LinkEntity)
				|| (object instanceof LinkReference)) {
			return LINK_INSTANCE;
		} else if ((object instanceof TopicInstance) || (object instanceof TopicEntity)) {
			return TOPIC_INSTANCE;
		} else if ((object instanceof CommentInstance) || (object instanceof CommentEntity)) {
			return COMMENT_INSTANCE;
		} else if (object instanceof EmfUser) {
			return USER;
		} else if (object instanceof EmfGroup) {
			return GROUP;
		} else {
			return UNKNOWN;
		}

	}

	@Override
	public int getTypeId() {
		return getType();
	}

	@Override
	public String getName() {
		return toString();
	}
}
