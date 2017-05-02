/*
 *
 */
package com.sirma.itt.cmf.beans.entity;

import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.template.TemplateEntity;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * The Enum EntityIdType.
 *
 * @author BBonev
 */
public enum EntityIdType implements EntityType {

	/** The unknown. */
	UNKNOWN(0), /** The case. */
	CASE(1), /** The section. */
	SECTION(2), /** The document. */
	DOCUMENT(3), /** The workflow. */
	WORKFLOW(4), /** The workflow task. */
	TASK(5), /** The user. */
	USER(6), /** The {@link CommonInstance} representation type. */
	INSTANCE(7), /** The value instance. */
	VALUE_INSTANCE(8), /** The link instance. */
	LINK_INSTANCE(9), /** The topic instance. */
	TOPIC_INSTANCE(10), /** The comment instance. */
	COMMENT_INSTANCE(11), /** The comment instance. */
	TEMPLATE_INSTANCE(14);

	/** The id. */
	private int id;

	/**
	 * Instantiates a new entity id type.
	 *
	 * @param id
	 *            the id
	 */
	private EntityIdType(int id) {
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
	 * Gets the {@link EntityIdType} by the given object instance. If the object instance is not recognized then
	 * {@link #UNKNOWN} type will be returned.
	 * <p>
	 * NOTE: this is implementation specific method!
	 *
	 * @param object
	 *            the object
	 * @return the type
	 */
	public static EntityIdType getType(Object object) {
		if (object instanceof CommonInstance) {
			return INSTANCE;
		} else if (object instanceof LinkInstance || object instanceof LinkEntity || object instanceof LinkReference) {
			return LINK_INSTANCE;
		} else if (object instanceof TemplateInstance || object instanceof TemplateEntity) {
			return TEMPLATE_INSTANCE;
		} else {
			// TODO: add checks for other supported types
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
