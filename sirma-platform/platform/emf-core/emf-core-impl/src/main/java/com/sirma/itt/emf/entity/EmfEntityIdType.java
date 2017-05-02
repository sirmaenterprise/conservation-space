/*
 *
 */
package com.sirma.itt.emf.entity;

import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.archive.ArchivedEntity;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;

/**
 * The Enum EntityIdType.
 *
 * @author BBonev
 */
public enum EmfEntityIdType implements EntityType {

	/** The unknown. */
	UNKNOWN(0), /** The user. */
	INSTANCE(7), /** The value instance. */
	VALUE_INSTANCE(8), /** The link instance. */
	LINK_INSTANCE(9), /** The topic instance. */
	TOPIC_INSTANCE(10), /** The comment instance. */
	COMMENT_INSTANCE(11), /** The group. */
	ARCHIVED_INSTANCE(15);

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
	 * Gets the {@link EmfEntityIdType} by the given object instance. If the object instance is not recognized then
	 * {@link #UNKNOWN} type will be returned.
	 * <p>
	 * NOTE: this is implementation specific method!
	 *
	 * @param object
	 *            the object
	 * @return the type
	 */
	public static EmfEntityIdType getType(Object object) {
		if (object instanceof CommonInstance) {
			return INSTANCE;
		} else if (object instanceof LinkInstance || object instanceof LinkEntity || object instanceof LinkReference) {
			return LINK_INSTANCE;
		} else if (object instanceof ArchivedInstance || object instanceof ArchivedEntity) {
			return ARCHIVED_INSTANCE;
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
